/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;

public class ComposerJson implements ModuleFileInterface {
    public static String FILE_NAME = "composer.json";
    public static String TEMPLATE = "Magento Module Composer";
    public static String DEFAULT_DEPENDENCY = "\"magento/framework\": \"*\"";
    public static String NO_DEPENDENCY_LABEL = "None";
    private static ComposerJson INSTANCE = null;

    public static ComposerJson getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ComposerJson();
        }
        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return JsonLanguage.INSTANCE;
    }
}
