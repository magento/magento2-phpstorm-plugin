"""Local workspace preparation, client registration and index verification."""
import fcntl
import hashlib
import json
from contextlib import contextmanager
from pathlib import Path
import sys
import tempfile
import tomllib
import xml.etree.ElementTree as ET


def digest(path):
    with path.open("rb") as source:
        return hashlib.file_digest(source, "sha256").hexdigest()


@contextmanager
def lock(path, wait=False):
    path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
    with path.open("a") as stream:
        try:
            fcntl.flock(stream, fcntl.LOCK_EX | (0 if wait else fcntl.LOCK_NB))
        except BlockingIOError:
            raise RuntimeError(f"Another HCE session is using {path.parent}. Close it and retry.") from None
        yield


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(dir=path.parent, delete=False) as output:
        temp = Path(output.name)
        try:
            output.write(data)
            output.flush()
            if path.exists():
                temp.chmod(path.stat().st_mode & 0o777)
            temp.replace(path)
        finally:
            temp.unlink(missing_ok=True)


def discover_module(project):
    for pattern in ("app/code/*/*/etc/module.xml", "vendor/*/*/etc/module.xml"):
        for file in project.glob(pattern):
            module = ET.parse(file).find("module")
            if module is not None and module.get("name"):
                return module.get("name")
    raise ValueError("No Magento module found. Point --magento-root at the Magento directory with its Composer dependencies installed.")


def prepare_import(project):
    idea = project / ".idea"
    if idea.is_symlink():
        raise ValueError("Cannot configure a symlinked .idea directory")
    idea.mkdir(exist_ok=True)
    modules = idea / "modules.xml"
    if modules.exists():
        return
    module = idea / "magento-hce.iml"
    body = ('<module type="JAVA_MODULE" version="4"><component name="NewModuleRootManager">'
            '<content url="file://$MODULE_DIR$/.."><sourceFolder url="file://$MODULE_DIR$/.." '
            'isTestSource="false"/></content><orderEntry type="sourceFolder" forTests="false"/>'
            '</component></module>\n')
    try:
        with module.open("x") as output:
            output.write(body)
    except FileExistsError:
        if module.is_symlink() or module.read_text() != body:
            raise ValueError(f"Existing module descriptor conflicts with setup: {module}")
    with modules.open("x") as output:
        output.write('<project version="4"><component name="ProjectModuleManager"><modules>'
                     '<module fileurl="file://$PROJECT_DIR$/.idea/magento-hce.iml" '
                     'filepath="$PROJECT_DIR$/.idea/magento-hce.iml"/></modules></component></project>\n')


def register(project, client, launcher):
    command = {"command": sys.executable, "args": [str(launcher), "serve"]}
    if client == "stdio":
        print(json.dumps({"mcpServers": {"magento-hce": command}}, indent=2))
        return
    dest = project / (".codex/config.toml" if client == "codex" else ".mcp.json")
    if dest.is_symlink() or dest.parent.is_symlink():
        raise ValueError(f"Cannot update symlinked client settings: {dest}")
    old = dest.read_text() if dest.exists() else ""
    if client == "codex":
        existing = tomllib.loads(old).get("mcp_servers", {}).get("magento-hce")
        addition = ('[mcp_servers.magento-hce]\ncommand = ' + json.dumps(command["command"], ensure_ascii=False)
                    + '\nargs = ' + json.dumps(command["args"], ensure_ascii=False) + '\n')
        updated = old.rstrip("\n") + ("\n\n" if old else "") + addition
        # Parsing the combined document also catches inline-table and dotted-key conflicts.
        if existing is None:
            tomllib.loads(updated)
    else:
        document = json.loads(old) if old else {}
        servers = document.setdefault("mcpServers", {})
        existing = servers.get("magento-hce")
        servers["magento-hce"] = {"type": "stdio", **command}
        updated = json.dumps(document, indent=2, ensure_ascii=False) + "\n"
    if existing is not None:
        if (isinstance(existing, dict) and existing.get("command") == command["command"]
                and existing.get("args") == command["args"] and existing.get("type", "stdio") == "stdio"):
            return
        raise ValueError(f"Existing magento-hce entry in {dest} points to another launcher")
    write(dest, updated.encode())


def check_index(engine, module, magento_root):
    def call(name, **args):
        reply = engine.tool(name, args)
        if reply["isError"]:
            raise RuntimeError(reply["content"])
        return json.loads(reply["content"])

    settings = call("configure_magento_project")["settings"]
    if not settings["enabled"] or settings.get("magentoPath") != str(magento_root):
        result = call("configure_magento_project", action="configure", magentoPath=str(magento_root.relative_to(engine.project)), enabled=True)
        if not result["successful"]:
            raise RuntimeError("Magento configuration failed: " + str(result))
    result = call("magento_inspect", mode="query", queryType="module_dependencies",
                  parametersJson=json.dumps({"moduleName": module, "direction": "both", "limit": 1}))
    if result.get("status") not in ("ok", "partial") or not result.get("declarationCount"):
        raise RuntimeError(f"The project index could not resolve {module}: " + str(result.get("status", result)))
