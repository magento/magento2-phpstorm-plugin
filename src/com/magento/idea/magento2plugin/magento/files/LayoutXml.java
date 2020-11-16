/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import java.util.Locale;

@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.ClassNamingConventions"})
public class LayoutXml implements ModuleFileInterface {
    public static String DEFAULT_FILENAME = "default.xml";
    public static String CACHEABLE_ATTRIBUTE_NAME = "cacheable";
    public static String CACHEABLE_ATTRIBUTE_VALUE_FALSE = "false";
    public static String BLOCK_ATTRIBUTE_TAG_NAME = "block";
    public static String REFERENCE_BLOCK_ATTRIBUTE_TAG_NAME = "referenceBlock";
    public static String ROOT_TAG_NAME = "body";
    public static String REFERENCE_CONTAINER_TAG_NAME = "referenceContainer";
    public static String UI_COMPONENT_TAG_NAME = "uiComponent";
    public static String XML_ATTRIBUTE_TEMPLATE = "template";
    public static String ARGUMENTS_TEMPLATE = "Magento Module Class Arguments In Xml";
    public static String PARENT_DIR = "layout";
    public static String NAME_ATTRIBUTE = "name";
    public static String CONTENT_CONTAINER_NAME = "content";

    public static String TEMPLATE = "Magento Layout XML";
    private String fileName;

    /**
     * Layout XML file.
     *
     * @param routeId String
     * @param controllerName String
     * @param actionName String
     */
    public LayoutXml(final String routeId, final String controllerName, final String actionName) {
        this.setFileName(
                routeId
                + "_"
                + controllerName.toLowerCase(new Locale("en","EN"))
                + "_"
                + actionName.toLowerCase(new Locale("en","EN"))
                + ".xml"
        );
    }

    /**
     * Get name of file.
     *
     * @return String
     */
    @Override
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Get name of file template.
     *
     * @return String
     */
    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    /**
     * Get language.
     *
     * @return Language
     */
    @Override
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
