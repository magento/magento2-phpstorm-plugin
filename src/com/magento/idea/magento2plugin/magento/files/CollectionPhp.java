/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

public class CollectionPhp extends AbstractPhpClass {
    public static final String ABSTRACT_COLLECTION
            = "Magento\\Framework\\Model\\ResourceModel\\Db\\Collection\\AbstractCollection";

    public CollectionPhp(final String className) {
        super(className);
    }

    @Override
    public String getTemplate() {
        return "Magento Collection Class";
    }
}
