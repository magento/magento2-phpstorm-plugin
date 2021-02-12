/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class CronGroupXmlTemplate implements ModuleFileInterface {
    public static final String FILE_NAME = "cron_groups.xml";
    public static final String TEMPLATE = "Magento Cron Groups XML";

    // code templates
    public static final String TEMPLATE_CRON_GROUP_REGISTRATION = "Magento Module Cron Group Xml";

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
