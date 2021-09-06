/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
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
        return ServiceManager.getService(project, UctSettingsService.class);
    }

    /**
     * Get UCT executable path for the project.
     *
     * @return String
     */
    public String getUctExecutablePath() {
        return uctExecutablePath.isEmpty() ? "" : uctExecutablePath;
    }

    /**
     * Set UCT executable path.
     *
     * @param uctExecutablePath String
     */
    public void setUctExecutablePath(final String uctExecutablePath) {
        this.uctExecutablePath = uctExecutablePath;
    }

    @Override
    public @Nullable UctSettingsService getState() {
        return this;
    }

    @Override
    public void loadState(final @NotNull UctSettingsService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
