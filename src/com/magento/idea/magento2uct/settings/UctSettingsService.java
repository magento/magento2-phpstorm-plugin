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
    public @Nullable static UctSettingsService getInstance(final @NotNull Project project) {
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
     * Get current version.
     *
     * @return SupportedVersion or null if current version is less than min supported version.
     */
    public SupportedVersion getCurrentVersion() {
        return SupportedVersion.V230;
    }

    /**
     * Get target supported version.
     *
     * @return SupportedVersion
     */
    public SupportedVersion getTargetVersion() {
        return SupportedVersion.V231;
    }

    /**
     * Get target module path (path to analyse).
     *
     * @return String
     */
    public String getModulePath() {
        return "test";
    }

    /**
     * Get minimum issue severity level.
     *
     * @return IssueSeverityLevel
     */
    public IssueSeverityLevel getMinIssueLevel() {
        return IssueSeverityLevel.WARNING;
    }

    /**
     * Check if analysis should ignore current version compatibility problems.
     *
     * @return boolean
     */
    public boolean shouldIgnoreCurrentVersion() {
        return false;
    }
}
