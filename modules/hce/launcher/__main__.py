"""Install and run the Magento HCE plugin in a development checkout."""
import argparse
import sys

if sys.version_info < (3, 12):
    sys.exit("Magento HCE setup requires Python 3.12 or newer.")

from contextlib import contextmanager
import hashlib
import json
import os
from pathlib import Path
import platform
import shlex
import shutil
import subprocess
import tarfile
import tempfile
import urllib.request
import zipfile

from engine import Engine, launch_command
from mcp import Server, magento_tools
from workspace import check_index, digest, discover_module, lock, prepare_import, register, write


def obtain(url, staging, local=None):
    print("Verifying " + (str(local) if local else url), file=sys.stderr)
    archive = local
    if archive is None:
        archive = staging / "download"
        with urllib.request.urlopen(url, timeout=90) as source, archive.open("xb") as target:
            shutil.copyfileobj(source, target)
    return archive


def verify_runtime(runtime, manifest):
    product = json.loads((runtime / "product-info.json").read_bytes())
    if (product.get("buildNumber") != manifest["build"] or product.get("productCode") != "ILS"
            or not os.access(runtime / "bin/intellij-server", os.X_OK)):
        raise ValueError(f"Expected IntelliJ Language Server {manifest['build']} at {runtime}")


def install_runtime(home, manifest, provided):
    system = platform.system() + "-" + platform.machine()
    if system not in manifest["platforms"]:
        raise ValueError("No verified HCE distribution for " + system)
    if provided:
        runtime = provided.resolve(strict=True)
    else:
        bundle = manifest["platforms"][system]
        runtime = home / ("runtime-" + bundle["sha256"])
        if not runtime.exists():
            with tempfile.TemporaryDirectory(dir=home) as tmp:
                staging = Path(tmp)
                archive = obtain(bundle["url"], staging)
                if digest(archive) != bundle["sha256"]:
                    raise ValueError("SHA-256 mismatch for " + bundle["url"])
                if system == "Darwin-arm64":
                    subprocess.run(["unzip", "-q", str(archive), "-d", tmp], check=True)
                else:
                    with tarfile.open(archive) as package:
                        package.extractall(tmp, filter="data")
                extracted = staging / ("intellij-server-" + manifest["productVersion"])
                verify_runtime(extracted, manifest)
                extracted.rename(runtime)
    verify_runtime(runtime, manifest)
    return runtime


def install_plugin(home, manifest, provided):
    members = manifest["members"]
    key = hashlib.sha256(json.dumps(members, sort_keys=True).encode()).hexdigest()
    plugin = home / ("plugins-" + key)
    if not plugin.exists():
        with tempfile.TemporaryDirectory(dir=home) as tmp:
            staging = Path(tmp)
            if not manifest["url"] and not provided:
                raise ValueError("This local preview requires --plugin-file pointing to the built plugin ZIP.")
            archive = obtain(manifest["url"], staging, provided)
            extracted = staging / "plugins"
            with zipfile.ZipFile(archive) as package:
                files = [entry for entry in package.infolist() if not entry.is_dir()]
                if sorted(entry.filename for entry in files) != sorted(members):
                    raise ValueError("Unexpected plugin archive contents")
                for entry in files:
                    expected = members[entry.filename]
                    if entry.file_size != expected["size"]:
                        raise ValueError("Size mismatch for " + entry.filename)
                    target = Path(package.extract(entry, extracted))
                    if digest(target) != expected["sha256"]:
                        raise ValueError("SHA-256 mismatch for " + entry.filename)
            extracted.rename(plugin)
    return plugin


def confirm(question):
    print(question, end=" ", file=sys.stderr, flush=True)
    return input().strip().lower() == "yes"


def consent(home, runtime, accepted):
    agreement = runtime / "EULA.txt"
    fingerprint = digest(agreement)
    receipt = home / "consent" / fingerprint
    if not receipt.exists():
        if not accepted:
            if not sys.stdin.isatty():
                raise ValueError(f"Read {agreement} and run setup with --accept-eula to accept it.")
            print(agreement.read_text(), file=sys.stderr)
            if not confirm("Accept this JetBrains agreement? [yes/no]"):
                raise ValueError("JetBrains agreement declined.")
        write(receipt, (fingerprint + "\n").encode())
    return fingerprint


def verify_license(runtime, env):
    launcher = str(runtime / "bin/intellij-server")

    def active():
        result = subprocess.run([launcher, "license", "status", "--json"], env=env,
                                stdout=subprocess.PIPE, timeout=90)
        if result.returncode not in (0, 2):
            result.check_returncode()
        state = json.loads(result.stdout)
        return result.returncode == 0 and state.get("activeLicense")

    if active():
        return
    login = [launcher, "license", "login", "--code"]
    if sys.stdin.isatty() and confirm("Sign in with JetBrains on this host? [yes/no]"):
        subprocess.run(login, env=env, stdout=sys.stderr, check=True)
        if active():
            return
    raise ValueError("Activate the runtime using " + shlex.join(login)
                     + " and rerun setup. For a custom store, use the same intellij.server.license.dir.")


