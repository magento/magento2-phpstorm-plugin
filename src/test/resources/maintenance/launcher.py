#!/usr/bin/env python3
"""Stand-in for bin/clinotty. Only the maintenance tool's read probes are accepted."""
import json
from pathlib import Path
import sys

args = sys.argv[1:]
state = json.loads(Path("observation.json").read_text())
with Path("probe-log").open("a") as log:
    print(json.dumps(args), file=log)
if args == ["bin/magento", "setup:db:status", "--no-ansi", "--no-interaction"]:
    print(state["setup"])
    sys.exit(state["exit"])
if args[0] == "composer" and "--no-plugins" in args and "--no-scripts" in args:
    if "validate" in args and "--check-lock" in args:
        print(state.get("validation", "./composer.json is valid"))
        sys.exit(state.get("validationExit", 0))
    if "config" in args and "--absolute" in args:
        print("/app/custom/bin" if "bin-dir" in args else "/app/custom")
        sys.exit(0)
if args[:2] == ["php", "-r"]:
    if "getInvalidated()" in args[2]:
        if state.get("cacheExit"):
            sys.exit(state["cacheExit"])
        print(json.dumps(state["cache"]))
        sys.exit(0)
    if "installed.json" in args[2] and args[-1] == "/app/custom":
        print(json.dumps(state["inventory"]))
        sys.exit(0)
    if "RequiredPool" in args[2] and args[-1] == "/app/custom":
        print(json.dumps(state["expected"]))
        sys.exit(0)
if args == ["/app/custom/bin/ece-patches", "status", "--format=json", "--no-interaction"]:
    print(json.dumps(state["rows"]))
    sys.exit(0)
print("Unexpected probe", file=sys.stderr)
sys.exit(90)
