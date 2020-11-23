/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

public class ResourceModelPhp extends AbstractPhpClass {
    public static final String RESOURCE_MODEL_DIRECTORY = "Model/ResourceModel";
    public static final String ABSTRACT_DB
            = "Magento\\Framework\\Model\\ResourceModel\\Db\\AbstractDb";

    public ResourceModelPhp(final String className) {
        super(className);
    }

    @Override
    public String getTemplate() {
        return "Magento Resource Model Class";
    }
}
