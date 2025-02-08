/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public class FormButtonBlockFile extends AbstractPhpFile {

    public static final String TEMPLATE = "Magento Form Button Block Class";
    public static final String HUMAN_READABLE_NAME = "Form button block class";
    public static final String DATA_PROVIDER_TYPE =
            "Magento\\Framework\\View\\Element\\UiComponent\\Control\\ButtonProviderInterface";
    public static final String TYPE_SAVE = "Save";
    public static final String TYPE_DELETE = "Delete";
    public static final String TYPE_BACK = "Back";
    public static final String TYPE_CUSTOM = "Custom";
    private final String directoryName;

    /**
     * Form button block file constructor.
     *
     * @param moduleName String
     * @param className String
     */
    public FormButtonBlockFile(
            final @NotNull String moduleName,
            final @NotNull String className,
            final @NotNull String directoryName
    ) {
        super(moduleName, className);
        this.directoryName = directoryName;
    }

    @Override
    public String getDirectory() {
        return directoryName;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
