/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class CategoryFormXmlFile implements ModuleFileInterface {

    public static final String FILE_NAME = "category_form.xml";
    public static final String TEMPLATE = "Magento Category Admin Form XML";
    public static final String DECLARATION_TEMPLATE = "Magento Category Admin Form XML Decoration";
    public static final String DIRECTORY = "view/adminhtml/ui_component";
    public static final String SUB_DIRECTORY = "ui_component";

    //attributes
    public static final String XML_ATTR_FIELDSET_NAME = "name";
    public static final String XML_ATTR_FIELD_NAME = "name";
    public static final String XML_ATTR_FIELD_SORT_ORDER = "sortOrder";
    public static final String XML_ATTR_FIELD_FORM_ELEMENT = "formElement";

    //tags
    public static final String XML_TAG_FIELDSET = "fieldset";
    public static final String XML_TAG_FIELD = "field";

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
