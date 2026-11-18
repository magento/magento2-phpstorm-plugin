#!/usr/bin/env python3
"""Build a self-contained installer pinned to this exact plugin ZIP and JetBrains runtime."""
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
    source = parser.add_mutually_exclusive_group(required=True)
    source.add_argument("--github-url", help="HTTPS URL of the magento-hce.zip release asset")
    source.add_argument("--marketplace", action="store_true", help="Download this plugin ID and version from Marketplace")
    source.add_argument("--github-repository", help="Generate the pilot URL for OWNER/REPO and tag hce-<plugin version>")
    args = parser.parse_args()
    descriptor = ET.parse(ROOT / "src/host/resources/META-INF/plugin.xml")
    expected = (descriptor.findtext("id"), descriptor.findtext("version"))
    identities = []
    with zipfile.ZipFile(args.plugin) as plugin:
        for member in plugin.namelist():
            if member.endswith(".jar"):
                with zipfile.ZipFile(io.BytesIO(plugin.read(member))) as jar:
                    if "META-INF/plugin.xml" in jar.namelist():
                        xml = ET.fromstring(jar.read("META-INF/plugin.xml"))
                        identities.append((xml.findtext("id"), xml.findtext("version")))
    if identities != [expected]:
        parser.error("Plugin ZIP identity/version differs from the HCE descriptor")
    url = args.github_url
    if args.github_repository:
        if not re.fullmatch(r"[\w.-]+/[\w.-]+", args.github_repository):
            parser.error("Expected --github-repository OWNER/REPO")
        url = f"https://github.com/{args.github_repository}/releases/download/hce-{expected[1]}/magento-hce.zip"
    if args.marketplace:
        url = "https://plugins.jetbrains.com/plugin/download?" + urlencode({
            "pluginId": descriptor.findtext("id"), "version": descriptor.findtext("version")})
    if not url.startswith("https://"):
        parser.error("A release plugin URL must use HTTPS")
    manifest = {"plugin": {"url": url, "sha256": hashlib.sha256(args.plugin.read_bytes()).hexdigest()},
                "runtime": json.loads((ROOT / "runtime.json").read_text())}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(args.output, "w", zipfile.ZIP_DEFLATED) as archive:
        for source_file in sorted((ROOT / "launcher").glob("*.py")):
            archive.write(source_file, source_file.name)
        archive.writestr("distribution.json", json.dumps(manifest))
    print(args.output)


if __name__ == "__main__":
    main()
