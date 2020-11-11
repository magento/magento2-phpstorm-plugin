/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;

public class ComposerJson implements ModuleFileInterface {
    public static final String FILE_NAME = "composer.json";
    public static final String TEMPLATE = "Magento Composer JSON";
    public static final String DEFAULT_DEPENDENCY = "\"magento/framework\": \"*\"";
    public static final String NO_DEPENDENCY_LABEL = "None";
    private static final ComposerJson INSTANCE = new ComposerJson();

    /**
     * Getter for singleton instance of class.
     */
    public static ComposerJson getInstance() {
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
