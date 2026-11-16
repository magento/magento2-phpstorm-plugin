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
            self.assertNotIn("magento_magerun", description["description"])
            self.assertNotIn("magento_skills", description["description"])
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
