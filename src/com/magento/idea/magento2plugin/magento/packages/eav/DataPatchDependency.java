/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum DataPatchDependency {
    ENV_SETUP("Magento\\Eav\\Setup\\EavSetup"),
    EAV_SETUP_FACTORY("Magento\\Eav\\Setup\\EavSetupFactory"),
    MODULE_DATA_SETUP_INTERFACE("Magento\\Framework\\Setup\\ModuleDataSetupInterface"),
    DATA_PATCH_INTERFACE("Magento\\Framework\\Setup\\Patch\\DataPatchInterface");

    private String classPatch;

    DataPatchDependency(final String classPatch) {
        this.classPatch = classPatch;
    }

    public String getClassPatch() {
        return classPatch;
    }
}
