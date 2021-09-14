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

    private final UctSettingsService settingsService;

    private static VersionStateManager instance;
    private final DeprecationStateIndex deprecationStateIndex;

    /**
     * Get instance of the version state manager.
     *
     * @param project Project
     *
     * @return VersionStateManager
     */
    public synchronized static VersionStateManager getInstance(final @NotNull Project project) {//NOPMD
        if (instance == null) {
            instance = new VersionStateManager(project);
        }
        return instance;
    }

    /**
     * Version state manager constructor.
     */
    private VersionStateManager(final @NotNull Project project) {
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
        final Boolean hasIgnoreFlagStoredValue = settingsService.shouldIgnoreCurrentVersion();
        boolean hasIgnoringFlag = false;

        if (hasIgnoreFlagStoredValue != null) {
            hasIgnoringFlag = hasIgnoreFlagStoredValue;
        }
        final SupportedVersion targetVersion = settingsService.getTargetVersion();

        if (targetVersion == null) {
            return;
        }
        final List<SupportedVersion> versionsToLoad = new LinkedList<>();
        final SupportedVersion currentVersion = settingsService.getCurrentVersion();

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
