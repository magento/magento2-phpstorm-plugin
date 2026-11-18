"""The pinned IntelliJ Language Server's stdio and native tool bridge."""
import json
import os
from pathlib import Path
import queue
import subprocess
import threading
import time


class Engine:
    def __init__(self, project, command, env, stderr, trace=None):
        self.project = project.resolve()
        self.folder = {"uri": self.project.as_uri(), "name": self.project.name}
        self.trace = trace
        self.imported = None
        self.identifier = 0
        self.messages = queue.SimpleQueue()
        self.process = subprocess.Popen(command, env=env, cwd=self.project, stdin=subprocess.PIPE,
                                        stdout=subprocess.PIPE, stderr=stderr)
        self.reader = threading.Thread(target=self._read, daemon=True)
        self.reader.start()

    def _read(self):
        try:
            while True:
                headers = {}
                while True:
                    line = self.process.stdout.readline(8192)
                    if not line:
                        raise EOFError("HCE closed its output")
                    if line in (b"\r\n", b"\n"):
                        break
                    key, value = line.split(b":", 1)
                    headers[key.lower()] = value.strip()
                size = int(headers[b"content-length"])
                if not 0 < size <= 64 * 1024 * 1024:
                    raise ValueError("Invalid HCE message length")
                self.messages.put(json.loads(self.process.stdout.read(size)))
        except (OSError, ValueError, KeyError, EOFError) as error:
            self.messages.put(error)

    def send(self, message):
        if self.trace:
            self.trace("send", message)
        body = json.dumps(message).encode()
        self.process.stdin.write(b"Content-Length: %d\r\n\r\n" % len(body) + body)
        self.process.stdin.flush()

    def receive(self, deadline):
        try:
            message = self.messages.get(timeout=max(0, deadline - time.monotonic()))
        except queue.Empty:
            raise TimeoutError("Timed out waiting for HCE") from None
        if isinstance(message, Exception):
            raise RuntimeError(str(message)) from message
        if self.trace:
            self.trace("receive", message)
        method = message.get("method")
        if method == "intellij/workspaceImportState" and message["params"]["phase"] == "FINISHED":
            self.imported = message["params"]
        if method and "id" in message:
            reply = {"jsonrpc": "2.0", "id": message["id"]}
            if method == "workspace/configuration":
                reply["result"] = [None] * len(message["params"]["items"])
            elif method == "workspace/workspaceFolders":
                reply["result"] = [self.folder]
            elif method in ("client/registerCapability", "window/workDoneProgress/create"):
                reply["result"] = None
            else:
                reply["error"] = {"code": -32601, "message": "Unsupported client method: " + method}
            self.send(reply)
        return message

    def request(self, method, params, timeout=300):
        self.identifier += 1
        self.send({"jsonrpc": "2.0", "id": self.identifier, "method": method, "params": params})
        deadline = time.monotonic() + timeout
        while True:
            message = self.receive(deadline)
            if "method" not in message and message.get("id") == self.identifier:
                if "error" in message:
                    raise RuntimeError(message["error"]["message"])
                return message["result"]

    def command(self, name, arguments=()):
        return self.request("workspace/executeCommand", {"command": name, "arguments": list(arguments)})

    def start(self, build):
        uri = self.folder["uri"]
        result = self.request("initialize", {
            "processId": os.getpid(), "clientInfo": {"name": "magento-hce", "version": "1"},
            "rootUri": uri, "workspaceFolders": [self.folder],
            "capabilities": {"workspace": {"workspaceFolders": True, "configuration": True}},
            "initializationOptions": {"buildTools": {uri: "jps"}, "projects": [{"type": "jps", "path": uri}]}})
        if result.get("serverInfo", {}).get("version") != "ILS-" + build:
            raise RuntimeError("Unexpected HCE server build")
        self.send({"jsonrpc": "2.0", "method": "initialized", "params": {}})
        deadline = time.monotonic() + 300
        while self.imported is None:
            self.receive(deadline)
        if not any(folder.get("status") == "SUCCESS" for folder in self.imported["folders"]):
            raise RuntimeError("HCE could not import the workspace: " + str(self.imported))
        self.command("wait-for-highwatermark")
        return self.command("list-mcp-tools")["tools"]

    def tool(self, name, arguments):
        return self.command("run-mcp-tool", [{"tool": name, "arguments": {
            **arguments, "projectPath": str(self.project)}}])

    def close(self, graceful=True):
        try:
            if graceful and self.process.poll() is None:
                self.request("shutdown", None, timeout=5)
                self.send({"jsonrpc": "2.0", "method": "exit"})
                self.process.stdin.close()
                self.process.wait(timeout=5)
        except (OSError, ValueError, RuntimeError, TimeoutError, subprocess.TimeoutExpired):
            pass
        finally:
            if self.process.poll() is None:
                self.process.terminate()
                try:
                    self.process.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    self.process.kill()
                    self.process.wait()
            self.process.stdin.close()
            self.reader.join(timeout=2)
            self.process.stdout.close()


def launch_command(runtime, plugins, session, settings, log, consent_hash, license_dir=None):
    options = {"idea.plugins.path": plugins, "idea.log.path": log,
               "intellij.server.eula.persistence": "false", "magento.hce.settings.dir": settings}
    if license_dir:
        options["intellij.server.license.dir"] = license_dir
    if any('"' in str(value) or "\n" in str(value) for value in options.values()):
        raise ValueError("The runtime's JVM options cannot represent quotes or newlines in these paths")
    env = dict(os.environ)
    env.pop("IJ_LAUNCHER_DEBUG", None)
    env["IJ_JAVA_OPTIONS"] = " ".join([env.get("IJ_JAVA_OPTIONS", "")] + [
        f'"-D{key}={value}"' for key, value in options.items()]).strip()
    return [str(Path(runtime) / "bin/intellij-server"), "--stdio", "--system-path=" + str(session),
            "--data-sharing=none", "--eula=" + consent_hash[:16]], env
