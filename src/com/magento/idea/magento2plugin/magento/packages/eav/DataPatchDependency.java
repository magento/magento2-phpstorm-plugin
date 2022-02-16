/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum DataPatchDependency {
    CUSTOMER_METADATA_INTERFACE("Magento\\Customer\\Api\\CustomerMetadataInterface"),
    DATA_PATCH_INTERFACE("Magento\\Framework\\Setup\\Patch\\DataPatchInterface"),
    EAV_CONFIG("Magento\\Eav\\Model\\Config"),
    EAV_SETUP_FACTORY("Magento\\Eav\\Setup\\EavSetupFactory"),
    ENV_SETUP("Magento\\Eav\\Setup\\EavSetup"),
    MODULE_DATA_SETUP_INTERFACE("Magento\\Framework\\Setup\\ModuleDataSetupInterface"),
    ATTRIBUTE_RESOURCE("Magento\\Customer\\Model\\ResourceModel\\Attribute");

    private String classPatch;

    DataPatchDependency(final String classPatch) {
        this.classPatch = classPatch;
    }

    public String getClassPatch() {
        return classPatch;
    }
}
