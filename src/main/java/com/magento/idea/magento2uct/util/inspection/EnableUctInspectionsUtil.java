/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.inspection;

import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolRegistrar;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import org.jetbrains.annotations.NotNull;

public final class EnableUctInspectionsUtil {

    private static final String GROUP = "UCT";

    private EnableUctInspectionsUtil() {
    }

    /**
     * Enable all UCT inspections in the current project inspection profile.
     * A shared IDE-level profile is copied to the project first, so other projects keep their settings.
     *
     * @param project Project
     */
    public static void execute(final @NotNull Project project) {
        final ProjectInspectionProfileManager manager = ProjectInspectionProfileManager.getInstance(project);
        InspectionProfileImpl profile = manager.getCurrentProfile();

        if (!profile.isProjectLevel()) {
            final InspectionProfileImpl copy = new InspectionProfileImpl(
                    profile.getName(),
                    InspectionToolRegistrar.getInstance(),
                    manager
            );
            copy.copyFrom(profile);
            copy.setProjectLevel(true);
            manager.addProfile(copy);
            manager.setRootProfile(copy.getName());
            profile = copy;
        }
        execute(profile, project);
        manager.fireProfileChanged(profile);
    }

    /**
     * Enable all UCT inspections in the given inspection profile.
     *
     * @param profile InspectionProfileImpl
     * @param project Project
     */
    public static void execute(
            final @NotNull InspectionProfileImpl profile,
            final @NotNull Project project
    ) {
        for (final InspectionToolWrapper<?, ?> tool : profile.getInspectionTools(null)) {
            final String[] groupPath = tool.getGroupPath();

            if (groupPath.length > 0 && GROUP.equals(groupPath[0])) {
                profile.setToolEnabled(tool.getShortName(), true, project);
            }
        }
    }
}
