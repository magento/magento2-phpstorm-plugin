/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.mcp.MagentoCompatibilityToolsProvider
import java.nio.file.Files
import java.nio.file.Path

class MagentoSkillInstallerTest : PhysicalMagentoTestCase() {
    fun testInspectionTemplateInstallsCompatibilityWorkflowForEveryAgentTarget() {
        val bundled = MagentoSkillInstaller::class.java.getResourceAsStream("/skills/magento-inspect/SKILL.md")!!
            .bufferedReader().use { it.readText() }
        val toolName = MagentoCompatibilityToolsProvider().getTools().single().descriptor.name
        assertTrue(bundled.contains("`$toolName`"))
        for (target in MagentoSkillInstaller.AgentTarget.values()) {
            val skill = MagentoSkillInstaller.Skill.MAGENTO_INSPECT
            val installed = Path.of(MagentoSkillInstaller.install(project, skill, target))
            assertEquals(bundled, Files.readString(installed))
            assertFalse(MagentoSkillInstaller.hasDifferentExistingSkill(project, skill, target))
            Files.writeString(installed, "$bundled\nTeam customization\n")
            assertTrue(MagentoSkillInstaller.hasDifferentExistingSkill(project, skill, target))
            MagentoSkillInstaller.install(project, skill, target)
            assertEquals(bundled, Files.readString(installed))
        }
    }
}
