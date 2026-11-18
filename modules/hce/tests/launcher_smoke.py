#!/usr/bin/env python3
"""Exercise both client registrations, a real stdio request and a warm restart."""
import argparse
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import tomllib

ROOT = Path(__file__).resolve().parents[3]


def checkout(path):
    path.mkdir()
    (path / "composer.json").write_text('{"name":"example/commerce"}')
    fixture = json.loads((ROOT / "src/test/resources/mcp/module-dependencies-query.json").read_text())
    for relative, body in fixture["files"].items():
        target = path / Path(relative).relative_to("src")
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(body)


def talk(config):
    requests = [
        ("initialize", {"protocolVersion": "2025-11-25", "capabilities": {},
                        "clientInfo": {"name": "smoke", "version": "1"}}),
        ("notifications/initialized", None),
        ("tools/list", {}),
        ("tools/call", {"name": "magento_inspect", "arguments": {"mode": "query",
                        "queryType": "module_dependencies", "parametersJson": json.dumps(
                            {"moduleName": "Graph_Feature", "direction": "dependencies"})}}),
        ("tools/call", {"name": "magento_inspect", "arguments": {"projectPath": "/elsewhere"}})]
    messages = []
    for index, (method, params) in enumerate(requests):
        message = {"jsonrpc": "2.0", "method": method}
        if params is not None:
            message.update(id=index, params=params)
        messages.append(json.dumps(message))
    response = subprocess.run([config["command"], *config["args"]], input="\n".join(messages) + "\n",
                              cwd=config.get("cwd"), text=True, stdout=subprocess.PIPE, check=True, timeout=660)
    init, catalog, call, wrong_project = map(json.loads, response.stdout.splitlines())
    assert init["result"]["protocolVersion"] == "2025-11-25", init
    tools = {tool["name"]: tool for tool in catalog["result"]["tools"]}
    assert "projectPath" not in tools["magento_inspect"]["inputSchema"]["properties"]
    assert "analyze_calls" not in tools
    assert not call["result"]["isError"], call
    result = json.loads(call["result"]["content"][0]["text"])
    assert any(edge.get("toModule") == "Graph_Core" and
               edge["source"]["path"] == "app/code/Graph/Feature/etc/module.xml" for edge in result["edges"]), result
    assert wrong_project["error"]["code"] == -32602


def smoke(launcher, plugin, runtime, directory, license_dir=None):
    project = directory / "Magento checkout"
    checkout(project)
    codex = project / ".codex/config.toml"
    claude = project / ".mcp.json"
    codex.parent.mkdir()
    codex.write_text("# preserve this\n[mcp_servers.other]\ncommand = 'other'\n")
    claude.write_text('{"mcpServers":{"other":{"command":"other"}}}')
    cmd = [sys.executable, str(launcher), "setup", "--project", str(project), "--runtime", str(runtime),
           "--data-dir", str(directory / "installed tools"), "--accept-eula", "--check-module", "Graph_Feature"]
    if license_dir:
        cmd.extend(["--license-dir", str(license_dir)])
    subprocess.run(cmd + ["--client", "codex", "--plugin-file", str(plugin)], check=True, timeout=660)
    first = codex.read_bytes()
    config = tomllib.loads(first.decode())["mcp_servers"]["magento-hce"]
    talk(config)
    # A cached reinstall works even without a plugin file or an available download endpoint.
    subprocess.run(cmd + ["--client", "claude"], check=True, timeout=660)
    second = json.loads(claude.read_text())["mcpServers"]
    assert second["other"]["command"] == "other"
    assert second["magento-hce"]["args"] == config["args"]
    talk(second["magento-hce"])
    subprocess.run(cmd + ["--client", "codex"], check=True, timeout=660)
    assert codex.read_bytes() == first and b"# preserve this" in first
    return project, config


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("launcher", "plugin", "runtime"):
        parser.add_argument("--" + name, type=Path, required=True)
    parser.add_argument("--license-dir", type=Path)
    parser.add_argument("--accept-eula", required=True, action="store_true",
                        help="Accept the installed runtime's EULA.txt for this test")
    args = parser.parse_args()
    with tempfile.TemporaryDirectory() as tmp:
        smoke(args.launcher.resolve(), args.plugin.resolve(), args.runtime.resolve(), Path(tmp), args.license_dir)
    print("Magento HCE installer smoke test passed.")
