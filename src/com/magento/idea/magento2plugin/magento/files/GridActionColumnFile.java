/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public final class GridActionColumnFile implements ModuleFileInterface {
    public static final String CLASS_NAME = "BlockActions";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Grid Ui Component Action Column Class";
    public static final String DIRECTORY = "UI/Component/Listing/Column";
    public static final String PARENT_CLASS = "Magento\\Ui\\Component\\Listing\\Columns\\Column";
    public static final String CONTEXT =
            "Magento\\Framework\\View\\Element\\UiComponent\\ContextInterface";
    public static final String UI_COMPONENT_FACTORY =
            "Magento\\Framework\\View\\Element\\UiComponentFactory";

    /**
     * Get Directory path from the module root.
     *
     * @return String
     */
    public String getDirectory() {
        return DIRECTORY;
    }

    @Override
    public String getFileName() {
        return CLASS_NAME.concat("." + FILE_EXTENSION);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
