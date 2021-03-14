/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import org.jetbrains.annotations.NotNull;

public class PreferenceDiXmFileData {
    private final String preferenceModule;
    private final String targetClass;
    private final String preferenceFqn;
    private final String namespace;
    private final String area;

    /**
     * Preference DI XML file data.
     *
     * @param preferenceModule String
     * @param targetClass String
     * @param preferenceFqn String
     * @param namespace String
     * @param area String
     */
    public PreferenceDiXmFileData(
            final @NotNull String preferenceModule,
            final @NotNull String targetClass,
            final @NotNull String preferenceFqn,
            final @NotNull String namespace,
            final @NotNull String area
    ) {
        this.preferenceModule = preferenceModule;
        this.targetClass = targetClass;
        this.preferenceFqn = preferenceFqn;
        this.namespace = namespace;
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
    public String getTargetClass() {
        return targetClass;
    }

    /**
     * Get preference FQN.
     *
     * @return String
     */
    public String getPreferenceFqn() {
        return preferenceFqn;
    }

    /**
     * Get namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
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
