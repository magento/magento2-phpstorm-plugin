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

    private static VersionStateManager instance;
    private final DeprecationStateIndex deprecationStateIndex;
    private final Boolean isSetIgnoreFlag;
    private final SupportedVersion currentVersion;
    private final SupportedVersion targetVersion;

    /**
     * Get instance of the version state manager.
     *
     * @param project Project
     *
     * @return VersionStateManager
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public static synchronized VersionStateManager getInstance(
            final @NotNull Project project
    ) { //NOPMD
        final UctSettingsService settingsService = UctSettingsService.getInstance(project);

        if (instance == null
                || !instance.isValidFor(settingsService.shouldIgnoreCurrentVersion(),
                settingsService.getCurrentVersion(),
                settingsService.getTargetVersion()
        )) {
            instance = new VersionStateManager(project);
        }
        return instance;
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
     * Version state manager constructor.
     */
    private VersionStateManager(final @NotNull Project project) {
        final UctSettingsService settingsService = UctSettingsService.getInstance(project);
        deprecationStateIndex = new DeprecationStateIndex();
        isSetIgnoreFlag = settingsService.shouldIgnoreCurrentVersion();
        currentVersion = settingsService.getCurrentVersion();
        targetVersion = settingsService.getTargetVersion();

        compute(deprecationStateIndex);
    }

    /**
     * Check if current instance is valid for settings.
     *
     * @param isSetIgnoreFlag boolean
     * @param currentVersion SupportedVersion
     * @param targetVersion SupportedVersion
     *
     * @return boolean
     */
    private synchronized boolean isValidFor(
            final Boolean isSetIgnoreFlag,
            final SupportedVersion currentVersion,
            final SupportedVersion targetVersion
    ) {
        return this.isSetIgnoreFlag.equals(isSetIgnoreFlag)
                && this.currentVersion.equals(currentVersion)
                && this.targetVersion.equals(targetVersion);
    }

    /**
     * Compute index data.
     *
     * @param index VersionStateIndex
     */
    private void compute(final VersionStateIndex index) {
        if (targetVersion == null) {
            return;
        }
        final List<SupportedVersion> versionsToLoad = new LinkedList<>();

        for (final SupportedVersion version : SupportedVersion.values()) {
            if (version.compareTo(targetVersion) <= 0) {
                if (isSetIgnoreFlag != null && isSetIgnoreFlag) {
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
