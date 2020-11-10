/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleEventsXml implements ModuleFileInterface {
    public static final String FILE_NAME = "events.xml";
    public static final String TEMPLATE = "Magento Events XML";

    //code templates
    public static final String TEMPLATE_OBSERVER = "Magento Module Events Xml Observer";

    //tags
    public static final String OBSERVER_TAG = "observer";
    public static final String INSTANCE_ATTRIBUTE = "instance";
    public static final String EVENT_TAG = "event";

    private static final ModuleEventsXml INSTANCE = new ModuleEventsXml();

    /**
     * Getter for singleton instance of class.
     */
    public static ModuleEventsXml getInstance() {
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
