/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.indexes.data.DeprecationStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.data.VersionStateIndex;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class VersionStateManager {

    private final Project project;
    private final UctSettingsService settingsService;

    private static VersionStateManager INSTANCE;
    private final DeprecationStateIndex deprecationStateIndex;

    /**
     * Get instance of the version state manager.
     *
     * @param project Project
     *
     * @return VersionStateManager
     */
    public static VersionStateManager getInstance(final @NotNull Project project) {
        if (INSTANCE == null) {
            INSTANCE = new VersionStateManager(project);
        }
        return INSTANCE;
    }

    /**
     * Version state manager constructor.
     */
    private VersionStateManager(final @NotNull Project project) {
        this.project = project;
        settingsService = UctSettingsService.getInstance(project);
        deprecationStateIndex = new DeprecationStateIndex();
        compute(deprecationStateIndex);
    }

    /**
     * Check if specified FQN exists in the deprecation index.
     *
     * @param fqn String
     *
     * @return boolean
     */
    public boolean isDeprecated(final @NotNull String fqn) {
        return deprecationStateIndex.has(fqn);
    }

    /**
     * Compute index data.
     *
     * @param index VersionStateIndex
     */
    private void compute(final VersionStateIndex index) {
        final SupportedVersion currentVersion = settingsService.getCurrentVersion();
        final SupportedVersion targetVersion = settingsService.getTargetVersion();
        final boolean hasIgnoringFlag = settingsService.shouldIgnoreCurrentVersion();

        if (targetVersion == null) {
            return;
        }
        final List<SupportedVersion> versionsToLoad = new LinkedList<>();

        for (final SupportedVersion version : SupportedVersion.values()) {
            if (version.compareTo(targetVersion) <= 0) {
                if (hasIgnoringFlag) {
                    // If current version is NULL, it is less than minimum supported version.
                    if (currentVersion == null || version.compareTo(currentVersion) > 0) {
                        versionsToLoad.add(version);
                    }
                } else {
                    versionsToLoad.add(version);
                }
            }
        }
        index.load(versionsToLoad);
    }
}
