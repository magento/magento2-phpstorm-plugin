"""Offline regression tests for release installation and MCP/LSP framing."""
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import subprocess
import sys
import tarfile
import tempfile
import unittest
from unittest.mock import patch
import zipfile

from launcher_smoke import ROOT, checkout, smoke

HCE = ROOT / "modules/hce"
sys.path.insert(0, str(HCE / "launcher"))
from workspace import lock, prepare_import, register

spec = importlib.util.spec_from_file_location("installer", HCE / "launcher/__main__.py")
installer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(installer)


# Scripted LSP peer deliberately sends a server request and an early import notification.
PEER = r'''#!/usr/bin/env python3
import json, sys
from pathlib import Path
if sys.argv[1] == "license":
    print(json.dumps({"activeLicense": {"status": "Valid"}}))
    sys.exit()
def send(**message):
    body = json.dumps(dict(jsonrpc="2.0", **message)).encode()
    sys.stdout.buffer.write(b"Content-Length: %d\r\n\r\n" % len(body) + body)
    sys.stdout.buffer.flush()
def read():
    size = int(sys.stdin.buffer.readline().split(b":")[1])
    assert sys.stdin.buffer.readline() == b"\r\n"
    return json.loads(sys.stdin.buffer.read(size))
while True:
    r = read()
    method = r.get("method")
    if method == "exit": break
    if method == "initialized": continue
    if method == "initialize":
        uri = r["params"]["rootUri"]
        send(method="workspace/workspaceFolders", id=500, params={})
        assert read()["result"][0]["uri"] == uri
        send(method="intellij/workspaceImportState", params={"phase":"FINISHED","folders":[{"status":"SUCCESS"}]})
        result = {"serverInfo":{"version":"ILS-271.2085.0"}}
    elif method == "workspace/executeCommand":
        cmd = r["params"]["command"]
        if cmd == "list-mcp-tools":
            tools = []
            for name, fields in [("magento_inspect", ["mode","queryType","parametersJson"]),
                                 ("configure_magento_project", ["action","enabled","magentoPath"]),
                                 ("analyze_calls", [])]:
                schema = {"type":"object","properties":{k:{} for k in fields+["projectPath"]},
                          "required":["projectPath"]}
                tools.append({"name":name, "inputSchema":schema, "description":name})
            result = {"tools":tools}
        elif cmd == "run-mcp-tool":
            item = r["params"]["arguments"][0]
            project = Path(item["arguments"]["projectPath"])
            assert project.resolve().as_uri() == uri
            if item["tool"] == "configure_magento_project":
                data = {"settings":{"enabled":False}, "successful":True}
            else:
                data = {"status":"ok","declarationCount":1,"edges":[{"toModule":"Graph_Core",
                        "source":{"path":"app/code/Graph/Feature/etc/module.xml"}}]}
            result = {"content":json.dumps(data),"isError":False}
        else:
            assert cmd == "wait-for-highwatermark"
            result = {}
    else:
        assert method == "shutdown"
        result = None
    send(id=r["id"], result=result)
'''


