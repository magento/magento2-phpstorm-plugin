"""Verify the HCE artifact's dependency/registration boundary without running HCE."""
import io
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
import zipfile

with zipfile.ZipFile(Path(sys.argv[1])) as archive:
    jars = [name for name in archive.namelist() if name.endswith(".jar")]
    assert len(jars) == 3, jars
    descriptors = []
    for jar in jars:
        with zipfile.ZipFile(io.BytesIO(archive.read(jar))) as contents:
            names = contents.namelist()
            assert not any(name.startswith(("com/intellij/", "com/jetbrains/php/")) for name in names), jar
            if "META-INF/plugin.xml" in names:
                descriptors.append(ET.fromstring(contents.read("META-INF/plugin.xml")))
                assert "com/magento/idea/magento2plugin/hce/ProjectToolset.class" in names
                assert not any("Probe" in name or name.endswith("/Settings.class") for name in names)
    assert len(descriptors) == 1
    descriptor = descriptors[0]
    assert descriptor.findtext("id") == "com.magento.idea.magento2plugin.hce"
    assert {element.text for element in descriptor.findall("depends")} == {
        "com.intellij.modules.platform", "org.jetbrains.ls.plugin.mcp"}
print("HCE archive boundary verified: one host plugin, JSON/YAML libraries, no bundled IDE or PHP classes")
