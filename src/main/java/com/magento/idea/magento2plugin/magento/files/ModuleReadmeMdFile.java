/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;

public class ModuleReadmeMdFile implements ModuleFileInterface {

    public static final String FILE_NAME = "README.md";
    public static final String TEMPLATE = "Magento Module Readme File MD";

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
        return HTMLLanguage.INSTANCE;
    }
}
