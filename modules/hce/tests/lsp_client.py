"""Run HCE contract fixtures using the same LSP transport as the installed launcher."""
import hashlib
import json
from pathlib import Path
import sys
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "launcher"))
from engine import Engine, launch_command


class HceClient:
    def __init__(self, runtime, plugin, license_dir, settings, project, output):
        self.project = project.resolve()
        self.index_dir = output / "rocksdb"
        output.mkdir(parents=True, exist_ok=False)
        with zipfile.ZipFile(plugin) as archive:
            archive.extractall(output / "plugins")
        self.trace = (output / "lsp.jsonl").open("w")
        self.stderr = (output / "stderr.log").open("w")
        eula = hashlib.sha256((runtime / "EULA.txt").read_bytes()).hexdigest()
        command, env = launch_command(runtime, output / "plugins", output / "session", settings,
                                      output / "log", eula, license_dir)
        command.append("--region=europe")
        self.engine = Engine(self.project, command, env, self.stderr, self.record)

    def record(self, direction, message):
        self.trace.write(json.dumps({direction: message}) + "\n")
        self.trace.flush()

    def __enter__(self):
        try:
            tools = self.engine.start("271.2085.0", self.index_dir)
            self.schemas = {t["name"]: t["inputSchema"] for t in tools}
            for name in ("configure_magento_project", "describe_magento_cli_environment", "get_magento_root_path"):
                assert name in self.schemas, name
            assert "magento_hce_probe" not in self.schemas
            return self
        except BaseException:
            self.close(False)
            raise

    def request(self, method, params):
        return self.engine.request(method, params)

    def call_text(self, name, **arguments):
        assert set(self.schemas[name].get("required", [])) <= set(arguments) | {"projectPath"}
        result = self.engine.tool(name, arguments)
        assert result["isError"] is False, result
        return result["content"]

    def call(self, name, **arguments):
        return json.loads(self.call_text(name, **arguments))

    def close(self, graceful=True):
        self.engine.close(graceful)
        self.trace.close()
        self.stderr.close()

    def __exit__(self, kind, value, traceback):
        self.close()
