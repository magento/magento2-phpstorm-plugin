/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class PreferenceFileData {
    private final String preferenceDirectory;
    private final String preferenceClassName;
    private final String preferenceModule;
    private final PhpClass targetClass;
    private final String preferenceFqn;
    private final String namespace;
    private final boolean inheritClass;
    private final boolean isAnInterface;

    /**
     * Constructor.
     */
    public PreferenceFileData(
            final String preferenceDirectory,
            final String preferenceClassName,
            final String preferenceModule,
            final PhpClass targetClass,
            final String preferenceFqn,
            final String namespace,
            final boolean inheritClass,
            final boolean isAnInterface
    ) {
        this.preferenceDirectory = preferenceDirectory;
        this.preferenceClassName = preferenceClassName;
        this.preferenceModule = preferenceModule;
        this.targetClass = targetClass;
        this.preferenceFqn = preferenceFqn;
        this.namespace = namespace;
        this.inheritClass = inheritClass;
        this.isAnInterface = isAnInterface;
    }

    public String getPreferenceClassName() {
        return preferenceClassName;
    }

    public String getPreferenceDirectory() {
        return preferenceDirectory;
    }

    public String getPreferenceModule() {
        return preferenceModule;
    }

    public PhpClass getTargetClass() {
        return targetClass;
    }

    public String getPreferenceFqn() {
        return preferenceFqn;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isInheritClass() {
        return inheritClass;
    }

    public boolean isInterface() {
        return isAnInterface;
    }
}
