/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class QueueCommunicationXml implements ModuleFileInterface {
    public static String fileName = "communication.xml";
    public static String template = "Magento Queue Communication XML";

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public Language getLanguage() {
        return XMLLanguage.INSTANCE;
    }
}
