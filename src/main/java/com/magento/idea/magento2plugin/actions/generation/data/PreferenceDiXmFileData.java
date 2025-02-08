/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class PreferenceDiXmFileData {

    private final String preferenceModule;
    private final String preferenceFor;
    private final String preferenceType;
    private final String area;

    /**
     * Preference DI XML file data.
     *
     * @param preferenceModule String
     * @param preferenceFor String
     * @param preferenceType String
     * @param area String
     */
    public PreferenceDiXmFileData(
            final @NotNull String preferenceModule,
            final @NotNull String preferenceFor,
            final @NotNull String preferenceType,
            final @NotNull String area
    ) {
        this.preferenceModule = preferenceModule;
        this.preferenceFor = preferenceFor;
        this.preferenceType = preferenceType;
        this.area = area;
    }

    /**
     * Get preference module.
     *
     * @return String
     */
    public String getPreferenceModule() {
        return preferenceModule;
    }

    /**
     * Get target class.
     *
     * @return String
     */
    public String getPreferenceFor() {
        return preferenceFor;
    }

    /**
     * Get preference FQN.
     *
     * @return String
     */
    public String getPreferenceType() {
        return preferenceType;
    }

    /**
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return area;
    }
}
