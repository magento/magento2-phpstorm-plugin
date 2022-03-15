/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "Magento2UctSettings", storages = @Storage(UctSettingsService.M2_UCT_SETTINGS_XML))
public class UctSettingsService implements PersistentStateComponent<UctSettingsService> {

    public static final String M2_UCT_SETTINGS_XML = "magento2uct.xml";

    @Property
    private String uctExecutablePath;

    @Property
    private Boolean enabled;

    @Property
    private String currentVersion;

    @Property
    private String targetVersion;

    @Property
    private String modulePath;

    @Property
    private Integer minIssueSeverityLevel;

    @Property
    private Boolean ignoreCurrentVersion;

    @Property
    private Boolean hasAdditionalPath;

    @Property
    private String additionalPath;

    @SuppressWarnings("PMD.UncommentedEmptyConstructor")
    public UctSettingsService() {
    }

    /**
     * UCT run configuration independent settings storage constructor.
     *
     * @param uctExecutablePath String
     */
    public UctSettingsService(final String uctExecutablePath) {
        this.uctExecutablePath = uctExecutablePath;
    }

    /**
     * Get settings service.
     *
     * @param project Project
     *
     * @return UctSettingsService
     */
    public static UctSettingsService getInstance(final @NotNull Project project) {
        return project.getService(UctSettingsService.class);
    }

    @Override
    public @Nullable UctSettingsService getState() {
        return this;
    }

    @Override
    public void loadState(final @NotNull UctSettingsService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    /**
     * Get UCT executable path for the project (UCT Run Configuration).
     *
     * @return String
     */
    public @NotNull String getUctExecutablePath() {
        if (uctExecutablePath == null) {
            return "";
        }
        return uctExecutablePath.isEmpty() ? "" : uctExecutablePath;
    }

    /**
     * Set UCT executable path (UCT Run Configuration).
     *
     * @param uctExecutablePath String
     */
    public void setUctExecutablePath(final String uctExecutablePath) {
        this.uctExecutablePath = uctExecutablePath;
    }

    /**
     * Check if specified issue severity level is satisfiable for the specified minimum value.
     *
     * @param issueSeverityLevel IssueSeverityLevel
     *
     * @return boolean
     */
    public boolean isIssueLevelSatisfiable(final @NotNull IssueSeverityLevel issueSeverityLevel) {
        if (minIssueSeverityLevel == null) {
            return false;
        }
        final IssueSeverityLevel minimumSatisfiable = IssueSeverityLevel.getByLevel(
                minIssueSeverityLevel
        );

        return issueSeverityLevel.getLevel() <= minimumSatisfiable.getLevel();
    }

    /**
     * Set is analysis enabled value.
     *
     * @param enabled boolean
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Check if the built-in UCT feature is enabled.
     *
     * @return boolean
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return false;
        }
        return enabled;
    }

    /**
     * Set current version.
     *
     * @param version SupportedVersion
     */
    public void setCurrentVersion(final @Nullable SupportedVersion version) {
        if (version == null) {
            this.currentVersion = null;//NOPMD
        } else {
            this.currentVersion = version.getVersion();
        }
    }

    /**
     * Get current version.
     *
     * @return SupportedVersion or null if current version is less than min supported version.
     */
    public @Nullable SupportedVersion getCurrentVersion() {
        if (currentVersion == null) {
            return null;
        }
        return SupportedVersion.getVersion(currentVersion);
    }

    /**
     * Get current version or default version instead.
     *
     * @return SupportedVersion
     */
    public @NotNull SupportedVersion getCurrentVersionOrDefault() {
        final SupportedVersion currentVersion = getCurrentVersion();

        return currentVersion == null ? SupportedVersion.V230 : currentVersion;
    }

    /**
     * Set target version.
     *
     * @param version SupportedVersion
     */
    public void setTargetVersion(final @NotNull SupportedVersion version) {
        this.targetVersion = version.getVersion();
    }

    /**
     * Get target supported version.
     *
     * @return SupportedVersion
     */
    public @Nullable SupportedVersion getTargetVersion() {
        if (targetVersion == null) {
            return null;
        }
        return SupportedVersion.getVersion(targetVersion);
    }

    /**
     * Set path to analyse.
     *
     * @param modulePath String
     */
    public void setModulePath(final @NotNull String modulePath) {
        this.modulePath = modulePath;
    }

    /**
     * Get target module path (path to analyse).
     *
     * @return String
     */
    public @Nullable String getModulePath() {
        return modulePath;
    }

    /**
     * Set minimum issue severity level.
     *
     * @param minIssueSeverityLevel int
     */
    public void setMinIssueSeverityLevel(final int minIssueSeverityLevel) {
        this.minIssueSeverityLevel = minIssueSeverityLevel;
    }

    /**
     * Get minimum issue severity level.
     *
     * @return IssueSeverityLevel
     */
    public @Nullable IssueSeverityLevel getMinIssueLevel() {
        if (minIssueSeverityLevel == null) {
            return null;
        }
        return IssueSeverityLevel.getByLevel(minIssueSeverityLevel);
    }

    /**
     * Set if analysis should ignore current version compatibility problems.
     *
     * @param ignoreCurrentVersion boolean
     */
    public void setIgnoreCurrentVersion(final boolean ignoreCurrentVersion) {
        this.ignoreCurrentVersion = ignoreCurrentVersion;
    }

    /**
     * Check if analysis should ignore current version compatibility problems.
     *
     * @return boolean
     */
    public @Nullable Boolean shouldIgnoreCurrentVersion() {
        return ignoreCurrentVersion;
    }

    /**
     * Set if show additional path.
     *
     * @param hasAdditionalPath boolean
     */
    public void setHasAdditionalPath(final boolean hasAdditionalPath) {
        this.hasAdditionalPath = hasAdditionalPath;
    }

    /**
     * Check if show additional path.
     *
     * @return boolean
     */
    public @NotNull Boolean getHasAdditionalPath() {
        return hasAdditionalPath != null && hasAdditionalPath;
    }

    /**
     * Set path to analyse.
     *
     * @param additionalPath String
     */
    public void setAdditionalPath(final @NotNull String additionalPath) {
        this.additionalPath = additionalPath;
    }

    /**
     * Get target module path (path to analyse).
     *
     * @return String
     */
    public @Nullable String getAdditionalPath() {
        return additionalPath;
    }
}
