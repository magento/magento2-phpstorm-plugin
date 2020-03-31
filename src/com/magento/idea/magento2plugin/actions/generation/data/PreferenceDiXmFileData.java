/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class PreferenceDiXmFileData {
    private String preferenceModule;
    private PhpClass targetClass;
    private String preferenceFqn;
    private String namespace;
    private String area;

    public PreferenceDiXmFileData(
            String preferenceModule,
            PhpClass targetClass,
            String preferenceFqn,
            String namespace,
            String area
    ) {
        this.preferenceModule = preferenceModule;
        this.targetClass = targetClass;
        this.preferenceFqn = preferenceFqn;
        this.namespace = namespace;
        this.area = area;
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

    public String getArea() {
        return area;
    }
}
