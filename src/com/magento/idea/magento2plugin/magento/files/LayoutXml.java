/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.ClassNamingConventions"})
public class LayoutXml implements ModuleFileInterface {
    public static String DEFAULT_FILENAME = "default.xml";
    public static String CACHEABLE_ATTRIBUTE_NAME = "cacheable";
    public static String CACHEABLE_ATTRIBUTE_VALUE_FALSE = "false";
    public static String BLOCK_ATTRIBUTE_TAG_NAME = "block";
    public static String REFERENCE_BLOCK_ATTRIBUTE_TAG_NAME = "referenceBlock";
    public static String XML_ATTRIBUTE_TEMPLATE = "template";
    public static String ARGUMENTS_TEMPLATE = "Magento Module Class Arguments In Xml";
    public static String PARENT_DIR = "layout";

    public static String TEMPLATE = "Magento Module Layout Xml";
    private String fileName;

    public LayoutXml(final String routeId, final String controllerName, final String actionName) {
        this.setFileName(
                routeId
                + "_"
                + controllerName.toLowerCase()
                + "_"
                + actionName.toLowerCase()
                + ".xml"
        );
    }

    /**
     * Get name of file.
     *
     * @return String
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Get name of file template.
     *
     * @return String
     */
    public String getTemplate() {
        return TEMPLATE;
    }

    /**
     * Get language.
     *
     * @return Language
     */
    public Language getLanguage() {
        return XMLLanguage.INSTANCE;
    }

    /**
     * Set name of layout file.
     *
     * @param filename String.
     */
    private void setFileName(final String filename) {
        this.fileName = filename;
    }
}
