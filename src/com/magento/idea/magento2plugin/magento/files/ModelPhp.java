/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

public class ModelPhp extends AbstractPhpClass {
    public static final String ABSTRACT_MODEL =
            "Magento\\Framework\\Model\\AbstractModel";
    public static final String MODEL_DIRECTORY = "Model";

    public ModelPhp(final String className) {
        super(className);
    }

    @Override
    public String getTemplate() {
        return "Magento Model Class";
    }
}
