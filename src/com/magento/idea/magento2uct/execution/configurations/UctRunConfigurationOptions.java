/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.configurations;

import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class UctRunConfigurationOptions extends LocatableRunConfigurationOptions {

    private final StoredProperty<String> myScriptName = string("")
            .provideDelegate(this, "scriptName");
    private final StoredProperty<String> projectRoot = string("")
            .provideDelegate(this, "projectRoot");
    private final StoredProperty<String> modulePath = string("")
            .provideDelegate(this, "modulePath");
    private final StoredProperty<String> comingVersion = string("")
            .provideDelegate(this, "comingVersion");
    private final StoredProperty<Integer> minIssueLevel = property(3)
            .provideDelegate(this, "minIssueLevel");
    private final StoredProperty<Boolean> ignoreCurrentVersionIssues = property(false)
            .provideDelegate(this, "hasIgnoreCurrentVersionIssues");
    private final StoredProperty<Boolean> newlyCreated = property(true)
            .provideDelegate(this, "isNewlyCreated");

    /**
     * Set script name setting.
     *
     * @param scriptName String
     */
    public void setScriptName(final String scriptName) {
        myScriptName.setValue(this, scriptName);
    }

    /**
     * Get script name setting.
     *
     * @return String
     */
    public String getScriptName() {
        return myScriptName.getValue(this) == null ? "" : myScriptName.getValue(this);
    }

    /**
     * Set project root setting.
     *
     * @param projectRoot String
     */
    public void setProjectRoot(final String projectRoot) {
        this.projectRoot.setValue(this, projectRoot);
    }

    /**
     * Get project root setting.
     *
     * @return String
     */
    public String getProjectRoot() {
        return projectRoot.getValue(this) == null ? "" : projectRoot.getValue(this);
    }

    /**
     * Set path to analyse setting.
     *
     * @param modulePath String
     */
    public void setModulePath(final String modulePath) {
        this.modulePath.setValue(this, modulePath);
    }

    /**
     * Get path to analyse.
     *
     * @return String
     */
    public String getModulePath() {
        return modulePath.getValue(this) == null ? "" : modulePath.getValue(this);
    }

    /**
     * Set coming version setting.
     *
     * @param comingVersion String
     */
    public void setComingVersion(final String comingVersion) {
        this.comingVersion.setValue(this, comingVersion);
    }

    /**
     * Get coming version setting.
     *
     * @return String
     */
    public String getComingVersion() {
        return comingVersion.getValue(this) == null ? "" : comingVersion.getValue(this);
    }

    /**
     * Set minimum issue severity level setting.
     *
     * @param minIssueLevel int
     */
    public void setMinIssueLevel(final int minIssueLevel) {
        this.minIssueLevel.setValue(this, minIssueLevel);
    }

    /**
     * Get minimum issue severity level setting.
     *
     * @return int
     */
    public int getMinIssueLevel() {
        return minIssueLevel.getValue(this);
    }

    /**
     * Set ignoring for current version issues setting.
     *
     * @param ignoreCurrentVersionIssues boolean
     */
    public void setIgnoreCurrentVersionIssues(final boolean ignoreCurrentVersionIssues) {
        this.ignoreCurrentVersionIssues.setValue(this, ignoreCurrentVersionIssues);
    }

    /**
     * Check if has ignoring for current version issues setting.
     *
     * @return boolean
     */
    public boolean hasIgnoreCurrentVersionIssues() {
        return ignoreCurrentVersionIssues.getValue(this);
    }

    /**
     * Set is settings is newly created.
     *
     * @param newlyCreated boolean
     */
    public void setNewlyCreated(final boolean newlyCreated) {
        this.newlyCreated.setValue(this, newlyCreated);
    }

    /**
     * Check if run configuration settings is newly created setting.
     *
     * @return boolean
     */
    public boolean isNewlyCreated() {
        return newlyCreated.getValue(this);
    }
}
