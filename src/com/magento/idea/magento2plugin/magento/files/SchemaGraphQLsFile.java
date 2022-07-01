/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.GraphQLLanguage;

public class SchemaGraphQLsFile implements ModuleFileInterface {

    public static final String FILE_NAME = "schema.graphqls";

    public static final String TEMPLATE = "Magento GraphQL Schema";
    private static final SchemaGraphQLsFile INSTANCE = new SchemaGraphQLsFile();

    public static SchemaGraphQLsFile getInstance() {
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
        return GraphQLLanguage.INSTANCE;
    }
}
