#!/usr/bin/env python3
"""Settings/environment contracts over the actual HCE LSP tool transport."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import unittest

from lsp_client import HceClient


class HceRuntimeContracts(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.settings = ARGS.output / "settings"
        cls.sequence = 0

    def fixture(self, name):
        project = ARGS.output / name
        repo = Path(__file__).resolve().parents[3]
        shutil.copytree(repo / "src/test/testData/project/magento2", project / "src")
        (project / ".idea").mkdir()
        (project / ".idea/modules.xml").write_text('''<project version="4"><component name="ProjectModuleManager"><modules>
<module fileurl="file://$PROJECT_DIR$/fixture.iml" filepath="$PROJECT_DIR$/fixture.iml"/>
</modules></component></project>''')
        (project / "fixture.iml").write_text('''<module type="JAVA_MODULE" version="4"><component name="NewModuleRootManager">
<content url="file://$MODULE_DIR$"><sourceFolder url="file://$MODULE_DIR$" isTestSource="false"/></content>
<orderEntry type="sourceFolder" forTests="false"/></component></module>''')
        return project.resolve()

    def file(self, project, name, body, executable=False):
        file = project / name
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(body)
        if executable:
            file.chmod(0o755)
        return file

    def client(self, project):
        type(self).sequence += 1
        return HceClient(ARGS.runtime, ARGS.plugin, ARGS.license_dir, self.settings, project,
            ARGS.output / ("session-%d" % self.sequence))

    def test_settings_wrappers_restart_and_project_isolation(self):
        project = self.fixture("docker-project")
        self.file(project, "compose.yaml", "services: {}")
        self.file(project, "bin/magento", "#!/bin/sh\ntouch executed", True)
        self.file(project, "src/bin/magento", "#!/bin/sh\ntouch executed", True)
        self.file(project, "tools/custom", "#!/bin/sh\ntouch executed", True)
        self.file(project, "src/bin/n98-magerun2", "<?php throw new Exception('do not execute');")
        with self.client(project) as client:
            initial = client.call("configure_magento_project")
            self.assertFalse(initial["settings"]["enabled"])
            self.assertFalse(Path(initial["settingsFile"]).exists())
            self.assertEqual("disabled", client.call("describe_magento_cli_environment")["status"])
            configured = client.call("configure_magento_project", action="configure", enabled=True,
                magentoPath="src", cliToolCandidates=["bin/magento", "tools/custom"])
            self.assertTrue(configured["configurationWritten"])
            self.assertEqual(str(project / "src"), client.call("get_magento_root_path")["magentoPath"])
            description = client.call("describe_magento_cli_environment")
            self.assertFalse(description["runtimeProbed"])
            self.assertIn("fallback for ./bin/magento", description["description"])
            self.assertIn("magento_magerun", description["description"])
            self.assertIn("magento_skills", description["description"])
            before = Path(configured["settingsFile"]).read_bytes()
            invalid = client.call("configure_magento_project", action="configure", magentoPath="../../outside", enabled=True)
            self.assertEqual("invalid_request", invalid["status"])
            self.assertEqual(before, Path(configured["settingsFile"]).read_bytes())
        with self.client(project) as restarted:
            state = restarted.call("configure_magento_project")["settings"]
            self.assertTrue(state["enabled"])
            self.assertEqual(["bin/magento", "tools/custom"], state["cliToolCandidates"])
            restarted.call("configure_magento_project", action="configure", enabled=False)
            self.assertEqual("disabled", restarted.call("describe_magento_cli_environment")["status"])
        second = self.fixture("isolated-project")
        with self.client(second) as client:
            self.assertEqual("unconfigured", client.call("get_magento_root_path")["status"])
            self.assertFalse(client.call("configure_magento_project")["settings"]["enabled"])
        self.assertFalse((project / "executed").exists())
        self.assertFalse((project / "src/executed").exists())

    def test_skill_delivery_preserves_customizations_and_instructions(self):
        project = self.fixture("skills-project")
        instructions = self.file(project, "AGENTS.md", "Existing project guidance\n")
        with self.client(project) as client:
            status = client.call_text("magento_skills", agentTarget="agents")
            self.assertIn("missing", status)
            self.assertFalse((project / ".agents").exists())
            installed = client.call_text("magento_skills", mode="install",
                skills=["magento-inspect", "magento-magerun"], agentTarget="agents")
            self.assertIn("success", installed)
            skill = project / ".agents/skills/magento-inspect/SKILL.md"
            self.assertTrue(skill.is_file())
            skill.write_text("My customized skill\n")
            blocked = client.call_text("magento_skills", mode="install", skills=["magento-inspect"], agentTarget="agents")
            self.assertIn("Customized skill preserved", blocked)
            self.assertEqual("My customized skill\n", skill.read_text())
            replaced = client.call_text("magento_skills", mode="install", skills=["magento-inspect"], agentTarget="agents", overwrite=True)
            self.assertIn("updated", replaced)
            self.assertEqual("Existing project guidance\n", instructions.read_text())
            discovery = client.call_text("magento_intelligence")
            self.assertIn("magento_skills", discovery)
            self.assertIn("magento_magerun", discovery)

    def test_magerun_metadata_process_cache_and_failure_boundaries(self):
        project = self.fixture("magerun-project")
        repo = Path(__file__).resolve().parents[3]
        self.file(project, "compose.yaml", "services: {}")
        self.file(project, "bin/n98-magerun2", 'bin/cli php bin/n98-magerun2.phar "$@"', True)
        self.file(project, "bin/n98-magerun2.phar", "<?php // fixture installation marker")
        self.file(project, "bin/clinotty", (Path(__file__).parent / "metadata_launcher.py").read_text(), True)
        catalogue = json.loads((repo / "src/test/resources/magerun/10.1.0-catalogue.json").read_text())
        catalogue["commands"].append({"name": "mcp:server:start", "description": "Fixture MCP server command"})
        self.file(project, ".metadata/catalogue.json", json.dumps(catalogue))
        self.file(project, ".metadata/sys-info-help.json",
            (repo / "src/test/resources/magerun/10.1.0-sys-info-help.json").read_text())
        with self.client(project) as client:
            client.call("configure_magento_project", action="configure", magentoPath="src", enabled=True)
            status = client.call("magento_magerun")
            self.assertEqual("detected", status["status"])
            self.assertFalse((project / "metadata-calls.jsonl").exists())
            commands = client.call("magento_magerun", mode="commands", search="sys", limit=1)
            self.assertEqual("ok", commands["status"])
            self.assertEqual("sys:info", commands["commands"][0]["name"])
            self.assertFalse(commands["cached"])
            calls = (project / "metadata-calls.jsonl").read_text()
            cached = client.call("magento_magerun", mode="commands", search="sys")
            self.assertTrue(cached["cached"])
            self.assertEqual(calls, (project / "metadata-calls.jsonl").read_text())
            help_result = client.call("magento_magerun", mode="command_help", command="sys:info")
            self.assertEqual("sys:info", help_result["command"]["name"])
            native = client.call("magento_magerun", mode="mcp_config", include="sys:info")
            self.assertEqual("ok", native["status"])
            self.assertFalse(native["serverStarted"])
            self.assertFalse(native["configurationWritten"])
            self.assertEqual("invalid_request", client.call("magento_magerun", mode="command_help", command="sys:info; touch bad")["status"])
            client.call("magento_magerun", refresh=True)
            self.assertFalse(client.call("magento_magerun", mode="commands")["cached"])
            self.file(project, "composer.lock", '{"packages": []}')
            self.assertFalse(client.call("magento_magerun", mode="commands")["cached"])
            self.file(project, "bin/n98-magerun2", "#!/bin/sh\ntouch unsupported-executed", True)
            self.assertEqual("unsupported_wrapper", client.call("magento_magerun", mode="commands")["status"])
            self.assertFalse((project / "unsupported-executed").exists())
            self.assertFalse((project / "unexpected-operation").exists())

    def test_xml_diagnostics_rules_locations_limits_and_updates(self):
        project = self.fixture("diagnostics-project")
        layout_path = "src/app/code/Foo/Bar2/view/frontend/layout/default.xml"
        module_path = "src/app/code/Foo/Bar2/etc/module.xml"
        bad_layout = '<page>\n  <body><referenceBlock name="test" cacheable="false"/></body>\n</page>'
        layout = self.file(project, layout_path, bad_layout)
        module = project / module_path
        original_module = module.read_text()
        module.write_text(original_module.replace("Foo_Bar2", "Wrong_Module"))
        with self.client(project) as client:
            def check(path=layout_path, **parameters):
                return client.call("magento_diagnostics", mode="check", filePath=path, **parameters)
            self.assertIn("magento_diagnostics", client.schemas)
            help_result = client.call("magento_diagnostics")
            ids = {r["ruleId"] for r in help_result["rules"]}
            self.assertEqual({"CacheableFalseInDefaultLayoutInspection", "ModuleDeclarationInModuleXmlInspection"}, ids)
            self.assertEqual("disabled", check()["status"])
            client.call("configure_magento_project", action="configure", magentoPath="src", enabled=True)
            found = check()
            self.assertEqual("ok", found["status"])
            self.assertTrue(found["complete"])
            self.assertEqual(1, len(found["diagnostics"]))
            finding = found["diagnostics"][0]
            self.assertEqual("warning", finding["severity"])
            self.assertEqual(layout_path, finding["path"])
            start, end = finding["range"]["start"], finding["range"]["end"]
            self.assertEqual('cacheable="false"', bad_layout[start["offset"]:end["offset"]])
            self.assertEqual(2, start["line"])
            wrong = check(module_path)
            self.assertEqual("error", wrong["diagnostics"][0]["severity"])
            self.assertIn("Foo_Bar2", wrong["diagnostics"][0]["message"])
            self.assertEqual(module_path, wrong["diagnostics"][0]["path"])
            self.assertEqual(bad_layout, layout.read_text())  # Inspection quick fixes are never applied.
            self.assertEqual("invalid_request", check(ruleIds=["PhpClassExistence"])["status"])
            self.assertEqual("invalid_request", check(limit=0)["status"])
            self.assertEqual("invalid_request", check("../outside.xml")["status"])
            self.assertEqual("invalid_request", check(str(layout))["status"])
            layout.write_text('<page><body>' + '<block name="test" cacheable="false"/>' * 3 + '</body></page>')
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            partial = check(limit=1)
            self.assertEqual("partial", partial["status"])
            self.assertFalse(partial["complete"])
            self.assertTrue(partial["truncated"])
            self.assertEqual(3, partial["collectedFindings"])
            self.assertEqual(1, len(partial["diagnostics"]))
            layout.write_text(bad_layout.replace('cacheable="false"', 'cacheable="true"'))
            module.write_text(original_module)
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertEqual([], check()["diagnostics"])
            self.assertEqual([], check(module_path)["diagnostics"])
            layout.write_text('<page><body><!--suppress CacheableFalseInDefaultLayoutInspection -->'
                '<referenceBlock name="test" cacheable="false"/></body></page>')
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertEqual([], check()["diagnostics"])
            layout.write_text('<page><body><block></page>')
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            invalid = check()
            self.assertEqual("invalid_xml", invalid["status"])
            self.assertFalse(invalid["complete"])

    def test_ui_template_candidates_shared_fixture_and_live_updates(self):
        project = self.fixture("ui-template-project")
        repo = Path(__file__).resolve().parents[3]
        fixture = json.loads((repo / "src/test/resources/mcp/ui-template-query.json").read_text())
        for path, content in fixture["files"].items():
            self.file(project, path, content)
        for index in range(fixture["boundedCount"]):
            self.file(project, "src/app/code/Foo/Bar2/view/frontend/web/template/bounds/%d/wire-bounded.html" % index, "<div/>")
        with self.client(project) as client:
            def query(path):
                return client.call_text("magento_inspect", mode="query", queryType="ui_template",
                    parametersJson=json.dumps({"templatePath": path}))
            self.assertIn("- `ui_template`:", client.call_text("magento_inspect", mode="help"))
            self.assertIn("Required: templatePath", client.call_text("magento_inspect", mode="detailed_schema", queryType="ui_template"))
            self.assertIn("disabled", query("Foo_Bar2/widget"))
            client.call("configure_magento_project", action="configure", magentoPath="src", enabled=True)
            for case in fixture["cases"]:
                result = query(case["templatePath"])
                self.assertEqual(case["paths"], [line[6:] for line in result.splitlines() if line.startswith("file: ")], result)
                self.assertIn("normalizedPath: " + case["normalizedPath"], result)
                self.assertIn("truncated: false", result)
                self.assertIn("no effective theme fallback", result)
                if not case["paths"]:
                    self.assertIn("No UI template files matched", result)
            bounded = query("wire-bounded")
            paths = [line[6:] for line in bounded.splitlines() if line.startswith("file: ")]
            self.assertEqual(20, len(paths), bounded)
            self.assertEqual(sorted(paths), paths)
            self.assertIn("totalCandidates: 21", bounded)
            self.assertIn("truncated: true", bounded)
            for invalid in ["../outside", "/absolute", "https://example.test/template"]:
                self.assertIn("Invalid templatePath", query(invalid))
            added = self.file(project, "src/app/code/Foo/Bar2/view/frontend/web/template/live.html", "<div>Live update</div>")
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("file: src/app/code/Foo/Bar2/view/frontend/web/template/live.html", query("Foo_Bar2/live"))
            renamed = added.with_name("renamed.html")
            added.rename(renamed)
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("No UI template files matched", query("Foo_Bar2/live"))
            self.assertIn("file: src/app/code/Foo/Bar2/view/frontend/web/template/renamed.html", query("Foo_Bar2/renamed"))
            renamed.unlink()
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("No UI template files matched", query("Foo_Bar2/renamed"))

    def test_module_and_layout_indexes_and_live_updates(self):
        project = self.fixture("module-layout-project")
        with self.client(project) as client:
            def inspect(kind, **parameters):
                return client.call_text("magento_inspect", mode="query", queryType=kind,
                    parametersJson=json.dumps(parameters))
            client.call("configure_magento_project", action="configure", magentoPath="src", enabled=True)
            module = inspect("module", moduleName="Foo_Bar2")
            self.assertIn("path: src/app/code/Foo/Bar2", module)
            self.assertIn("editable: yes", module)
            vendor = inspect("module", moduleName="Magento_Catalog")
            self.assertIn("path: src/vendor/magento/module-catalog", vendor)
            self.assertIn("editable: no", vendor)
            self.assertIn("Foo_Bar2", inspect("module", moduleName="Bar2"))
            client.call("configure_magento_project", action="configure", enabled=False)
            self.assertIn("disabled", inspect("module", moduleName="Foo_Bar2"))
            self.assertIn("disabled", inspect("layout_entities", name="test_index_index"))
            client.call("configure_magento_project", action="configure", enabled=True)
            self.assertIn("No Magento modules matched", inspect("module", moduleName="NoSuchModuleHce"))
            path = "src/vendor/magento/module-catalog/view/frontend/layout/test_index_index.xml"
            for name, section in [("test_index_index", "layout handles"),
                                  ("test_index_index_block2", "blocks"),
                                  ("test_index_index_container2", "containers")]:
                result = inspect("layout_entities", name=name)
                self.assertIn(section, result)
                self.assertIn(path, result)
            self.assertIn("No layout handles, blocks, or containers matched",
                inspect("layout_entities", name="NoSuchLayoutHce"))
            layout = project / path
            layout.write_text(layout.read_text().replace("test_index_index_block2", "hce_updated_block")
                .replace("test_index_index_container2", "hce_updated_container"))
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("hce_updated_block -> " + path, inspect("layout_entities", name="hce_updated_block"))
            self.assertIn("hce_updated_container -> " + path, inspect("layout_entities", name="hce_updated_container"))
            self.assertIn("No layout handles, blocks, or containers matched",
                inspect("layout_entities", name="test_index_index_block2"))
            module_xml = project / "src/app/code/Foo/Bar2/etc/module.xml"
            module_xml.write_text(module_xml.read_text().replace("Foo_Bar2", "Foo_HceUpdated"))
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("path: src/app/code/Foo/Bar2", inspect("module", moduleName="Foo_HceUpdated"))
            self.assertIn("No Magento modules matched", inspect("module", moduleName="Foo_Bar2"))
            created = self.file(project, "src/app/code/Foo/Bar2/view/frontend/layout/hce_added_handle.xml",
                '<page><body><container name="hce_added_container" htmlTag="section" htmlClass="hce">'
                '<block name="hce_added_block" class="Foo\\Bar\\Block" template="Foo_Bar2::added.phtml"/>'
                '</container></body></page>')
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("hce_added_handle ->", inspect("layout_entities", name="hce_added_handle"))
            self.assertIn("template=Foo_Bar2::added.phtml", inspect("layout_entities", name="hce_added_block"))
            self.assertIn("htmlTag=section htmlClass=hce", inspect("layout_entities", name="hce_added_container"))
            created.unlink()
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("No layout handles, blocks, or containers matched",
                inspect("layout_entities", name="hce_added_block"))

    def test_shared_xml_queries_and_live_updates(self):
        project = self.fixture("analysis-project")
        self.file(project, "src/app/code/Foo/Bar/etc/adminhtml/menu.xml",
            '<config><menu><add id="Foo_Bar::hce" title="HCE Menu" resource="Magento_Catalog::test"/></menu></config>')
        with self.client(project) as client:
            def inspect(kind, parameters):
                return client.call_text("magento_inspect", mode="query", queryType=kind,
                    parametersJson=json.dumps(parameters))
            help_text = client.call_text("magento_inspect", mode="help")
            for kind in ["module", "layout_entities", "di_config", "observers_for_event", "ui_component", "ui_template", "acl_or_menu"]:
                self.assertIn("- `" + kind + "`", help_text)
                self.assertIn("queryType: " + kind, client.call_text("magento_inspect", mode="detailed_schema", queryType=kind))
            for kind in ["plugins_for_method"]:
                self.assertNotIn("- `" + kind + "`", help_text)
            self.assertIn("disabled", inspect("di_config", {"className": "Magento\\Theme\\Block\\Html\\Topmenu"}))
            client.call("configure_magento_project", action="configure", magentoPath="src", enabled=True)
            di = inspect("di_config", {"className": "Magento\\Theme\\Block\\Html\\Topmenu"})
            self.assertIn("plugin name=catalogTopmenu", di)
            self.assertIn("src/vendor/magento/module-catalog/etc/di.xml", di)
            observers = inspect("observers_for_event", {"eventName": "test_event_in_test_class"})
            self.assertIn("observer=test_observer", observers)
            self.assertIn("src/vendor/magento/module-catalog/etc/events.xml", observers)
            self.assertIn("Test Resource", inspect("acl_or_menu", {"identifier": "Magento_Catalog::test"}))
            menu = inspect("acl_or_menu", {"identifier": "Foo_Bar::hce"})
            self.assertIn("title=HCE Menu", menu)
            self.assertIn("src/app/code/Foo/Bar/etc/adminhtml/menu.xml", menu)
            self.assertIn("recently_viewed_2", inspect("ui_component", {"name": "recently_viewed_2"}))
            self.assertIn("No Magento events matched", inspect("observers_for_event", {"eventName": "__absent__"}))
            self.assertIn("Missing required parameter", inspect("di_config", {}))
            for kind in ["plugins_for_method"]:
                self.assertIn("unavailable in this host", inspect(kind, {}))
            events = project / "src/vendor/magento/module-catalog/etc/events.xml"
            events.write_text(events.read_text().replace('name="test_observer"', 'name="updated_observer"'))
            client.request("workspace/executeCommand", {"command": "wait-for-highwatermark", "arguments": []})
            self.assertIn("observer=updated_observer", inspect("observers_for_event", {"eventName": "test_event_in_test_class"}))

    def test_ddev_override_refresh_and_invalid_configuration(self):
        project = self.fixture("ddev-project")
        self.file(project, ".ddev/config.yaml", "name: base\ntype: magento2\ndocroot: src/pub\nweb_environment: [PRIVATE_TOKEN=never-export]\n")
        override = self.file(project, ".ddev/config.local.yaml", "name: commerce-local\nworking_dir: {web: '/var/www/html/src with spaces'}\n")
        with self.client(project) as client:
            client.call("configure_magento_project", action="configure", magentoPath="src", enabled=True)
            text = client.call("describe_magento_cli_environment")["description"]
            self.assertIn("commerce-local", text)
            self.assertIn("/var/www/html/src with spaces", text)
            self.assertNotIn("never-export", text)
            self.assertNotIn("PhpStorm", text)
            override.write_text("name: changed-project\n")
            self.assertIn("changed-project", client.call("describe_magento_cli_environment")["description"])
            override.write_text("name: [malformed-private-value")
            text = client.call("describe_magento_cli_environment")["description"]
            self.assertIn("invalid_configuration", text)
            self.assertNotIn("malformed-private-value", text)
            unavailable = client.call("configure_magento_project", action="configure", excludeGeneratedFiles=True)
            self.assertEqual("unsupported_capability", unavailable["status"])


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ["runtime", "plugin", "license-dir", "output"]:
        parser.add_argument("--" + name, type=Path, required=True)
    parser.add_argument("--accept-eula", action="store_true")
    ARGS = parser.parse_args()
    if not ARGS.accept_eula:
        parser.error("Read the installed runtime's EULA.txt and supply --accept-eula before starting HCE.")
    for key in ["runtime", "plugin", "license_dir", "output"]:
        setattr(ARGS, key, getattr(ARGS, key).resolve())
    ARGS.output.mkdir(parents=True, exist_ok=False)
    product = json.loads((ARGS.runtime / "product-info.json").read_text())
    assert product["buildNumber"] == "271.2085.0", product["buildNumber"]
    metadata = {"runtime": product["name"], "build": product["buildNumber"],
        "pluginSha256": hashlib.sha256(ARGS.plugin.read_bytes()).hexdigest(), "transport": "HCE LSP tool bridge"}
    suite = unittest.defaultTestLoader.loadTestsFromTestCase(HceRuntimeContracts)
    result = unittest.TextTestRunner(verbosity=2).run(suite)
    metadata.update(tests=result.testsRun, failures=len(result.failures), errors=len(result.errors), successful=result.wasSuccessful())
    (ARGS.output / "results.json").write_text(json.dumps(metadata, indent=2) + "\n")
    raise SystemExit(0 if result.wasSuccessful() else 1)
