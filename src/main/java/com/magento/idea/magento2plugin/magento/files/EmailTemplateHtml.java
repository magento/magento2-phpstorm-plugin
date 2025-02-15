/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;

public class EmailTemplateHtml implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Module Email Template Html";
    public static final String HTML_TYPE = "html";
    public static final String TEXT_TYPE = "text";
    public static final String HTML_FILE_EXTENSION = "html";
    private static EmailTemplateHtml instance;
    private String fileName;

    /**
     * Returns a new instance of the class.
     *
     * @return EmailTemplatesXml
     */
    public static EmailTemplateHtml getInstance(final String fileName) {
        if (null == instance) { //NOPMD
            instance = new EmailTemplateHtml();
        }

        instance.setFileName(fileName);

        return instance;
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", this.fileName, HTML_FILE_EXTENSION);
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return HTMLLanguage.INSTANCE;
    }

    private void setFileName(final String fileName) {
        this.fileName = fileName;
    }
}
