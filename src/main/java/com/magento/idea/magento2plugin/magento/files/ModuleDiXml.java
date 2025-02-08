/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;

@SuppressWarnings({
        "PMD.FieldNamingConventions",
        "PMD.RedundantFieldInitializer",
        "PMD.NonThreadSafeSingleton",
        "PMD.TooManyFields"
})
public class ModuleDiXml implements ModuleFileInterface {
    public static String FILE_NAME = "di.xml";
    public static String TEMPLATE = "Magento DI XML";

    //code templates
    public static String TEMPLATE_PLUGIN = "Magento Module DI Xml Plugin";
    public static String TEMPLATE_PREFERENCE = "Magento Module DI Xml Preference";
    public static String TEMPLATE_CLI_COMMAND = "Magento Module DI Xml CLI Command";

    //tags
    public static String TYPE_TAG = "type";
    public static String PLUGIN_TAG_NAME = "plugin";
    public static String PREFERENCE_TAG_NAME = "preference";
    public static String DISABLED_ATTR_NAME = "disabled";

    //classes
    public static String CLI_COMMAND_INTERFACE =
            "Magento\\Framework\\Console\\CommandListInterface";
    public static String DATA_PROVIDER_SEARCH_RESULT =
            "Magento\\Framework\\View\\Element\\UiComponent\\DataProvider\\SearchResult";
    public static String DATA_PROVIDER_COLLECTION_FACTORY =
            "Magento\\Framework\\View\\Element\\UiComponent\\DataProvider\\CollectionFactory";

    // general tags
    public static String ARGUMENTS_TAG = "arguments";
    public static String ARGUMENT_TAG = "argument";
    public static String NAME_TAG = "name";
    public static String VIRTUAL_TYPE_TAG = "virtualType";
    public static String ITEM_TAG = "item";

    // attributes
    public static String NAME_ATTR = "name";
    public static String SORT_ORDER_ATTR = "sortOrder";
    public static String TYPE_ATTR = "type";
    public static String PREFERENCE_ATTR_FOR = "for";
    public static String CLI_COMMAND_ATTR_COMMANDS = "commands";
    public static String MAIN_TABLE_ATTR = "mainTable";
    public static String RESOURCE_MODEL_ATTR = "resourceModel";
    public static String XSI_TYPE_ATTR = "xsi:type";

    // attribute values
    public static String XSI_TYPE_OBJECT = "object";
    public static String XSI_TYPE_STRING = "string";
    public static String XSI_TYPE_ARRAY = "array";
    public static String COLLECTIONS_ATTR_VALUE = "collections";

    private static ModuleDiXml INSTANCE = null;

    /**
     * Get instance of the class.
     *
     * @return ModuleDiXml
     */
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
