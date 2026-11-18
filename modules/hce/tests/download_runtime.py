#!/usr/bin/env python3
"""Obtain the pinned official HCE distribution without launching or activating it."""
import argparse
import hashlib
import json
from pathlib import Path
import platform
import subprocess
import tarfile
import urllib.request

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--output", type=Path, required=True)
args = parser.parse_args()
manifest = json.loads((Path(__file__).resolve().parents[1] / "runtime.json").read_text())
key = platform.system() + "-" + platform.machine()
if key not in manifest["platforms"]:
    parser.error("No verified distribution configured for " + key)
bundle = manifest["platforms"][key]
args.output.mkdir(parents=True, exist_ok=True)
archive = args.output / bundle["url"].rsplit("/", 1)[1]
if not archive.exists():
    partial = archive.with_name(archive.name + ".partial")
    try:
        urllib.request.urlretrieve(bundle["url"], partial)
        partial.replace(archive)
    finally:
        partial.unlink(missing_ok=True)
hasher = hashlib.sha256()
with archive.open("rb") as source:
    for block in iter(lambda: source.read(1024 * 1024), b""):
        hasher.update(block)
digest = hasher.hexdigest()
if digest != bundle["sha256"]:
    raise RuntimeError("Runtime checksum mismatch; remove " + str(archive) + " and retry")
runtime = args.output / ("intellij-server-" + manifest["productVersion"])
if not runtime.exists():
    if archive.name.endswith(".tar.gz"):
        with tarfile.open(archive) as package:
            package.extractall(args.output, filter="data")
    else:
        # Matches the public JetBrains installer's macOS extraction mechanism;
        # preserves executable modes and the runtime's relative symbolic links.
        subprocess.run(["unzip", "-q", str(archive), "-d", str(args.output)], check=True)
product = json.loads((runtime / "product-info.json").read_text())
assert product["buildNumber"] == manifest["build"], product["buildNumber"]
print(runtime.resolve())
