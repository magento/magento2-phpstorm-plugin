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
    public static final String DEFAULT_FILENAME = "default.xml";
    public static final String CACHEABLE_ATTRIBUTE_NAME = "cacheable";
    public static final String CACHEABLE_ATTRIBUTE_VALUE_FALSE = "false";
    public static final String BLOCK_ATTRIBUTE_TAG_NAME = "block";
    public static final String REFERENCE_BLOCK_ATTRIBUTE_TAG_NAME = "referenceBlock";
    public static final String ROOT_TAG_NAME = "body";
    public static final String REFERENCE_CONTAINER_TAG_NAME = "referenceContainer";
    public static final String UI_COMPONENT_TAG_NAME = "uiComponent";
    public static final String XML_ATTRIBUTE_TEMPLATE = "template";
    public static final String ARGUMENTS_TEMPLATE = "Magento Module Class Arguments In Xml";
    public static final String PARENT_DIR = "layout";
    public static final String PAGE_LAYOUT_DIR = "page_layout";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String CONTENT_CONTAINER_NAME = "content";

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
     * Layout XML file.
     *
     * @param routeId String
     */
    public LayoutXml(final String routeId) {
        this.setFileName(routeId + ".xml");
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
