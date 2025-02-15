/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.graphql;

import com.intellij.lang.jsgraphql.psi.GraphQLStringValue;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolver;
import com.magento.idea.magento2plugin.magento.packages.Package;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GraphQlUtil {

    private GraphQlUtil() {}

    /**
     * Converts resolver FQN to PHP FQN
     * eg. \\Foo\\Bar -> \Foo\Bar
     *
     * @param resolverFQN String
     * @return String
     */
    @NotNull
    public static String resolverStringToPhpFQN(final String resolverFQN) {
        String result = resolverFQN.replace("\\\\", Package.fqnSeparator).replace("\"","");
        if (!result.startsWith(Package.fqnSeparator)) {
            result = Package.fqnSeparator.concat(result);
        }
        return result;
    }

    /**
     * Fetch a string from an argument.
     *
     * @param argument PsiElement
     * @return GraphQLStringValue
     */
    @Nullable
    public static GraphQLStringValue fetchResolverQuotedStringFromArgument(
            final PsiElement argument
    ) {
        final PsiElement[] argumentChildren = argument.getChildren();

        final int argumentChildrenMin = 2;
        if (argumentChildren.length < argumentChildrenMin) {
            return null;
        }
        if (!(argumentChildren[1] instanceof GraphQLStringValue)) {
            return null;
        }


        final PsiElement argumentIdentifier = argumentChildren[0];
        if (!argumentIdentifier.getText().equals(GraphQlResolver.CLASS_ARGUMENT)) {
            return null;
        }

        return (GraphQLStringValue) argumentChildren[1];
    }

    /**
     * Checks whether PHP class is resolver.
     *
     * @param psiElement PhpClass
     * @return boolean
     */
    public static boolean isResolver(final PhpClass psiElement) {
        final PhpClass[] implementedInterfaces = psiElement.getImplementedInterfaces();
        for (final PhpClass implementedInterface: implementedInterfaces) {
            if (implementedInterface.getFQN().equals(
                    GraphQlResolver.RESOLVER_INTERFACE
            ) || implementedInterface.getFQN().equals(
                    GraphQlResolver.BATCH_RESOLVER_INTERFACE
            ) || implementedInterface.getFQN().equals(
                    GraphQlResolver.BATCH_SERVICE_CONTRACT_RESOLVER_INTERFACE
            )) {
                return true;
            }
        }
        return false;
    }
}
