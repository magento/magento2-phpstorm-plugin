/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleMenuXml implements ModuleFileInterface {
    public static String fileName = "menu.xml";
    public static String menuTag = "menu";
    public static String addTag = "add";
    public static String idTagAttribute = "id";
    public static String parentTagAttribute = "parent";
    public static String sortOrderTagAttribute = "sortOrder";
    public static String titleTagAttribute = "title";
    public static String moduleTagAttribute = "module";
    public static String resourceTagAttribute = "resource";
    public static String translateTagAttribute = "translate";
    public static String actionTagAttribute = "action";
    public static String defaultAcl = "Magento_Backend::admin";

    public static String template = "Magento Menu XML";

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
