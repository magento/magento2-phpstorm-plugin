/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.intellij.driver.client.utility
import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.isPluginLoaded
import com.magento.idea.magento2plugin.mcp.McpHttpDriverClient
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import java.nio.file.Files

class MagentoSkillsUiTest {
    @Test
    fun installSkillsOverHttpWithoutPhpOrEnabledIntegration() = runWebStormUiTest(
        "file-generators", enableSupport = false,
        prepareProject = { root -> Files.writeString(root.resolve("AGENTS.md"), "# Existing team rules\n") }
    ) { root ->
        check(!isPluginLoaded("com.jetbrains.php"))
        val bridge = utility<RemoteSkillMcpSessionBridge>()
        val (port, tokenName, token) = bridge.startSession().split(":")
        try {
            McpHttpDriverClient(port.toInt(), tokenName, token, root.toString()).use { mcp ->
                mcp.initialize()
                val status = mcp.callText("magento_skills", JSONObject())
                check(status.contains("magento-inspect: missing")) { status }
                val args = JSONObject().put("mode", "install")
                    .put("skills", JSONArray(listOf("magento-inspect")))
                    .put("agentTarget", "agents").put("projectPath", root.toString())
                val installed = mcp.callText("magento_skills", args)
                check(installed.contains("magento-inspect: installed")) { installed }
                check(installed.contains("[magento-inspect](.agents/skills/magento-inspect/SKILL.md)")) { installed }
                check(Files.readString(root.resolve(".agents/skills/magento-inspect/SKILL.md")).contains("magento_inspect"))
                check(mcp.callText("magento_skills", args).contains("magento-inspect: unchanged"))
                check(Files.readString(root.resolve("AGENTS.md")) == "# Existing team rules\n")
                check(mcp.callText("configure_magento_project", JSONObject().put("action", "status"))
                    .contains("Integration enabled: no"))
            }
        } finally {
            bridge.stopSession()
        }
    }
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoMcpSessionBridge", plugin = "com.magento.idea.magento2plugin")
private interface RemoteSkillMcpSessionBridge {
    fun startSession(): String
    fun stopSession()
}
