"""Verify the HCE artifact's dependency/registration boundary without running HCE."""
import io
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
import zipfile

skill_root = Path(__file__).resolve().parents[3] / "skills"

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
                assert not any("Probe" in name or "PhpMethodPluginQueryProvider" in name for name in names)
                assert "com/magento/idea/magento2plugin/hce/AnalysisToolset.class" in names
                assert "com/magento/idea/magento2plugin/hce/RuntimeToolset.class" in names
                expected = {f"skills/{p.parent.name}/SKILL.md": p for p in skill_root.glob("*/SKILL.md")}
                packaged = {name for name in names if name.startswith("skills/") and name.endswith("/SKILL.md")}
                assert expected and packaged == expected.keys(), (packaged, expected.keys())
                for resource, source in expected.items():
                    assert contents.read(resource) == source.read_bytes(), f"Outdated bundled skill: {resource}"
                for inspection in ("CacheableFalseInDefaultLayoutInspection", "ModuleDeclarationInModuleXmlInspection"):
                    assert f"com/magento/idea/magento2plugin/inspections/xml/{inspection}.class" in names
                for probe in ("cache", "patches", "inventory"):
                    assert f"maintenance/{probe}.php" in names
                assert "com/magento/idea/magento2plugin/mcp/MaintenanceService.class" in names
                assert not any("MaintenanceDesktop" in name or "MaintenanceWidget" in name for name in names)
                assert "magento2/inspection.properties" in names
                assert "com/magento/idea/magento2plugin/util/magento/ui/TemplatePathResolver.class" in names
                assert "com/magento/idea/magento2plugin/util/magento/js/KnockoutTemplatePathResolver.class" not in names
                assert "com/magento/idea/magento2plugin/mcp/MagentoModuleDependencyQueries.class" in names
                assert "META-INF/withJson.xml" in names
                assert "META-INF/withXml.xml" in names
                xml = ET.fromstring(contents.read("META-INF/withXml.xml"))
                assert {e.get("implementation") for e in xml.findall(".//fileBasedIndex")} == {
                    "com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex",
                    "com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex",
                    "com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex"}
                assert xml.find(".//projectService").get("serviceImplementation") == \
                    "com.magento.idea.magento2plugin.hce.AnalysisSettings"
    assert len(descriptors) == 1
    descriptor = descriptors[0]
    assert descriptor.findtext("id") == "com.magento.idea.magento2plugin"
    assert descriptor.findtext("version").endswith("-hce")
    assert descriptor.find("idea-version").attrib == {"since-build": "271.2085", "until-build": "271.*"}
    assert {element.text for element in descriptor.findall("depends") if element.get("optional") != "true"} == {
        "com.intellij.modules.platform", "org.jetbrains.ls.plugin.mcp"}
    assert descriptor.find("depends[@config-file='withXml.xml']").text == "com.intellij.modules.xml"
    assert descriptor.find("depends[@config-file='withJson.xml']").text == "com.intellij.modules.json"
print("HCE archive boundary verified: one host plugin, JSON/YAML libraries, no bundled IDE or PHP classes")
