/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class EmailTemplatesXml implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Module Email Templates Xml";
    public static final String FILE_NAME = "email_templates.xml";

    // code templates
    public static final String EMAIL_TEMPLATE_REGISTRATION = "Magento Module Email Template Xml";

    // XML definitions
    public static final String TEMPLATE_TAG = "template";
    public static final String TEMPLATE_ID_ATTRIBUTE = "id";

    private static EmailTemplatesXml instance;

    /**
     * Returns a new instance of the class.
     *
     * @return EmailTemplatesXml
     */
    public static EmailTemplatesXml getInstance() {
        if (null == instance) { //NOPMD
            instance = new EmailTemplatesXml();
        }

        return instance;
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
        return XMLLanguage.INSTANCE;
    }
}
