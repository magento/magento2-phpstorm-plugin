#!/usr/bin/env python3
"""Build a self-contained installer pinned to the packaged plugin JARs and JetBrains runtime."""
import argparse
import hashlib
import io
import re
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parent


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plugin", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--marketplace", nargs="?", type=Path, metavar="UPDATE_JSON",
                        help="Pin the Marketplace upload response. Local previews require --plugin-file.")
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
    url = None
    if args.marketplace:
        update = json.loads(args.marketplace.read_bytes())
        update_id = update.get("id")
        if (type(update_id) is not int or update_id <= 0 or update.get("pluginId") != 8024
                or update.get("channel") != "hce-alpha" or update.get("version") != identities[0][1]):
            parser.error("Marketplace update does not match the packaged HCE plugin")
        url = f"https://plugins.jetbrains.com/plugin/download?rel=true&updateId={update_id}"
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
