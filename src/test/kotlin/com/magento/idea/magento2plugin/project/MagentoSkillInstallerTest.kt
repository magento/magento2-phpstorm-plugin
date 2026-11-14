/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.mcp.MagentoCompatibilityToolsProvider
import java.nio.file.Files
import java.nio.file.Path
import java.io.IOException
import java.nio.file.FileAlreadyExistsException

class MagentoSkillInstallerTest : PhysicalMagentoTestCase() {
    fun testNonOverwritingInstallPreservesCustomizedFile() {
        val skill = MagentoSkillInstaller.Skill.MAGENTO_INSPECT
        val target = MagentoSkillInstaller.AgentTarget.PROJECT_SKILLS
        val path = Path.of(MagentoSkillInstaller.install(project, skill, target, false))
        Files.writeString(path, "Team instructions")
        try {
            MagentoSkillInstaller.install(project, skill, target, false)
            fail("Customized skill must not be overwritten")
        } catch (_: FileAlreadyExistsException) {
            assertEquals("Team instructions", Files.readString(path))
        }
    }

    fun testRefusesSymlinkedSkillFileAndAncestorEvenWithOverwrite() {
        // Windows hosts may not grant symlink privileges; POSIX CI exercises this boundary.
        if (com.intellij.openapi.util.SystemInfo.isWindows) return
        val root = Path.of(project.basePath!!)
        val outside = Files.createTempDirectory("magento-skills-outside")
        try {
            val sentinel = outside.resolve("SKILL.md")
            Files.writeString(sentinel, "Do not change")
            Files.createSymbolicLink(root.resolve("skills"), outside)
            assertUnsafeDestination()
            Files.delete(root.resolve("skills"))
            val skillDir = Files.createDirectories(root.resolve("skills/magento-inspect"))
            Files.createSymbolicLink(skillDir.resolve("SKILL.md"), sentinel)
            assertUnsafeDestination()
            assertEquals("Do not change", Files.readString(sentinel))
            Files.delete(skillDir.resolve("SKILL.md"))
            Files.createSymbolicLink(skillDir.resolve("SKILL.md"), outside.resolve("absent"))
            assertUnsafeDestination()
            assertFalse(Files.exists(outside.resolve("absent")))
            Files.delete(skillDir.resolve("SKILL.md"))
        } finally {
            Files.deleteIfExists(root.resolve("skills/magento-inspect/SKILL.md"))
            if (Files.isSymbolicLink(root.resolve("skills"))) Files.delete(root.resolve("skills"))
            Files.deleteIfExists(outside.resolve("SKILL.md"))
            Files.deleteIfExists(outside)
        }
    }

    private fun assertUnsafeDestination() {
        try {
            MagentoSkillInstaller.install(project, MagentoSkillInstaller.Skill.MAGENTO_INSPECT,
                MagentoSkillInstaller.AgentTarget.PROJECT_SKILLS, true)
            fail("Symlinked destinations must be rejected")
        } catch (expected: IOException) {
            assertTrue(expected.message!!, expected.message!!.contains("symbolic links"))
        }
    }

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
