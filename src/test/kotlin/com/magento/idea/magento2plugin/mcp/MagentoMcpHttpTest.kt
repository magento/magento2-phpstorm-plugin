/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.JUnit38AssumeSupportRunner
import com.intellij.util.EnvironmentUtil
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.project.indexing.MagentoAdditionalLibraryRootsProvider
import org.json.JSONObject
import org.junit.runner.RunWith
import java.nio.file.Files
import java.nio.file.Path

/** Assertions cross the HTTP API; magerun metadata comes from an actual PHP process. */
@RunWith(JUnit38AssumeSupportRunner::class)
class MagentoMcpHttpTest : McpHttpTestCase() {
    private val root: Path get() = Path.of(project.basePath!!)

    override fun setUp() {
        super.setUp()
        Settings.getInstance(project).pluginEnabled = true
        Settings.getInstance(project).magentoPath = root.resolve("src").toString()
        file("src/app/etc/di.xml", "<config/>")
    }

    fun testDiscoversRegisteredToolsAndTypedSchemasOverHttp() {
        val discoveredTools = mcp().tools()
        assertEquals(1, discoveredTools.count { it.getString("name") == "magento_inspect" })
        val tools = discoveredTools.associateBy { it.getString("name") }
        assertTrue(tools.getValue("magento_diagnostics").getJSONObject("annotations").getBoolean("readOnlyHint"))
        assertEquals(MAGENTO_TOOLS, tools.keys.filter { it != "execute_tool" }.toSet())
        val properties = tools.getValue("magento_magerun").getJSONObject("inputSchema").getJSONObject("properties")
        for ((name, type) in listOf("mode" to "string", "command" to "string", "search" to "string",
            "offset" to "integer", "limit" to "integer", "refresh" to "boolean", "include" to "string", "exclude" to "string")) {
            assertTrue("$name: $properties", properties.getJSONObject(name).toString().contains(type))
        }
        val descriptions = mapOf(
            "magento_scaffold" to listOf("mode `help`", "mode `detailed_schema`", "mode `render`", "parametersJson", "entity_crud", "product_eav_attribute"),
            "magento_inspect" to listOf("mode `help`", "mode `detailed_schema`", "mode `query`", "queryType", "di_config", "acl_or_menu"),
            "magento_compatibility" to listOf("coverage_required"),
            "get_magento_root_path" to listOf("absolute project path", "resolved Magento root directory"),
            "describe_magento_cli_environment" to listOf("nearest Docker Compose configuration", "never search outside the project", "./bin/magento takes precedence over ./src/bin/magento"),
            "magento_magerun" to listOf("mode=status only reads local files", "mode=commands", "mode=command_help", "mode=mcp_config", "refresh=true", "never fall back to another runtime"),
            "get_magento_indexing_exclusions" to listOf("indexing exclusion", "preview"),
            "configure_magento_indexing_exclusions" to listOf("apply", "update", "reset", "preview", ".runtime/"),
            "configure_magento_project" to listOf("Inspect or configure Magento project integration", "status", "configure", "magentoPath"),
        )
        for ((name, fragments) in descriptions) {
            val description = tools.getValue(name).getString("description")
            for (fragment in fragments) assertTrue("$name: $description", description.contains(fragment))
        }
        assertEquals("Configured Magento root path: ${root.resolve("src").toString().replace('\\', '/')}", text("get_magento_root_path"))
        val exclusionsStatus = text("get_magento_indexing_exclusions")
        assertTrue(exclusionsStatus, exclusionsStatus.contains("Exclusions cannot be applied: Magento installation path is invalid"))
        assertTrue(exclusionsStatus, exclusionsStatus.contains("Configure a valid Magento path before managing indexing exclusions."))
    }

