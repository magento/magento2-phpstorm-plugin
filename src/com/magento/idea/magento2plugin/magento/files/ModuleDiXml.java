/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

public class ModuleDiXml implements ModuleFileInterface {
    public static String FILE_NAME = "di.xml";
    public static String TEMPLATE = "Magento Module DI Xml";

    //code templates
    public static String TEMPLATE_PLUGIN = "Magento Module DI Xml Plugin";
    public static String TEMPLATE_PREFERENCE = "Magento Module DI Xml Preference";
    public static String TEMPLATE_CLI_COMMAND = "Magento Module DI Xml CLI Command";

    //tags
    public static String PLUGIN_TYPE_TAG = "type";
    public static String PLUGIN_TYPE_ATTRIBUTE = "type";
    public static String PLUGIN_TAG_NAME = "plugin";
    public static String PLUGIN_TYPE_ATTR_NAME = "name";
    public static String PREFERENCE_TAG_NAME = "preference";
    public static String PREFERENCE_ATTR_FOR = "for";
    public static String DISABLED_ATTR_NAME = "disabled";
    public static String CLI_COMMAND_TAG = "type";
    public static String CLI_COMMAND_ATTR_NAME = "name";
    public static String CLI_COMMAND_ATTR_COMMANDS = "commands";
    public static String CLI_COMMAND_ATTR_ITEM_TAG = "item";
    public static String CLI_COMMAND_INTERFACE = "Magento\\Framework\\Console\\CommandListInterface";

    // general tags
    public static String ARGUMENTS_TAG = "arguments";
    public static String ARGUMENT_TAG = "argument";
    public static String NAME_TAG = "name";


    private static ModuleDiXml INSTANCE = null;

    public static ModuleDiXml getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ModuleDiXml();
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
