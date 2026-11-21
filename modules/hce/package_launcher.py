#!/usr/bin/env python3
"""Build a self-contained installer pinned to the packaged plugin JARs and JetBrains runtime."""
import argparse
import hashlib
import io
import re
import json
from pathlib import Path
from urllib.parse import urlencode
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parent


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plugin", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--marketplace", action="store_true", required=True,
                        help="Pin the Marketplace update for the packaged plugin")
    args = parser.parse_args()
    descriptor = ET.parse(ROOT / "src/host/resources/META-INF/plugin.xml")
    identities = []
    members = {}
    with zipfile.ZipFile(args.plugin) as plugin:
        for entry in plugin.infolist():
            if entry.is_dir():
                continue
            member = entry.filename
            if not re.fullmatch(r"magento-hce/lib/[\w.+-]+\.jar", member) or member in members:
                parser.error("Unexpected plugin archive member: " + member)
            data = plugin.read(entry)
            members[member] = {"size": len(data), "sha256": hashlib.sha256(data).hexdigest()}
            with zipfile.ZipFile(io.BytesIO(data)) as jar:
                if "META-INF/plugin.xml" in jar.namelist():
                    xml = ET.fromstring(jar.read("META-INF/plugin.xml"))
                    identities.append((xml.findtext("id"), xml.findtext("version")))
    if (len(identities) != 1 or identities[0][0] != descriptor.findtext("id")
            or not re.fullmatch(r"[0-9][A-Za-z0-9.+_-]*", identities[0][1] or "")):
        parser.error("Plugin ZIP identity/version differs from the HCE descriptor")
    url = "https://plugins.jetbrains.com/plugin/download?" + urlencode({
        "pluginId": descriptor.findtext("id"), "version": identities[0][1]})
    manifest = {"plugin": {"url": url, "members": members},
                "runtime": json.loads((ROOT / "runtime.json").read_text())}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(args.output, "w", zipfile.ZIP_DEFLATED) as archive:
        for source_file in sorted((ROOT / "launcher").glob("*.py")):
            archive.write(source_file, source_file.name)
        archive.writestr("distribution.json", json.dumps(manifest))
    print(args.output)


if __name__ == "__main__":
    main()