@contextmanager
def open_engine(state, workspace, verify=False):
    log = workspace / "log"
    log.mkdir(exist_ok=True)
    runtime = Path(state["runtime"])
    if digest(runtime / "EULA.txt") != state["eula"]:
        raise ValueError("Runtime agreement changed. Rerun setup.")
    command, env = launch_command(runtime, state["plugins"], workspace / "index", workspace / "settings",
                                  log, state["eula"], state["license_dir"])
    if verify:
        verify_license(runtime, env)
        prepare_import(Path(state["project"]))
    with (log / "launcher.log").open("w") as errors:
        engine = Engine(Path(state["project"]), command, env, errors)
        try:
            engine.catalog = magento_tools(engine.start(state["build"], workspace / "rocksdb"))
            yield engine
        except (OSError, RuntimeError, ValueError) as error:
            raise RuntimeError(f"{error} (HCE logs: {log})") from error
        finally:
            engine.close()


def setup(args, archive):
    project = args.project.resolve(strict=True)
    magento_root = (project / args.magento_root).resolve(strict=True)
    if not magento_root.is_relative_to(project) or not (magento_root / "composer.json").is_file():
        raise ValueError("--magento-root must be a directory in --project containing composer.json")
    module = args.check_module or discover_module(magento_root)
    home = args.data_dir.expanduser().resolve()
    workspace = home / "projects" / hashlib.sha256(os.fsencode(project)).hexdigest()
    with zipfile.ZipFile(archive) as bundle:
        manifest = json.loads(bundle.read("distribution.json"))
    key = digest(archive)
    launcher = home / "launchers" / key / "magento-hce.pyz"
    with lock(home / "install.lock", wait=True):
        runtime = install_runtime(home, manifest["runtime"], args.runtime)
        agreement = consent(home, runtime, args.accept_eula)
        plugins = install_plugin(home, manifest["plugin"], args.plugin_file)
        if not launcher.exists():
            write(launcher, archive.read_bytes())
    state = {"project": str(project), "runtime": str(runtime), "build": manifest["runtime"]["build"],
             "license_dir": str(args.license_dir.resolve(strict=True)) if args.license_dir else None,
             "plugins": str(plugins), "eula": agreement}
    with lock(workspace / "session.lock"):
        print(f"Indexing {project} and checking {module}…", file=sys.stderr)
        with open_engine(state, workspace, verify=True) as engine:
            check_index(engine, module, magento_root)
            state["tools"] = engine.catalog
        write(workspace / (key + ".json"), json.dumps(state).encode())
        stable = workspace / "magento-hce.pyz"
        # Keep the previous client command working through upgrades and failed installs.
        with tempfile.TemporaryDirectory(dir=workspace) as tmp:
            link = Path(tmp) / "launcher"
            link.symlink_to(launcher)
            register(project, args.client, stable)
            link.replace(stable)
    print(f"Verified {module}. Magento HCE is ready for {args.client} in {project}.", file=sys.stderr)


def serve(archive):
    workspace = archive.parent
    state = json.loads((workspace / (digest(archive.resolve()) + ".json")).read_bytes())

    @contextmanager
    def connect():
        with lock(workspace / "session.lock"), open_engine(state, workspace) as engine:
            if engine.catalog != state["tools"]:
                raise ValueError("HCE tool discovery changed. Rerun setup.")
            yield engine
    Server(state["tools"], connect).run()


def main():
    os.umask(0o077)
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    install = commands.add_parser("setup", help="Install the runtime and Magento plugin for this checkout")
    install.add_argument("--client", choices=("codex", "claude", "stdio"), default="codex")
    install.add_argument("--project", type=Path, default=Path.cwd(), help="Workspace containing client settings and wrappers")
    install.add_argument("--magento-root", type=Path, default=Path("."), help="Magento root inside --project (default: .)")
    install.add_argument("--runtime", type=Path, help="Reuse an installed, pinned JetBrains runtime")
    install.add_argument("--license-dir", type=Path, help="Use an already provisioned JetBrains license store")
    install.add_argument("--plugin-file", type=Path, help="Use a local copy of the exact release ZIP")
    install.add_argument("--data-dir", type=Path, default=Path(os.environ.get("XDG_DATA_HOME", Path.home() / ".local/share")) / "magento-hce")
    install.add_argument("--accept-eula", action="store_true", help="Accept the installed build's EULA.txt")
    install.add_argument("--check-module", help="Magento module to resolve during setup")
    commands.add_parser("serve", help="Run the registered project MCP server over stdio")
    args = parser.parse_args()
    archive = Path(sys.argv[0]).absolute()
    try:
        if args.command == "setup":
            setup(args, archive)
        else:
            serve(archive)
    except (OSError, EOFError, ValueError, RuntimeError, subprocess.SubprocessError, zipfile.BadZipFile,
            tarfile.TarError) as error:
        sys.exit("Magento HCE: " + str(error))


if __name__ == "__main__":
    main()
