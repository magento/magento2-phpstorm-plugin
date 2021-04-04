/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public final class FormGenericButtonBlockFile extends AbstractPhpFile {

    public static final String CLASS_NAME = "GenericButton";
    public static final String HUMAN_READABLE_NAME = "Generic button block class";
    public static final String TEMPLATE = "Magento PHP Form Generic Button Block Class";
    public static final String DIRECTORY = "Block/Form";
    public static final String CONTEXT = "Magento\\Backend\\Block\\Widget\\Context";

    /**
     * Form generic button block file constructor.
     *
     * @param moduleName String
     */
    public FormGenericButtonBlockFile(final @NotNull String moduleName) {
        super(moduleName, CLASS_NAME);
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getDirectory() {
        return DIRECTORY;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
