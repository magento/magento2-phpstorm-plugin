/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp;

import com.intellij.mcpserver.skills.AgentSkillTarget;
import com.intellij.mcpserver.skills.BundledSkill;
import com.intellij.mcpserver.skills.BundledSkillsProvider;
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller.Skill;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Offers the packaged skill documents to IDE-managed agent sessions. */
public final class MagentoBundledSkillsProvider implements BundledSkillsProvider {
    @Override
    public @NotNull List<BundledSkill> registeredSkills() {
        return Arrays.stream(Skill.values())
            .map(skill -> new BundledSkill(
                skill.getDirectoryName(),
                Set.of(),
                EnumSet.allOf(AgentSkillTarget.class),
                "skills/" + skill.getDirectoryName(),
                null,
                Set.of()
            ))
            .toList();
    }
}
