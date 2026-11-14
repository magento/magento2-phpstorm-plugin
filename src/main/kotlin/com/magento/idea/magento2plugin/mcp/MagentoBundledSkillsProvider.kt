/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.skills.BundledSkill
import com.intellij.mcpserver.skills.BundledSkillsProvider
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller.Skill

/** Offers the packaged skill documents to IDE-managed agent sessions. */
class MagentoBundledSkillsProvider : BundledSkillsProvider {
    override fun registeredSkills(): List<BundledSkill> = Skill.entries.map {
        BundledSkill(skillId = it.directoryName, resourcePath = "skills/${it.directoryName}")
    }
}
