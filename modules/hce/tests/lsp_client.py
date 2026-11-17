"""Real intellij-server stdio transport for HCE contract tests (no IDE test application)."""
import hashlib
import json
import os
from pathlib import Path
import queue
import subprocess
import threading
import time
import zipfile


class HceClient:
    def __init__(self, runtime, plugin, license_dir, settings, project, output):
        self.runtime, self.project, self.output = runtime, project.resolve(), output
        output.mkdir(parents=True, exist_ok=False)
        with zipfile.ZipFile(plugin) as archive:
            archive.extractall(output / "plugins")
        self.trace = (output / "lsp.jsonl").open("w")
        self.stderr = (output / "stderr.log").open("w")
        options = ["-Didea.plugins.path=" + str(output / "plugins"),
                   "-Didea.log.path=" + str(output / "log"),
                   "-Dintellij.server.license.dir=" + str(license_dir),
                   "-Dintellij.server.eula.persistence=false",
                   "-Dmagento.hce.settings.dir=" + str(settings)]
        env = dict(os.environ)
        env.pop("IJ_LAUNCHER_DEBUG", None)
        env["IJ_JAVA_OPTIONS"] = " ".join('"' + option + '"' for option in options)
        eula = hashlib.sha256((runtime / "EULA.txt").read_bytes()).hexdigest()[:16]
        self.process = subprocess.Popen([str(runtime / "bin/intellij-server"), "--stdio",
            "--system-path=" + str(output / "session"), "--eula=" + eula,
            "--region=europe", "--data-sharing=none"],
            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=self.stderr, env=env)
        self.messages = queue.Queue()
        self.notifications = []
        self.identifier = 0
        self.closed = False
        self.reader = threading.Thread(target=self._read, daemon=True)
        self.reader.start()

    def _read(self):
        try:
            while True:
                headers = {}
                while True:
                    line = self.process.stdout.readline()
                    if not line:
                        return
                    if line in (b"\n", b"\r\n"):
                        break
                    key, value = line.decode().split(":", 1)
                    headers[key.lower()] = value.strip()
                self.messages.put(json.loads(self.process.stdout.read(int(headers["content-length"]))))
        except Exception as error:
            self.messages.put({"reader_error": repr(error)})

    def send(self, message):
        self.trace.write(json.dumps({"send": message}) + "\n")
        self.trace.flush()
        body = json.dumps(message).encode()
        self.process.stdin.write(("Content-Length: %d\r\n\r\n" % len(body)).encode() + body)
        self.process.stdin.flush()

    def _response(self, identifier=None, notification=None):
        if notification:
            existing = next((m for m in self.notifications if notification(m)), None)
            if existing:
                return existing
        deadline = time.monotonic() + 60
        while time.monotonic() < deadline:
            try:
                message = self.messages.get(timeout=0.5)
            except queue.Empty:
                if self.process.poll() is not None:
                    raise RuntimeError("HCE exited %s; inspect %s" % (self.process.returncode, self.output))
                continue
            self.trace.write(json.dumps({"receive": message}) + "\n")
            self.trace.flush()
            if "reader_error" in message:
                raise RuntimeError(message["reader_error"])
            if "method" in message and "id" not in message:
                self.notifications.append(message)
                if notification and notification(message):
                    return message
            elif "method" in message:
                method = message["method"]
                if method == "workspace/configuration":
                    result = [None] * len(message.get("params", {}).get("items", []))
                elif method == "workspace/workspaceFolders":
                    result = [{"uri": self.project.as_uri(), "name": "magento-fixture"}]
                elif method in ("client/registerCapability", "window/workDoneProgress/create"):
                    result = None
                else:
                    self.send({"jsonrpc": "2.0", "id": message["id"], "error": {
                        "code": -32601, "message": "Test client does not implement " + method}})
                    continue
                self.send({"jsonrpc": "2.0", "id": message["id"], "result": result})
            elif notification is None and message.get("id") == identifier:
                if "error" in message:
                    raise RuntimeError(json.dumps(message["error"]))
                return message["result"]
        raise TimeoutError("HCE response timed out; inspect " + str(self.output))

    def request(self, method, params):
        self.identifier += 1
        self.send({"jsonrpc": "2.0", "id": self.identifier, "method": method, "params": params})
        return self._response(self.identifier)

    def __enter__(self):
        try:
            root = self.project.as_uri()
            initialized = self.request("initialize", {"processId": os.getpid(),
                "clientInfo": {"name": "magento-hce-contract-tests", "version": "1"},
                "rootUri": root, "workspaceFolders": [{"uri": root, "name": "magento-fixture"}],
                "capabilities": {"workspace": {"workspaceFolders": True, "configuration": True}},
                "initializationOptions": {"buildTools": {root: "jps"}, "projects": [{"type": "jps", "path": root}]}})
            assert initialized["serverInfo"]["version"] == "ILS-271.2085.0", initialized["serverInfo"]
            self.send({"jsonrpc": "2.0", "method": "initialized", "params": {}})
            imported = self._response(notification=lambda m: m.get("method") == "intellij/workspaceImportState"
                and m.get("params", {}).get("phase") == "FINISHED")
            assert any(f.get("status") == "SUCCESS" for f in imported["params"]["folders"]), imported
            self.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            tools = self.request("workspace/executeCommand", {"command": "list-mcp-tools", "arguments": []})
            self.schemas = {t["name"]: t["inputSchema"] for t in tools["tools"]}
            for name in ["configure_magento_project", "describe_magento_cli_environment", "get_magento_root_path"]:
                assert name in self.schemas, name
            assert "magento_hce_probe" not in self.schemas
            return self
        except BaseException:
            self.close(graceful=False)
            raise

    def call(self, name, **arguments):
        return json.loads(self.call_text(name, **arguments))

    def call_text(self, name, **arguments):
        schema = self.schemas[name]
        arguments["projectPath"] = str(self.project)
        assert set(schema.get("required", [])).issubset(arguments), schema
        result = self.request("workspace/executeCommand", {"command": "run-mcp-tool",
            "arguments": [{"tool": name, "arguments": arguments}]})
        assert result["isError"] is False, result
        return result["content"]

    def close(self, graceful=True):
        if self.closed:
            return
        self.closed = True
        try:
            if graceful and self.process.poll() is None:
                self.request("shutdown", None)
                self.send({"jsonrpc": "2.0", "method": "exit"})
                self.process.stdin.close()
                assert self.process.wait(timeout=15) == 0
        finally:
            if self.process.poll() is None:
                self.process.terminate()
                try:
                    self.process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    self.process.kill()
                    self.process.wait()
            self.reader.join(timeout=2)
            self.trace.close()
            self.stderr.close()

    def __exit__(self, kind, value, traceback):
        self.close()
