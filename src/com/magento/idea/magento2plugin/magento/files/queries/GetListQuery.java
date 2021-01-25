/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files.queries;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import com.magento.idea.magento2plugin.magento.packages.Package;
import org.jetbrains.annotations.NotNull;

public final class GetListQuery implements ModuleFileInterface {
    public static final String DIRECTORY = "Query";
    public static final String CLASS_NAME = "GetListQuery";
    public static final String FILE_EXTENSION = "php";
    public static final String TEMPLATE = "Magento Get List Query Model";
    private static final GetListQuery INSTANCE = new GetListQuery();

    /**
     * Get singleton instance of the class.
     *
     * @return GetListQuery
     */
    public static GetListQuery getInstance() {
        return INSTANCE;
    }

    /**
     * Get class FQN.
     *
     * @param moduleName String
     *
     * @return String
     */
    public static String getClassFqn(final @NotNull String moduleName) {
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                moduleName,
                GetListQuery.CLASS_NAME,
                GetListQuery.DIRECTORY
        );

        return String.format(
                "%s%s%s",
                namespaceBuilder.getNamespace(),
                Package.fqnSeparator,
                CLASS_NAME
        );
    }

    @Override
    public String getFileName() {
        return CLASS_NAME.concat("." + FILE_EXTENSION);
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
