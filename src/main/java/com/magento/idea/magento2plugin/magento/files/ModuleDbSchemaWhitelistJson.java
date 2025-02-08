/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;

public class ModuleDbSchemaWhitelistJson implements ModuleFileInterface {
    private static final ModuleDbSchemaWhitelistJson INSTANCE = new ModuleDbSchemaWhitelistJson();
    public static final String FILE_NAME = "db_schema_whitelist.json";
    public static final String TEMPLATE = "Magento Module Declarative Schema Whitelist JSON";

    public static final String COLUMN_OBJECT_NAME = "column";
    public static final String CONSTRAINT_OBJECT_NAME = "constraint";
    public static final String INDEX_OBJECT_NAME = "index";

    /**
     * Get singleton instance of class.
     */
    public static ModuleDbSchemaWhitelistJson getInstance() {
        return INSTANCE;
    }

    /**
     * Get filename.
     *
     * @return String
     */
    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    /**
     * Get template name.
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
        return JsonLanguage.INSTANCE;
    }
}
