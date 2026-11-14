/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.testFramework.DumbModeTestUtils
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller
import com.magento.idea.magento2plugin.project.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path

class MagentoSkillSetupMcpTest : McpHttpTestCase() {
    private val root get() = Path.of(project.basePath!!)

    override fun setUp() {
        super.setUp()
        Settings.getInstance(project).pluginEnabled = false
        Settings.getInstance(project).magentoPath = null
        Settings.getInstance(project).excludeGeneratedFiles = null
    }

    fun testDiscoveryAndReadOnlyActionsNeverInstallSkills() {
        val tool = mcp().tools().single { it.getString("name") == "magento_skills" }
        val schema = tool.getJSONObject("inputSchema").getJSONObject("properties")
        assertTrue(schema.toString(), schema.getJSONObject("skills").toString().contains("array"))
        assertTrue(schema.toString(), schema.getJSONObject("skills").toString().contains("string"))
        assertTrue(schema.toString(), schema.getJSONObject("overwrite").toString().contains("boolean"))
        assertTrue(schema.toString(), schema.getJSONObject("agentTarget").toString().contains("string"))
        for (action in listOf("status", "help")) {
            val result = call(request().put("mode", action))
            assertTrue(result, result.contains(MagentoSkillCommands.WIKI_URL))
            assertFalse(Files.exists(root.resolve("skills")))
            assertFalse(Settings.getInstance(project).pluginEnabled)
        }
        call(JSONObject())
        call(JSONObject().put("skills", JSONArray()))
        assertFalse(Files.exists(root.resolve("skills")))
        assertFalse(Files.exists(root.resolve("AGENTS.md")))
    }

    fun testInstallEveryTargetDuringIndexingPreservesInstructionsAndSettings() {
        val instructions = "# Project rules\r\nKeep our custom conventions.\r\n"
        Files.writeString(root.resolve("AGENTS.md"), instructions)
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            for ((target, folder) in listOf("project" to "skills", "agents" to ".agents/skills",
                "codex" to ".codex/skills", "claude" to ".claude/skills")) {
                val args = request().put("agentTarget", target)
                    .put("skills", JSONArray(listOf("magento-inspect", "magento-scaffold")))
                val installed = call(args)
                assertTrue(installed, installed.contains("Skill installation result: success"))
                for (skill in listOf("magento-inspect", "magento-scaffold")) {
                    val relative = "$folder/$skill/SKILL.md"
                    val expected = MagentoSkillInstaller::class.java.getResourceAsStream("/skills/$skill/SKILL.md")!!
                        .bufferedReader().use { it.readText() }
                    assertEquals(expected, Files.readString(root.resolve(relative)))
                    assertTrue(installed, installed.contains("[$skill]($relative)"))
                    val timestamp = Files.getLastModifiedTime(root.resolve(relative))
                    assertTrue(call(args).contains("$skill: unchanged"))
                    assertEquals(timestamp, Files.getLastModifiedTime(root.resolve(relative)))
                }
                assertEquals(instructions, Files.readString(root.resolve("AGENTS.md")))
                assertFalse(Settings.getInstance(project).pluginEnabled)
                assertNull(Settings.getInstance(project).magentoPath)
                assertNull(Settings.getInstance(project).excludeGeneratedFiles)
            }
        }
    }

    fun testSetupSuggestsSeparateToolAndNestedMagentoStillInstallsAtIdeRoot() {
        myFixture.addFileToProject("src/composer.json", """{"name":"magento/project-community-edition"}""")
        myFixture.addFileToProject("src/app/etc/di.xml", "<config/>")
        val configured = McpHttpClient.text(mcp().call("configure_magento_project",
            JSONObject().put("magentoPath", "src").put("enabled", true)))
        assertTrue(configured, configured.contains("Result: success"))
        assertTrue(configured, configured.contains("magento_skills"))
        assertFalse(Files.exists(root.resolve("skills")))
        val installed = call(request())
        assertTrue(installed, installed.contains("magento-inspect: installed"))
        assertTrue(Files.exists(root.resolve("skills/magento-inspect/SKILL.md")))
        assertFalse(Files.exists(root.resolve("src/skills")))
        assertFalse(Files.exists(root.resolve("AGENTS.md")))
    }

    fun testCustomizedSkillsRequireExplicitReplacementBeforeAnyMutation() {
        val custom = myFixture.addFileToProject("skills/magento-scaffold/SKILL.md", "Team-specific scaffold instructions")
        val args = request().put("skills", JSONArray(listOf("magento-inspect", "magento-scaffold")))
        val conflict = call(args)
        assertTrue(conflict, conflict.contains("Customized skill preserved"))
        assertEquals("Team-specific scaffold instructions", Files.readString(Path.of(custom.virtualFile.path)))
        assertFalse(Files.exists(root.resolve("skills/magento-inspect")))
        assertNull(Settings.getInstance(project).excludeGeneratedFiles)
        val installed = call(args.put("overwrite", true))
        assertTrue(installed, installed.contains("magento-scaffold: updated"))
        assertTrue(installed, installed.contains("magento-inspect: installed"))
        assertFalse(Files.exists(root.resolve("AGENTS.md")))
    }

    fun testInvalidRequestsDoNotWriteSkillsOrSettings() {
        val invalidRequests = listOf(
            request().put("skills", JSONArray(listOf("magento-inspect", "../../outside"))),
            request().put("agentTarget", "../outside"),
            request().put("mode", "preview"),
            request().put("projectPath", root.resolve("another-project").toString()),
            request().put("skills", JSONArray())
        )
        for (args in invalidRequests) {
            val result = call(args)
            assertTrue(result, result.contains("error", ignoreCase = true) || result.contains("does not match"))
            assertFalse(Files.exists(root.resolve("skills")))
            assertFalse(Settings.getInstance(project).pluginEnabled)
            assertNull(Settings.getInstance(project).magentoPath)
        }
        val invalidType = mcp().callEnvelope("magento_skills", JSONObject().put("skills", "magento-inspect"))
        assertTrue(invalidType.toString(), invalidType.has("error") || invalidType.optJSONObject("result")?.optBoolean("isError") == true)
        assertFalse(Files.exists(root.resolve("skills")))
    }

    private fun request() = JSONObject().put("mode", "install")
        .put("skills", JSONArray(listOf("magento-inspect"))).put("projectPath", root.toString())

    private fun call(args: JSONObject): String {
        val response = mcp().call("magento_skills", args)
        return McpHttpClient.text(response)
    }
}
