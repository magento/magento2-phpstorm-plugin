package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleXmlHaveSetupVersion implements ModuleFileInterface {

    public static String FILE_NAME = "module.xml";
    public static String MODULE_ATTR_NAME = "name";
    public static String TEMPLATE = "Magento Module Xml";
    private static ModuleXmlHaveSetupVersion INSTANCE = null;

    public static ModuleXmlHaveSetupVersion getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ModuleXmlHaveSetupVersion();
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
