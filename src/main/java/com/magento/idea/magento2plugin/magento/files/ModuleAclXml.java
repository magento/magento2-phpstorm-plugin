/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleAclXml implements ModuleFileInterface {
    private static final ModuleAclXml INSTANCE = new ModuleAclXml();
    public static final String FILE_NAME = "acl.xml";
    public static final String TEMPLATE = "Magento ACL XML";

    //attributes
    public static final String XML_ATTR_ID = "id";
    public static final String XML_ATTR_TITLE = "title";

    //tags
    public static final String XML_TAG_RESOURCE = "resource";
    public static final String XML_TAG_RESOURCES = "resources";
    public static final String XML_TAG_ACL = "acl";

    public static ModuleAclXml getInstance() {
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
