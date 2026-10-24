/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.JUnit38AssumeSupportRunner
import com.magento.idea.magento2plugin.project.Settings
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
        )
        for ((name, fragments) in descriptions) {
            val description = tools.getValue(name).getString("description")
            for (fragment in fragments) assertTrue("$name: $description", description.contains(fragment))
        }
        assertEquals("Configured Magento root path: ${root.resolve("src").toString().replace('\\', '/')}", text("get_magento_root_path"))
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

    private fun inspect(mode: String, queryType: String = "", parametersJson: String = ""): String =
        text("magento_inspect", JSONObject().put("mode", mode).put("queryType", queryType)
            .put("parametersJson", parametersJson))

    fun testInspectionReportsIndexingAndUnavailableCapabilityOverHttp() {
        ExtensionTestUtil.maskExtensions(MagentoMethodPluginQueryProvider.EP_NAME, emptyList(), testRootDisposable)
        mcp()
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            for (type in listOf("module", "di_config", "observers_for_event", "layout_entities",
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