class LauncherTest(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = Path(directory.name)
        self.runtime = self.root / "runtime"
        (self.runtime / "bin").mkdir(parents=True)
        engine = self.runtime / "bin/intellij-server"
        engine.write_text(PEER)
        engine.chmod(0o755)
        (self.runtime / "product-info.json").write_text('{"productCode":"ILS","buildNumber":"271.2085.0"}')
        (self.runtime / "EULA.txt").write_text("Test fixture")
        self.plugin = self.root / "magento-hce.zip"
        jar = io.BytesIO()
        with zipfile.ZipFile(jar, "w") as archive:
            descriptor = (HCE / "src/host/resources/META-INF/plugin.xml").read_text()
            archive.writestr("META-INF/plugin.xml", descriptor.replace("${pluginVersion}", "2026.3.7-alpha.42-hce"))
        with zipfile.ZipFile(self.plugin, "w") as archive:
            archive.writestr("magento-hce/lib/test.jar", jar.getvalue())
        self.launcher = self.root / "magento-hce.pyz"
        subprocess.run([sys.executable, str(HCE / "package_launcher.py"), "--plugin", str(self.plugin),
                        "--output", str(self.launcher), "--marketplace"], check=True, stdout=subprocess.DEVNULL)

    def test_two_clients_and_restart(self):
        project, config = smoke(self.launcher, self.plugin, self.runtime, self.root)
        original = (project / ".idea/modules.xml").read_bytes()
        prepare_import(project)
        self.assertEqual(original, (project / ".idea/modules.xml").read_bytes())
        # A second editor session gets an actionable busy error and cannot corrupt the index.
        with lock(Path(config["args"][0]).parent / "session.lock"):
            with self.assertRaisesRegex(RuntimeError, "Another HCE session"):
                with lock(Path(config["args"][0]).parent / "session.lock"):
                    pass

    def test_digest_and_consent(self):
        project = self.root / "project"
        checkout(project)
        base = [sys.executable, str(self.launcher), "setup", "--project", str(project), "--runtime",
                str(self.runtime), "--data-dir", str(self.root / "data"), "--plugin-file", str(self.plugin)]
        def run(extra):
            return subprocess.run(base + extra, stdin=subprocess.DEVNULL, capture_output=True, text=True)
        self.assertIn("--accept-eula", run([]).stderr)
        self.plugin.write_bytes(b"corrupt")
        self.assertIn("SHA-256 mismatch", run(["--accept-eula"]).stderr)
        self.assertFalse((project / ".codex/config.toml").exists())
        self.assertFalse(list((self.root / "data").glob("plugins-*")))

    def test_existing_client_configuration(self):
        project = self.root / "project"
        project.mkdir()
        file = project / ".mcp.json"
        file.write_text('{"mcpServers":{"magento-hce":{"command":"custom"}}}')
        original = file.read_bytes()
        with self.assertRaisesRegex(ValueError, "another launcher"):
            register(project, "claude", self.launcher)
        self.assertEqual(original, file.read_bytes())

    def test_linux_runtime_download_and_offline_reuse(self):
        data = io.BytesIO()
        with tarfile.open(fileobj=data, mode="w:gz") as archive:
            archive.add(self.runtime, arcname="intellij-server-2027.1")
        payload = data.getvalue()
        home = self.root / "installed"
        home.mkdir()
        url = "https://download.jetbrains.com/example.tar.gz"
        manifest = {"build": "271.2085.0", "productVersion": "2027.1", "platforms": {
            "Linux-x86_64": {"url": url, "sha256": hashlib.sha256(payload).hexdigest()}}}
        with patch("platform.system", return_value="Linux"), patch("platform.machine", return_value="x86_64"):
            with patch("urllib.request.urlopen", return_value=io.BytesIO(payload)) as download:
                runtime = installer.install_runtime(home, manifest, None)
                self.assertTrue((runtime / "bin/intellij-server").is_file())
                self.assertEqual(runtime, installer.install_runtime(home, manifest, None))
                download.assert_called_once_with(url, timeout=90)
            manifest["platforms"]["Linux-x86_64"]["sha256"] = "0" * 64
            with patch("urllib.request.urlopen", return_value=io.BytesIO(payload)):
                with self.assertRaisesRegex(ValueError, "SHA-256"):
                    installer.install_runtime(home, manifest, None)
                self.assertFalse((home / ("runtime-" + "0" * 64)).exists())

    def test_installer_pins_shared_release_tag(self):
        subprocess.run([sys.executable, str(HCE / "package_launcher.py"), "--plugin", str(self.plugin),
                        "--github-repository", "example/plugin", "--release-tag", "2026.3.7-preview",
                        "--output", str(self.launcher)], check=True, stdout=subprocess.DEVNULL)
        with zipfile.ZipFile(self.launcher) as archive:
            url = json.loads(archive.read("distribution.json"))["plugin"]["url"]
        self.assertEqual("https://github.com/example/plugin/releases/download/2026.3.7-preview/magento-hce.zip", url)

    def test_pinned_marketplace_url(self):
        with zipfile.ZipFile(self.launcher) as archive:
            url = json.loads(archive.read("distribution.json"))["plugin"]["url"]
        version = "2026.3.7-alpha.42-hce"
        self.assertEqual("https://plugins.jetbrains.com/plugin/download?pluginId=com.magento.idea.magento2plugin&version=" + version, url)


if __name__ == "__main__":
    unittest.main()
