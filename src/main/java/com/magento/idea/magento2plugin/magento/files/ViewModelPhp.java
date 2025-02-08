/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class ViewModelPhp implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Regular Class";
    public static final String DEFAULT_DIR = "ViewModel";
    public static final String INTERFACE_FQN
            = "Magento\\Framework\\View\\Element\\Block\\ArgumentInterface";
    public static final String INTERFACE_NAME = "ArgumentInterface";

    private static final ViewModelPhp INSTANCE = new ViewModelPhp();
    private String fileName;

    /**
     * Getter for singleton instance of class.
     */
    public static ViewModelPhp getInstance(final String className) {
        INSTANCE.fileName = className.concat(".php");

        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return this.fileName;
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