    fun testModuleDependenciesOverHttp() {
        val fixture = JSONObject(javaClass.getResource("/mcp/module-dependencies-query.json")!!.readText())
        val files = fixture.getJSONObject("files")
        for (path in files.keySet()) myFixture.addFileToProject(path, files.getString(path))
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertTrue(inspect("help").contains("- `module_dependencies`:"))
        assertTrue(inspect("detailed_schema", "module_dependencies").contains("Required: moduleName"))
        fun query(name: String, direction: String = "both", limit: Any = 20) = JSONObject(inspect("query", "module_dependencies",
            JSONObject().put("moduleName", name).put("direction", direction).put("limit", limit).toString()))
        val feature = query("Graph_Feature", "dependencies")
        assertEquals(feature.toString(), "ok", feature.getString("status"))
        assertEquals(5, feature.getInt("totalEdges"))
        val edges = feature.getJSONArray("edges").let { a -> (0 until a.length()).map { a.getJSONObject(it) } }
        assertEquals(setOf("sequence_after", "composer_require", "composer_require_dev"), edges.map { it.getString("kind") }.toSet())
        assertEquals("Graph_Core", edges.single { it.optString("toPackage") == "graph/core" }.getJSONArray("targetModules").getString(0))
        assertEquals(0, edges.single { it.optString("toPackage") == "php" }.getJSONArray("targetModules").length())
        for (edge in edges) {
            val source = edge.getJSONObject("source")
            val body = files.getString(source.getString("path"))
            val expected = edge.optString("toPackage", edge.optString("toModule"))
            assertEquals("\"$expected\"", body.substring(source.getInt("startOffset"), source.getInt("endOffset")))
            assertEquals(body.take(source.getInt("startOffset")).count { it == '\n' } + 1, source.getInt("line"))
        }
        val core = query("Graph_Core", "dependents")
        assertEquals(2, core.getInt("declarationCount"))
        assertEquals(3, core.getInt("totalEdges"))
        assertEquals("module_not_found", query("Graph_Missing").getString("status"))
        assertEquals(1, query("Graph_Missing").getInt("totalEdges"))
        assertEquals(0, query("Graph_Nothing").getInt("totalEdges"))
        assertEquals("absent", query("Graph_Loose").getJSONArray("declarations").getJSONObject(0).getString("composerStatus"))
        assertEquals("partial", query("Graph_Feature", limit = 1).getString("status"))
        for (limit in listOf(0, 101, 1.5, "2")) assertEquals("invalid_request", query("Graph_Core", limit = limit).getString("status"))
        assertEquals("invalid_request", query("Graph_Core", "sideways").getString("status"))
        // Committed, unsaved editor changes must invalidate the same native PSI cache used by MCP.
        val path = fixture.getString("featureModule")
        val psi = myFixture.findFileInTempDir(path).let { com.intellij.psi.PsiManager.getInstance(project).findFile(it)!! }
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
            val manager = com.intellij.psi.PsiDocumentManager.getInstance(project)
            val document = manager.getDocument(psi)!!
            document.setText(files.getString(path).replace("Graph_Missing", "Graph_New"))
            manager.commitDocument(document)
        }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertEquals(0, query("Graph_Missing").getInt("totalEdges"))
        assertEquals(1, query("Graph_New").getInt("totalEdges"))
        myFixture.addFileToProject(fixture.getString("featureComposer"), "{broken")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        val partial = query("Graph_Feature")
        assertEquals("partial", partial.getString("status"))
        assertFalse(partial.getBoolean("complete"))
        assertEquals(2, partial.getInt("totalEdges"))
        assertTrue(partial.getJSONArray("problems").toString().contains("invalid_json"))
    }

    fun testSharedUiTemplateCandidatesOverHttp() {
        val fixture = JSONObject(javaClass.getResource("/mcp/ui-template-query.json")!!.readText())
        val files = fixture.getJSONObject("files")
        for (path in files.keySet()) myFixture.addFileToProject(path, files.getString(path))
        for (index in 0 until fixture.getInt("boundedCount")) myFixture.addFileToProject(
            "src/app/code/Foo/Bar2/view/frontend/web/template/bounds/$index/wire-bounded.html", "<div/>")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertTrue(inspect("help").contains("- `ui_template`:"))
        assertTrue(inspect("detailed_schema", "ui_template").contains("Required: templatePath"))
        fun query(path: String) = inspect("query", "ui_template", JSONObject().put("templatePath", path).toString())
        val cases = fixture.getJSONArray("cases")
        for (index in 0 until cases.length()) {
            val case = cases.getJSONObject(index)
            val result = query(case.getString("templatePath"))
            val paths = case.getJSONArray("paths").let { values -> (0 until values.length()).map { values.getString(it) } }
            assertEquals(paths, result.lineSequence().filter { it.startsWith("file: ") }.map { it.removePrefix("file: ") }.toList())
            assertTrue(result, result.contains("normalizedPath: " + case.getString("normalizedPath")))
            assertTrue(result, result.contains("truncated: false"))
            assertTrue(result, result.contains("no effective theme fallback"))
            if (paths.isEmpty()) assertTrue(result, result.contains("No UI template files matched"))
        }
        val bounded = query("wire-bounded")
        val boundedPaths = bounded.lineSequence().filter { it.startsWith("file: ") }.toList()
        assertEquals(20, boundedPaths.size)
        assertEquals(boundedPaths.sorted(), boundedPaths)
        assertTrue(bounded, bounded.contains("totalCandidates: 21") && bounded.contains("truncated: true"))
        for (path in listOf("Magento_Ui", "Foo_Bar2/", "text!Foo_Bar2/.html", "../outside", "/absolute", "https://example.test/template")) {
            assertTrue(query(path).contains("Invalid templatePath"))
        }
        assertTrue(inspect("query", "ui_template", "{}").contains("Missing required parameter `templatePath`"))
        val live = myFixture.addFileToProject("src/app/code/Foo/Bar2/view/frontend/web/template/live.html", "<div>Live update</div>")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertTrue(query("Foo_Bar2/live").contains("file: src/app/code/Foo/Bar2/view/frontend/web/template/live.html"))
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) { live.virtualFile.rename(this, "renamed.html") }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertTrue(query("Foo_Bar2/live").contains("No UI template files matched"))
        assertTrue(query("Foo_Bar2/renamed").contains("file: src/app/code/Foo/Bar2/view/frontend/web/template/renamed.html"))
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) { live.virtualFile.delete(this) }
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertTrue(query("Foo_Bar2/renamed").contains("No UI template files matched"))
    }

    fun testXmlDiagnosticsForLibraryOnlyVendorFileOverHttp() {
        val path = "src/vendor/acme/module/view/frontend/layout/default.xml"
        val layout = myFixture.addFileToProject(path, """<page><body><block cacheable="false"/></body></page>""").virtualFile
        val vendor = myFixture.findFileInTempDir("src/vendor")!!
        ModuleRootModificationUtil.updateModel(module) { model ->
            model.contentEntries.single().addExcludeFolder(vendor)
        }
        MagentoAdditionalLibraryRootsProvider.refreshRoots(project)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertFalse(GlobalSearchScope.projectScope(project).contains(layout))
        assertTrue(GlobalSearchScope.allScope(project).contains(layout))

        val result = JSONObject(text("magento_diagnostics", JSONObject().put("mode", "check").put("filePath", path)))
        assertEquals("ok", result.getString("status"))
        val finding = result.getJSONArray("diagnostics").getJSONObject(0)
        assertEquals("CacheableFalseInDefaultLayoutInspection", finding.getString("ruleId"))
        assertEquals(path, finding.getString("path"))
    }

    fun testSharedXmlDiagnosticsOverHttp() {
        val path = "src/app/code/Inspect/Module/view/frontend/layout/default.xml"
        myFixture.addFileToProject(path, "<page><body><referenceBlock name=\"test\" cacheable=\"false\"/></body></page>")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        val help = JSONObject(text("magento_diagnostics"))
        assertEquals(2, help.getJSONArray("rules").length())
        val result = JSONObject(text("magento_diagnostics", JSONObject().put("mode", "check").put("filePath", path)))
        assertEquals("ok", result.getString("status"))
        val diagnostic = result.getJSONArray("diagnostics").getJSONObject(0)
        assertEquals("CacheableFalseInDefaultLayoutInspection", diagnostic.getString("ruleId"))
        assertEquals("warning", diagnostic.getString("severity"))
        assertEquals(path, diagnostic.getString("path"))
        val invalid = JSONObject(text("magento_diagnostics", JSONObject().put("mode", "check").put("filePath", path)
            .put("ruleIds", org.json.JSONArray(listOf("PhpClassExistence")))))
        assertEquals("invalid_request", invalid.getString("status"))
    }

    fun testSharedInspectionQueriesAndUnavailablePhpCapabilityOverHttp() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        myFixture.addFileToProject("src/app/code/Inspect/Module/etc/module.xml", """
            <config><module name="Inspect_Module"/></config>
        """.trimIndent())
        myFixture.addFileToProject("src/app/code/Inspect/Module/etc/di.xml", """
            <config><type name="Inspect\Module\Target"><plugin name="inspect_plugin" type="Inspect\Module\Plugin"/></type></config>
        """.trimIndent())
        myFixture.addFileToProject("src/app/code/Inspect/Module/etc/events.xml", """
            <config><event name="inspect_event"><observer name="inspect_observer" instance="Inspect\Module\Observer" disabled="true"/></event></config>
        """.trimIndent())
        myFixture.addFileToProject("src/app/code/Inspect/Module/view/frontend/layout/inspect_handle.xml", """
            <page><body><container name="inspect_container"><block name="inspect_block" template="Inspect_Module::example.phtml"/></container></body></page>
        """.trimIndent())
        myFixture.addFileToProject("src/app/code/Inspect/Module/view/frontend/ui_component/inspect_listing.xml", "<listing/>")
        myFixture.addFileToProject("src/app/code/Inspect/Module/etc/acl.xml", """
            <config><acl><resources><resource id="Inspect_Module::root" title="Inspect root"/></resources></acl></config>
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val help = inspect("help")
        assertFalse(help, help.contains("- `plugins_for_method`:"))
        assertTrue(help, help.contains("requires the JetBrains PHP plugin"))
        val queries = listOf(
            Triple("module", "moduleName" to "Inspect_Module", "src/app/code/Inspect/Module"),
            Triple("di_config", "className" to "Inspect\\Module\\Target", "plugin name=inspect_plugin"),
            Triple("observers_for_event", "eventName" to "inspect_event", "observer=inspect_observer instance=Inspect\\Module\\Observer disabled=true"),
            Triple("layout_entities", "name" to "inspect_block", "template=Inspect_Module::example.phtml"),
            Triple("ui_component", "name" to "inspect_listing", "rootTag: listing"),
            Triple("acl_or_menu", "identifier" to "Inspect_Module::root", "Inspect_Module::root -> title=Inspect root")
        )
        for ((queryType, parameter, expected) in queries) {
            val schema = inspect("detailed_schema", queryType)
            assertTrue(schema, schema.contains("queryType: $queryType"))
            val result = inspect("query", queryType, JSONObject().put(parameter.first, parameter.second).toString())
            assertTrue(result, result.replace('\\', '/').contains(expected.replace('\\', '/')))
        }
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            inspect("query", "plugins_for_method", "{}"))
        assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
            inspect("detailed_schema", "plugins_for_method"))
        assertTrue(inspect("query", "di_config", "{}").contains("Missing required parameter `className`"))
        assertTrue(inspect("query", "acl_or_menu", "{invalid").contains("parametersJson must be a valid JSON object"))
    }

    fun testInspectAndScaffoldRequireOnlyModeOverHttp() {
        val client = mcp()
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        val tools = client.tools().associateBy { it.getString("name") }
        for (tool in listOf("magento_inspect", "magento_scaffold")) {
            assertEquals(listOf<Any>("mode"),
                tools.getValue(tool).getJSONObject("inputSchema").getJSONArray("required").toList())
            val missingMode = client.callEnvelope(tool, JSONObject())
            assertTrue(missingMode.toString(), missingMode.has("error") ||
                missingMode.optJSONObject("result")?.optBoolean("isError") == true)
        }
        fun inspect(vararg arguments: Pair<String, String>) = text("magento_inspect", JSONObject(mapOf(*arguments)))
        fun scaffold(vararg arguments: Pair<String, String>) = text("magento_scaffold", JSONObject(mapOf(*arguments)))

        assertTrue(inspect("mode" to "help").contains("Queries:"))
        assertTrue(scaffold("mode" to "help").contains("Scaffolds:"))
        assertTrue(scaffold("mode" to "help", "scaffoldType" to "", "parametersJson" to "").contains("Scaffolds:"))
        assertTrue(inspect("mode" to "detailed_schema", "queryType" to "module").contains("queryType: module"))
        assertTrue(scaffold("mode" to "detailed_schema", "scaffoldType" to "module").contains("scaffoldType: module"))
        assertEquals(MagentoInspectionCommands.MISSING_TYPE_MESSAGE, inspect("mode" to "detailed_schema"))
        assertEquals(MagentoScaffoldCommands.MISSING_TYPE_MESSAGE, scaffold("mode" to "detailed_schema"))
        assertTrue(inspect("mode" to "query", "queryType" to "module")
            .contains("Missing required parameter `moduleName`"))
        assertTrue(scaffold("mode" to "render", "scaffoldType" to "module")
            .contains("Missing required parameter `packageName`"))
        // A missing type can never succeed, so indexing must not tell the agent to retry it.
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertEquals(MagentoInspectionCommands.MISSING_TYPE_MESSAGE, inspect("mode" to "query"))
            assertEquals(MagentoScaffoldCommands.MISSING_TYPE_MESSAGE, scaffold("mode" to "render"))
        }
    }

    private fun inspect(mode: String, queryType: String = "", parametersJson: String = ""): String =
        text("magento_inspect", JSONObject().put("mode", mode).put("queryType", queryType)
            .put("parametersJson", parametersJson))

    fun testInspectionReportsIndexingAndUnavailableCapabilityOverHttp() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        mcp()
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            for (type in listOf("module", "module_dependencies", "ui_template", "di_config", "observers_for_event", "layout_entities",
                "ui_component", "acl_or_menu")) {
                assertEquals(MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE,
                    inspect("query", type, "{}"))
            }
            assertEquals(MagentoMethodPluginQueryProvider.UNAVAILABLE_MESSAGE,
                inspect("query", "plugins_for_method", "{}"))
            assertTrue(inspect("detailed_schema", "layout_entities").contains("queryType: layout_entities"))
            assertTrue(inspect("help").contains("- `layout_entities`:"))
        }
    }

    fun testDockerDirectoryWrappersArePreferredAndDescriptionIsDeterministic() {
        file("compose.yaml", "services: {}")
        file("bin/magento", "#!/bin/sh\nexit 91\n", executable = true)
        file("src/bin/magento", "#!/bin/sh\nexit 92\n", executable = true)
        file("bin/.htaccess", "deny from all")
        file("bin/common", "# sourced helper")
        installMagerun("bin/n98-magerun2.phar")
        val first = text("describe_magento_cli_environment")
        assertEquals(first, text("describe_magento_cli_environment"))
        assertTrue(first, first.contains("./bin/magento\n  type: Magento CLI wrapper\n  selection: preferred"))
        assertTrue(first, first.contains("./src/bin/magento\n  type: Magento CLI wrapper\n  selection: fallback"))
        assertTrue(first, first.indexOf("./bin/magento\n  type: Magento CLI wrapper\n  selection: preferred") < first.indexOf("./src/bin/magento\n  type: Magento CLI wrapper\n  selection: fallback"))
        assertFalse(first.contains("./bin/.htaccess"))
        // Executability of a new helper file depends on the host filesystem.
        assertEquals(first, Files.isExecutable(root.resolve("bin/common")), first.contains("./bin/common"))
        val status = magerun()
        assertEquals("detected", status.getString("status"))
        assertFalse(status.getBoolean("runtimeProbed"))
        assertTrue(calls("bin").isEmpty())
    }

    fun testWithoutProjectComposeUsesOnlyMagentoDirectory() {
        file("bin/magento", "#!/bin/sh\nexit 91\n", executable = true)
        file("src/bin/magento", "#!/bin/sh\nexit 92\n", executable = true)
        installMagerun("bin/n98-magerun2.phar")
        installMagerun("src/bin/n98-magerun2.phar")
        val description = text("describe_magento_cli_environment")
        assertTrue(description, description.contains("./src/bin/magento\n  type: Magento CLI wrapper\n  selection: preferred"))
        assertFalse(description, description.contains("./bin/magento\n  type: Magento CLI wrapper\n  selection: preferred"))
        assertEquals(root.resolve("src/bin/n98-magerun2.phar").toString(), magerun().getJSONObject("selected").getString("path"))
        assertTrue(calls("bin").isEmpty())
        assertTrue(calls("src/bin").isEmpty())
    }

    fun testDdevDiscoveryMetadataAndNativeConfigurationOverHttp() {
        org.junit.Assume.assumeFalse("POSIX DDEV fixture", com.intellij.openapi.util.SystemInfoRt.isWindows)
        file(".ddev/config.yaml", "name: commerce-local\ntype: magento2\ndocroot: src/pub\ncomposer_root: src\nworking_dir: {web: /var/www/html/src}\n")
        file("src/fixture.php", javaClass.getResource("/magerun/http-fixture.php")!!.readText())
        file("host tools/ddev", """
            #!/bin/sh
            printf '%s\n' "${'$'}*" >> ddev-calls.txt
            [ "${'$'}1" = exec ] && [ "${'$'}2" = --raw ] && [ "${'$'}3" = --service=web ] && [ "${'$'}4" = --project=commerce-local ] && [ "${'$'}5" = --dir=/var/www/html/src ] && [ "${'$'}6" = -- ] && [ "${'$'}7" = magerun2 ] || exit 91
            if [ -f ddev-stopped ]; then echo 'DDEV project is stopped or permission denied' >&2; exit 1; fi
            if [ -f missing-magerun ]; then echo 'magerun2: command not found' >&2; exit 127; fi
            shift 7
            exec php fixture.php "${'$'}@"
        """.trimIndent() + "\n", executable = true)
        withDdevPath(root.resolve("host tools").toString() + java.io.File.pathSeparator +
            EnvironmentUtil.getValue("PATH").orEmpty()) {
            val description = text("describe_magento_cli_environment")
            assertTrue(description, description.contains("DDEV configuration: ${root.resolve(".ddev/config.yaml")}"))
            assertTrue(description, description.contains("ddev exec --raw --service=web --project=commerce-local --dir=/var/www/html/src -- php -v"))
            assertTrue(description, description.contains("PhpStorm's Terminal"))
            val status = magerun()
            assertEquals(status.toString(), "detected", status.getString("status"))
            assertEquals("ddev", status.getJSONObject("invocation").getString("runtime"))
            assertEquals(root.resolve("src").toString(), status.getJSONObject("invocation").getString("cwd"))
            assertFalse(Files.exists(root.resolve("src/ddev-calls.txt")))
            val commands = magerun("commands", JSONObject().put("search", "sys:info"))
            assertEquals(commands.toString(), "ok", commands.getString("status"))
            assertEquals(1, commands.getInt("total"))
            assertEquals("sys:info", magerun("command_help", JSONObject().put("command", "si"))
                .getJSONObject("command").getString("name"))
            val config = magerun("mcp_config").getJSONObject("configuration")
                .getJSONObject("mcpServers").getJSONObject("magento-magerun2")
            assertEquals(root.resolve("host tools/ddev").toString(), config.getString("command"))
            assertEquals(listOf("exec", "--raw", "--service=web", "--project=commerce-local", "--dir=/var/www/html/src", "--", "magerun2"), config.getJSONArray("args").toList().take(7))
            assertEquals(root.resolve("src").toString(), config.getString("cwd"))
            assertEquals(4, calls("src").size)
            for (call in calls("src")) {
                assertEquals(root.resolve("src").toString(), call.getString("cwd"))
                assertFalse(call.getJSONArray("arguments").toList().any { it.toString().startsWith("--root-dir=") })
            }
            file("src/ddev-stopped", "stopped")
            val failure = magerun("commands", JSONObject().put("refresh", true))
            assertEquals("runtime_unavailable", failure.getString("status"))
            assertTrue(failure.getString("message").contains("permission denied"))
            assertEquals("ddev exec --raw --service=web --project=commerce-local --dir=/var/www/html/src -- magerun2 --version", failure.getJSONObject("terminalGuidance").getString("command"))
            assertTrue(failure.has("invocation"))
            Files.delete(root.resolve("src/ddev-stopped"))
            file("src/missing-magerun", "missing")
            assertEquals("missing_installation", magerun("commands").getString("status"))
            assertEquals(6, Files.readAllLines(root.resolve("src/ddev-calls.txt")).size)
        }
    }

    fun testDdevMissingHostExecutableOverHttp() {
        file(".ddev/config.yaml", "name: commerce-local\ntype: magento2\ndocroot: src/pub\ncomposer_root: src\nworking_dir: {web: /var/www/html/src}\n")
        withDdevPath(root.resolve("empty-path").toString()) {
            val description = text("describe_magento_cli_environment")
            assertTrue(description, description.contains("missing_launcher"))
            val status = magerun()
            assertEquals("missing_launcher", status.getString("status"))
            assertTrue(status.getJSONObject("terminalGuidance").getString("message").contains("PhpStorm's Terminal"))
            assertEquals("missing_launcher", magerun("commands").getString("status"))
        }
    }

    fun testDdevCliConfigurationWithoutMagerunAndInvalidYamlOverHttp() {
        file(".ddev/config.yaml", "name: commerce\ntype: magento2\ndocroot: src/pub\nworking_dir: {web: /original}\n")
        file(".ddev/config.local.yaml", "name: commerce-local\nworking_dir: {web: /var/www/html/src}\nphp_version: '8.4'\n")
        val description = text("describe_magento_cli_environment")
        assertTrue(description, description.contains("--project=commerce-local --dir=/var/www/html/src -- php -v"))
        assertTrue(description, description.contains("8.4") && description.contains("src/pub"))
        assertTrue(description, description.contains(JSONObject.quote(root.resolve(".ddev/config.local.yaml").toString())))
        file(".ddev/config.local.yaml", "working_dir: [invalid")
        val invalid = text("describe_magento_cli_environment")
        assertTrue(invalid, invalid.contains("invalid_configuration"))
        assertFalse(invalid, invalid.contains("execPrefix"))
        assertEquals("invalid_configuration", magerun("commands").getString("status"))
    }

    private fun withDdevPath(path: String, test: () -> Unit) {
        val original = EnvironmentUtil.getEnvironmentMap()
        EnvironmentUtil.setEnvironmentLoader { original + ("PATH" to path) }
        try { test() } finally { EnvironmentUtil.setEnvironmentLoader { original } }
    }

    fun testMagerunCatalogueHelpCacheAndNativeConfigurationUseRealProcesses() {
        file("compose.yaml", "services: {}")
        installMagerun("bin/n98-magerun2.phar")
        installMagerun("src/bin/n98-magerun2.phar")
        val first = magerun("commands", JSONObject().put("limit", 1))
        assertEquals(first.toString(), "ok", first.getString("status"))
        assertFalse(first.getBoolean("cached"))
        assertEquals("mcp:server:start", first.getJSONArray("commands").getJSONObject(0).getString("name"))
        assertEquals(1, first.getInt("nextOffset"))
        assertEquals(3, first.getInt("total"))
        val second = magerun("commands", JSONObject().put("offset", first.getInt("nextOffset")).put("limit", 1))
        assertEquals("shop:custom", second.getJSONArray("commands").getJSONObject(0).getString("name"))
        val custom = magerun("commands", JSONObject().put("search", "custom"))
        assertTrue(custom.getBoolean("cached"))
        assertEquals(1, custom.getInt("total"))
        assertEquals("shop:custom", custom.getJSONArray("commands").getJSONObject(0).getString("name"))
        assertEquals(2, calls("bin").size)
        val help = magerun("command_help", JSONObject().put("command", "si"))
        assertEquals("sys:info", help.getJSONObject("command").getString("name"))
        assertTrue(help.getJSONObject("command").getJSONObject("definition").has("options"))
        magerun("command_help", JSONObject().put("command", "sys:info"))
        assertEquals(3, calls("bin").size)
        val configuration = magerun("mcp_config")
        assertFalse(configuration.getBoolean("serverStarted"))
        assertFalse(configuration.getBoolean("configurationWritten"))
        val server = configuration.getJSONObject("configuration").getJSONObject("mcpServers").getJSONObject("magento-magerun2")
        assertEquals(root.toString(), server.getString("cwd"))
        assertTrue(server.getJSONArray("args").toList().contains("--include=sys:info"))
        assertEquals(4, calls("bin").size)
        val cached = magerun()
        assertEquals("cached", cached.getString("status"))
        assertFalse(cached.getBoolean("runtimeProbed"))
        assertEquals(4, calls("bin").size)
        assertEquals("detected", magerun("status", JSONObject().put("refresh", true)).getString("status"))
        assertEquals(4, calls("bin").size)
        magerun("commands")
        assertEquals(6, calls("bin").size)
        file("composer.lock", "{\"packages\":[]}")
        magerun("commands")
        assertEquals(8, calls("bin").size)
        assertTrue(calls("src/bin").isEmpty())
        for (call in calls("bin")) {
            assertEquals(root.toString(), call.getString("cwd"))
            assertEquals("1", call.getString("noColor"))
            val args = call.getJSONArray("arguments").toList()
            assertTrue(args.contains("--root-dir=${root.resolve("src")}"))
            assertTrue(args.contains("--no-interaction"))
            assertTrue(args.contains("--no-ansi"))
            assertTrue(args.contains("--version") || args.contains("list") || args.contains("help"))
        }
        assertFalse(Files.exists(root.resolve("bin/application-command-executed")))
    }

    fun testProbeFailureNeverUsesMagentoFallback() {
        file("compose.yaml", "services: {}")
        installMagerun("bin/n98-magerun2.phar")
        installMagerun("src/bin/n98-magerun2.phar")
        file("bin/fail-probe", "fail")
        val result = magerun("commands")
        assertEquals(result.toString(), "probe_failed", result.getString("status"))
        assertEquals(1, calls("bin").size)
        assertTrue(calls("src/bin").isEmpty())
        assertEquals("detected", magerun().getString("status"))
    }

    fun testExecutablePhpWithoutShebangStillUsesPhpInterpreter() {
        installMagerun("src/bin/n98-magerun2.phar")
        check(root.resolve("src/bin/n98-magerun2.phar").toFile().setExecutable(true))
        val result = magerun("commands")
        assertEquals(result.toString(), "ok", result.getString("status"))
        assertEquals(2, calls("src/bin").size)
    }

    fun testMarkShustAdapterUsesClinottyAndNeverBootstrapsOrFallsBack() {
        org.junit.Assume.assumeFalse("POSIX shell wrapper fixture", com.intellij.openapi.util.SystemInfoRt.isWindows)
        file("compose.yaml", "services: {}")
        file("bin/n98-magerun2", javaClass.getResource("/magerun/mark-shust-wrapper")!!.readText(), executable = true)
        file("bin/clinotty", "#!/bin/sh\nprintf '%s\\n' \"\$*\" >> bin/launcher-calls.txt\nexec \"\$@\"\n", executable = true)
        // Any accidental execution of the download wrapper fails locally, without network access.
        file("bin/cliq", "#!/bin/sh\ntouch bootstrap-executed\nexit 0\n", executable = true)
        installMagerun("bin/n98-magerun2.phar")
        installMagerun("src/bin/n98-magerun2.phar")
        val result = magerun("commands")
        assertEquals(result.toString(), "ok", result.getString("status"))
        assertEquals(2, calls("bin").size)
        for (call in calls("bin")) {
            assertEquals(root.toString(), call.getString("cwd"))
            assertFalse(call.getJSONArray("arguments").toList().any { it.toString().startsWith("--root-dir=") })
        }
        val launcherCalls = Files.readAllLines(root.resolve("bin/launcher-calls.txt"))
        assertEquals("test -f bin/n98-magerun2.phar", launcherCalls.first())
        assertTrue(launcherCalls.drop(1).all { it.startsWith("php bin/n98-magerun2.phar ") })
        Files.delete(root.resolve("bin/n98-magerun2.phar"))
        assertEquals("missing_installation", magerun("commands", JSONObject().put("refresh", true)).getString("status"))
        assertEquals(4, Files.readAllLines(root.resolve("bin/launcher-calls.txt")).size)
        assertTrue(calls("src/bin").isEmpty())
        assertFalse(Files.exists(root.resolve("bootstrap-executed")))
    }

    fun testRejectsInvalidInputsBeforeLaunchingPhp() {
        installMagerun("src/bin/n98-magerun2.phar")
        for (command in listOf("sys:info; touch bad", "--version", "db:query SELECT 1")) {
            assertEquals("invalid_request", magerun("command_help", JSONObject().put("command", command)).getString("status"))
        }
        assertEquals("invalid_request", magerun("commands", JSONObject().put("limit", 0)).getString("status"))
        val typeError = mcp().callEnvelope("magento_magerun", JSONObject().put("mode", "commands").put("limit", "invalid"))
        // The host rejects a typed argument before entering the plugin: JSON-RPC error, not tool content.
        assertFalse(typeError.has("result"))
        assertTrue(typeError.getJSONObject("error").getInt("code") < 0)
        assertTrue(typeError.getJSONObject("error").getString("message").contains("invalid"))
        assertTrue(calls("src/bin").isEmpty())
    }

    fun testUnsupportedWrapperIsReportedWithoutExecution() {
        file("src/bin/n98-magerun2", "#!/bin/sh\ntouch application-command-executed\n", executable = true)
        val result = magerun("commands")
        assertEquals(result.toString(), "unsupported_wrapper", result.getString("status"))
        assertFalse(Files.exists(root.resolve("src/application-command-executed")))
    }

    fun testConfigureMagentoProjectOverHttp() {
        val originalPath = Settings.getInstance(project).magentoPath
        val originalEnabled = Settings.getInstance(project).pluginEnabled
        try {
            // Start with integration disabled to verify configuration from scratch
            Settings.getInstance(project).pluginEnabled = false
            Settings.getInstance(project).magentoPath = ""

            // Status reflects disabled
            val initialStatus = text("configure_magento_project", JSONObject().put("action", "status"))
            assertTrue(initialStatus, initialStatus.contains("Magento Project Integration Status"))
            assertTrue(initialStatus, initialStatus.contains("Integration enabled: no"))

            // Help mode
            val help = text("configure_magento_project", JSONObject().put("action", "help"))
            assertTrue(help, help.contains("Magento Project Configuration Tool"))
            assertTrue(help, help.contains("action=status"))
            assertTrue(help, help.contains("action=configure"))

            // Invalid path rejected without changing settings
            val invalidResult = text("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", "nonexistent_dir")
                .put("enabled", true)
            )
            assertTrue(invalidResult, invalidResult.contains("Result: error"))
            assertTrue(invalidResult, invalidResult.contains("Path does not exist"))
            assertFalse(Settings.getInstance(project).pluginEnabled)

            // Unsupported action rejected without changing settings
            val unsupported = text("configure_magento_project", JSONObject()
                .put("action", "preview")
                .put("enabled", false)
            )
            assertTrue(unsupported, unsupported.contains("Result: error"))
            assertTrue(unsupported, unsupported.contains("Unsupported action 'preview'"))

            // Project root configuration from disabled state
            file("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
            file("composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8"}]}""")
            file("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")
            file("vendor/autoload.php", "<?php\n")
            val configureRoot = text("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", ".")
                .put("enabled", true)
                .put("excludeGeneratedFiles", true)
            )
            assertTrue(configureRoot, configureRoot.contains("Result: success"))
            assertTrue(configureRoot, configureRoot.contains("Integration enabled: yes"))
            assertTrue(configureRoot, configureRoot.contains("Indexing exclusions: enabled"))
            assertEquals("Configured Magento root path: ${root.toString().replace('\\', '/')}", text("get_magento_root_path"))

            // Idempotent repeat
            val repeatRoot = text("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", ".")
                .put("enabled", true)
                .put("excludeGeneratedFiles", true)
            )
            assertTrue(repeatRoot, repeatRoot.contains("no changes required"))
        } finally {
            Settings.getInstance(project).magentoPath = originalPath
            Settings.getInstance(project).pluginEnabled = originalEnabled
        }
    }

    fun testControlledConfigurationDuringIndexingOverHttp() {
        val originalPath = Settings.getInstance(project).magentoPath
        val originalEnabled = Settings.getInstance(project).pluginEnabled
        val originalExclusions = Settings.getInstance(project).excludeGeneratedFiles
        try {
            Settings.getInstance(project).pluginEnabled = false
            Settings.getInstance(project).magentoPath = null

            file("src/composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
            file("src/composer.lock", """{"packages":[{"name":"magento/product-community-edition","version":"2.4.8"}]}""")
            file("src/vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")
            file("src/vendor/autoload.php", "<?php\n")
            myFixture.addFileToProject("src/app/code/HttpIndex/Module/etc/module.xml", """
                <config><module name="HttpIndex_Module"/></config>
            """.trimIndent())

            DumbModeTestUtils.runInDumbModeSynchronously(project) {
                val configured = text("configure_magento_project", JSONObject()
                    .put("action", "configure")
                    .put("magentoPath", "src")
                    .put("enabled", true)
                    .put("excludeGeneratedFiles", true)
                )
                assertTrue(configured, configured.contains("Result: success"))
                assertTrue(configured, configured.contains("Integration enabled: yes"))
                assertTrue(configured, configured.contains("Pending index refresh: yes"))

                val status = text("configure_magento_project", JSONObject().put("action", "status"))
                assertTrue(status, status.contains("Pending index refresh: yes"))
                assertTrue(status, status.contains("Wait for IDE indexing to finish"))
            }

            IndexingTestUtil.waitUntilIndexesAreReady(project)

            val moduleResult = text("magento_inspect", JSONObject()
                .put("mode", "query")
                .put("queryType", "module")
                .put("parametersJson", """{"moduleName":"HttpIndex_Module"}""")
            )
            assertTrue(moduleResult, moduleResult.replace('\\', '/').contains("src/app/code/HttpIndex/Module"))
        } finally {
            Settings.getInstance(project).magentoPath = originalPath
            Settings.getInstance(project).pluginEnabled = originalEnabled
            Settings.getInstance(project).setExcludeGeneratedFiles(originalExclusions)
        }
    }

    private fun text(tool: String, arguments: JSONObject = JSONObject()): String {
        val result = mcp().call(tool, arguments)
        assertFalse(result.toString(), result.optBoolean("isError"))
        return McpHttpClient.text(result)
    }

    private fun magerun(mode: String? = null, arguments: JSONObject = JSONObject()): JSONObject {
        mode?.let { arguments.put("mode", it) }
        return JSONObject(text("magento_magerun", arguments))
    }

    private fun installMagerun(path: String) = file(path, javaClass.getResource("/magerun/http-fixture.php")!!.readText())

    private fun calls(directory: String): List<JSONObject> = root.resolve("$directory/calls.jsonl").let { path ->
        if (Files.exists(path)) Files.readAllLines(path).map(::JSONObject) else emptyList()
    }

    private fun file(path: String, content: String, executable: Boolean = false) {
        val target = root.resolve(path)
        Files.createDirectories(target.parent)
        Files.writeString(target, content)
        if (executable) check(target.toFile().setExecutable(true))
    }
}
