/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class GraphQlResolverPhp implements ModuleFileInterface {
    public static String TEMPLATE = "Magento GraphQL Resolver Class";
    public static String DEFAULT_DIR = "Model";
    public static final String GRAPHQL_RESOLVER_TEMPLATE_NAME = "Magento GraphQL Resolver Method Resolve";

    private String fileName;

    public GraphQlResolverPhp(String className) {
        fileName = className.concat(".php");
    }

    public static GraphQlResolverPhp getInstance(String className) { return new GraphQlResolverPhp(className); }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
