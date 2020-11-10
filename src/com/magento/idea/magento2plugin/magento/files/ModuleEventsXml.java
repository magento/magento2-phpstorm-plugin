/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleEventsXml implements ModuleFileInterface {
    public static String FILE_NAME = "events.xml";
    public static String TEMPLATE = "Magento Events XML";

    //code templates
    public static String TEMPLATE_OBSERVER = "Magento Module Events Xml Observer";

    //tags
    public static String OBSERVER_TAG = "observer";
    public static String INSTANCE_ATTRIBUTE = "instance";
    public static String EVENT_TAG = "event";

    private static ModuleEventsXml INSTANCE = null;

    /**
     * Getter for singleton instance of class.
     */
    public static ModuleEventsXml getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ModuleEventsXml();
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
        return XMLLanguage.INSTANCE;
    }
}
