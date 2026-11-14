/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool

/** The discovery entry point also appears in the direct tool list in router mode. */
class MagentoSkillsToolset : McpToolset {
    override fun alwaysIncluded() = true

    @McpTool(name = "magento_skills")
    @McpDescription(
        "Discover bundled agent skills for Magento and Adobe Commerce. " +
        "Check skill status, get help, or install explicitly selected skills."
    )
    suspend fun magentoSkills(
        @McpDescription("help, status (default, read-only), or install.")
        mode: String = "status",
        @McpDescription("For install, explicitly select magento-inspect, magento-scaffold, and/or magento-magerun.")
        skills: List<String> = emptyList(),
        @McpDescription("Choose project (skills/, default), agents (.agents/skills/), codex (.codex/skills/), or claude (.claude/skills/).")
        agentTarget: String = "project",
        @McpDescription("Explicitly replace customized copies. Defaults to false.")
        overwrite: Boolean = false,
        @McpDescription("Optional IDE project directory to verify.")
        projectPath: String = ""
    ): String {
        if (mode == "help") return MagentoSkillCommands.help()
        return MagentoMcpToolsetSupport.withProjectEdtAction(validateProject = false) {
            MagentoSkillCommands.execute(it, mode, skills, agentTarget, overwrite, projectPath)
        }
    }
}
