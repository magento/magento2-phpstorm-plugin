"""Expose the Magento tools discovered by HCE as a line-delimited MCP server."""
import json
import sys
from contextlib import ExitStack

VERSIONS = ("2025-11-25", "2025-06-18", "2025-03-26", "2024-11-05")


def magento_tools(discovered):
    tools = {}
    for tool in discovered:
        name = tool["name"]
        if not (name.startswith("magento_") or name in (
                "get_magento_root_path", "describe_magento_cli_environment", "configure_magento_project")):
            continue
        schema = dict(tool["inputSchema"])
        schema["properties"] = {k: v for k, v in schema.get("properties", {}).items() if k != "projectPath"}
        schema["required"] = [k for k in schema.get("required", []) if k != "projectPath"]
        tools[name] = {"name": name, "description": tool["description"], "inputSchema": schema}
    return tools


class RpcError(Exception):
    def __init__(self, code, message):
        self.code = code
        super().__init__(message)


class Server:
    def __init__(self, catalog, connect):
        self.catalog = catalog
        self.connect = connect
        self.engine = None
        self.stack = ExitStack()
        self.initialized = False

    def dispatch(self, method, params):
        if method == "initialize":
            if self.initialized:
                raise RpcError(-32600, "Already initialized")
            self.initialized = True
            version = params.get("protocolVersion")
            return {"protocolVersion": version if version in VERSIONS else VERSIONS[0],
                    "capabilities": {"tools": {}}, "serverInfo": {"name": "magento-hce", "version": "1"}}
        if method == "ping":
            return {}
        if not self.initialized:
            raise RpcError(-32600, "Initialize the server first")
        if method == "tools/list":
            return {"tools": list(self.catalog.values())}
        if method != "tools/call":
            raise RpcError(-32601, "Unknown method: " + method)
        name = params.get("name")
        args = params.get("arguments", {})
        if not isinstance(name, str) or name not in self.catalog or not isinstance(args, dict):
            raise RpcError(-32602, "Unknown tool or invalid arguments")
        schema = self.catalog[name]["inputSchema"]
        if set(schema["required"]) - args.keys() or args.keys() - schema["properties"].keys():
            raise RpcError(-32602, "Arguments do not match the advertised tool schema")
        try:
            if self.engine is None:
                self.engine = self.stack.enter_context(self.connect())
            value = self.engine.tool(name, args)
            return {"content": [{"type": "text", "text": value["content"]}], "isError": value["isError"]}
        except (OSError, RuntimeError, ValueError) as error:
            self.stack.close()
            self.engine = None
            return {"content": [{"type": "text", "text": str(error)}], "isError": True}

    def handle(self, line):
        identifier = None
        try:
            try:
                request = json.loads(line)
            except (UnicodeError, ValueError):
                raise RpcError(-32700, "Invalid JSON")
            if not isinstance(request, dict) or request.get("jsonrpc") != "2.0" or not isinstance(request.get("method"), str):
                raise RpcError(-32600, "Invalid JSON-RPC request")
            if "id" not in request:
                return None
            identifier = request["id"]
            params = request.get("params", {})
            if not isinstance(params, dict):
                raise RpcError(-32602, "Expected named parameters")
            result = self.dispatch(request["method"], params)
            return {"jsonrpc": "2.0", "id": identifier, "result": result}
        except RpcError as error:
            return {"jsonrpc": "2.0", "id": identifier, "error": {"code": error.code, "message": str(error)}}

    def run(self):
        try:
            for line in sys.stdin.buffer:
                reply = self.handle(line)
                if reply is not None:
                    print(json.dumps(reply), flush=True)
        finally:
            self.stack.close()
