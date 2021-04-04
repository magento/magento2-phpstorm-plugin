/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import org.jetbrains.annotations.NotNull;

public final class GridActionColumnFile extends AbstractPhpFile {

    public static final String CLASS_NAME = "BlockActions";
    public static final String HUMAN_READABLE_NAME = "Grid Ui Component actions column class";
    public static final String TEMPLATE = "Magento Grid Ui Component Action Column Class";
    public static final String DIRECTORY = "Ui/Component/Listing/Column";
    public static final String PARENT_CLASS = "Magento\\Ui\\Component\\Listing\\Columns\\Column";
    public static final String CONTEXT =
            "Magento\\Framework\\View\\Element\\UiComponent\\ContextInterface";
    public static final String UI_COMPONENT_FACTORY =
            "Magento\\Framework\\View\\Element\\UiComponentFactory";

    public GridActionColumnFile(
            final @NotNull String moduleName
    ) {
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
