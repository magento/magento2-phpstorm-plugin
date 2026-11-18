/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.singleProject
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.writeText

/** Runs the production MCP query dispatcher inside WebStorm with no PHP support. */
class MagentoInspectionUiTest {
    @Test
    fun sharedConfigurationQueriesRunWithoutPhpSupport() = runWebStormUiTest(
        "file-generators",
        prepareProject = { root ->
            val files = mapOf(
                "app/code/Acme/Shipping/composer.json" to """{"name":"acme/shipping","require":{"php":">=8.2"}}""",
                "app/code/Acme/Shipping/view/frontend/web/template/shipping.html" to "<div>Shipping template</div>",
                "app/code/Acme/Shipping/view/frontend/layout/default.xml" to
                    "<page><body><referenceBlock name=\"test\" cacheable=\"false\"/></body></page>",
                "app/code/Acme/Shipping/etc/di.xml" to """
                    <config><type name="Acme\Shipping\Target"><plugin name="shipping_plugin" type="Acme\Shipping\Plugin"/></type></config>
                """.trimIndent(),
                "app/code/Acme/Shipping/etc/events.xml" to """
                    <config><event name="shipping_event"><observer name="shipping_observer" instance="Acme\Shipping\Observer" disabled="true"/></event></config>
                """.trimIndent(),
                "app/code/Acme/Shipping/view/frontend/layout/shipping_mcp.xml" to """
                    <page><body><container name="shipping_container"><block name="shipping_block" template="Acme_Shipping::example.phtml"/></container></body></page>
                """.trimIndent(),
                "app/code/Acme/Shipping/view/frontend/ui_component/shipping_listing.xml" to "<listing/>",
                "app/code/Acme/Shipping/etc/acl.xml" to """
                    <config><acl><resources><resource id="Acme_Shipping::root" title="Shipping root"/></resources></acl></config>
                """.trimIndent(),
                "app/code/Acme/Shipping/etc/adminhtml/menu.xml" to """
                    <config><menu><add id="Acme_Shipping::root" title="Shipping menu" resource="Acme_Shipping::root"/></menu></config>
                """.trimIndent()
            )
            for ((path, content) in files) {
                Files.createDirectories(root.resolve(path).parent)
                root.resolve(path).writeText(content)
            }
        }
    ) { root ->
        check(isPluginLoaded("JavaScript")) { "WebStorm should load the optional JavaScript integration" }
        check(!isPluginLoaded("com.jetbrains.php")) { "This test requires WebStorm without PHP support" }
        check(isPluginLoaded("com.intellij.mcpServer")) { "The optional MCP integration must be loaded" }
        val bridge = utility<DiagnosticsMcpSessionBridge>()
        val (port, tokenName, token) = bridge.startSession().split(":")
        try {
            McpHttpDriverClient(port.toInt(), tokenName, token, root.toString()).use { mcp ->
                mcp.initialize()
                check(mcp.tools().any { it.getString("name") == "magento_diagnostics" })
                val result = org.json.JSONObject(mcp.callText("magento_diagnostics", org.json.JSONObject()
                    .put("mode", "check").put("filePath", "app/code/Acme/Shipping/view/frontend/layout/default.xml")))
                val dependencies = org.json.JSONObject(mcp.callText("magento_inspect", org.json.JSONObject()
                    .put("mode", "query").put("queryType", "module_dependencies")
                    .put("parametersJson", """{"moduleName":"Acme_Shipping","direction":"dependencies"}""")))
                check(dependencies.getString("status") == "ok") { dependencies.toString() }
                check(dependencies.getJSONArray("edges").toString().contains("composer_require")) { dependencies.toString() }
                check(result.getString("status") == "ok") { result.toString() }
                check(result.getJSONArray("diagnostics").getJSONObject(0).getString("ruleId") == "CacheableFalseInDefaultLayoutInspection")
            }
        } finally { bridge.stopSession() }
        val project = singleProject()
        val commands = utility<InspectionCommands>()
        val help = commands.help()
        check(!help.contains("- `plugins_for_method`:")) { help }
        check(help.contains("requires the JetBrains PHP plugin")) { help }
        val cases = listOf(
            Triple("module", """{"moduleName":"Acme_Shipping"}""", "app/code/Acme/Shipping"),
            Triple("di_config", """{"className":"Acme\\Shipping\\Target"}""", "plugin name=shipping_plugin"),
            Triple("observers_for_event", """{"eventName":"shipping_event"}""", "observer=shipping_observer instance=Acme\\Shipping\\Observer disabled=true"),
            Triple("layout_entities", """{"name":"shipping_block"}""", "template=Acme_Shipping::example.phtml"),
            Triple("layout_entities", """{"name":"shipping_mcp"}""", "shipping_mcp -> app/code/Acme/Shipping/view/frontend/layout/shipping_mcp.xml"),
            Triple("layout_entities", """{"name":"shipping_container"}""", "shipping_container -> app/code/Acme/Shipping/view/frontend/layout/shipping_mcp.xml"),
            Triple("ui_template", """{"templatePath":"Acme_Shipping/shipping"}""", "file: app/code/Acme/Shipping/view/frontend/web/template/shipping.html"),
            Triple("ui_component", """{"name":"shipping_listing"}""", "rootTag: listing"),
            Triple("acl_or_menu", """{"identifier":"Acme_Shipping::root"}""", "Acme_Shipping::root -> title=Shipping root"),
            Triple("acl_or_menu", """{"identifier":"Acme_Shipping::root"}""", "Acme_Shipping::root -> title=Shipping menu resource=Acme_Shipping::root")
        )
        for ((type, parameters, expected) in cases) {
            check(help.contains("- `$type`:")) { help }
            val schema = commands.detailedSchema(type)
            check(schema.contains("queryType: $type")) { schema }
            val result = withReadAction { commands.query(project, type, parameters) }
            check(result.replace('\\', '/').contains(expected.replace('\\', '/'))) { "$type: $result" }
        }
        val unavailable = withReadAction { commands.query(project, "plugins_for_method", "{}") }
        check(unavailable.contains("unavailable") && unavailable.contains("requires the JetBrains PHP plugin")) { unavailable }
        check(commands.detailedSchema("plugins_for_method") == unavailable)
        val empty = withReadAction { commands.query(project, "ui_component", """{"name":"missing_component"}""") }
        check(empty == "No UI components matched \"missing_component\".") { empty }
    }
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoInspectionCommands", plugin = "com.magento.idea.magento2plugin")
private interface InspectionCommands {
    fun help(): String
    fun detailedSchema(queryType: String): String
    fun query(project: Project, queryType: String, parametersJson: String): String
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoMcpSessionBridge", plugin = "com.magento.idea.magento2plugin")
private interface DiagnosticsMcpSessionBridge {
    fun startSession(): String
    fun stopSession()
}
