/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class PreferenceFileData {
    private String preferenceDirectory;
    private String preferenceClassName;
    private String preferenceModule;
    private PhpClass targetClass;
    private String preferenceFqn;
    private String namespace;
    private boolean inheritClass;
    private boolean isInterface;

    public PreferenceFileData(
            String preferenceDirectory,
            String preferenceClassName,
            String preferenceModule,
            PhpClass targetClass,
            String preferenceFqn,
            String namespace,
            boolean inheritClass,
            boolean isInterface
    ) {
        this.preferenceDirectory = preferenceDirectory;
        this.preferenceClassName = preferenceClassName;
        this.preferenceModule = preferenceModule;
        this.targetClass = targetClass;
        this.preferenceFqn = preferenceFqn;
        this.namespace = namespace;
        this.inheritClass = inheritClass;
        this.isInterface = isInterface;
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
        return isInterface;
    }
}
