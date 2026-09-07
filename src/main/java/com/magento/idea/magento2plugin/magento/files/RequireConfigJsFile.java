/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;

public class RequireConfigJsFile implements ModuleFileInterface {

    public static final String TEMPLATE = "Require Config JS File";
    public static final String FILE_NAME = "requirejs-config.js";

    public static final String MAP_PROPERTY = "map";
    public static final String ASTERISK_PROPERTY = "*";

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
        return JavascriptLanguage.INSTANCE;
    }
}
