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

public class GraphQlUtil {

    @NotNull
    public static String resolverStringToPhpFQN(String resolverFQN) {
        resolverFQN = resolverFQN.replace("\\\\", Package.FQN_SEPARATOR).replace("\"","");
        if (!resolverFQN.startsWith(Package.FQN_SEPARATOR)) {
            resolverFQN = Package.FQN_SEPARATOR.concat(resolverFQN);
        }
        return resolverFQN;
    }

    @Nullable
    public static GraphQLStringValue fetchResolverQuotedStringFromArgument(PsiElement argument) {
        PsiElement[] argumentChildren = argument.getChildren();

        if (argumentChildren.length < 2) {
            return null;
        }
        PsiElement argumentIdentifier = argumentChildren[0];
        if (!(argumentChildren[1] instanceof GraphQLStringValue)) {
            return null;
        }

        GraphQLStringValue argumentStringValue = (GraphQLStringValue) argumentChildren[1];

        if (!argumentIdentifier.getText().equals(GraphQlResolver.CLASS_ARGUMENT)) {
            return null;
        }

        return argumentStringValue;
    }

    public static boolean isResolver(PhpClass psiElement) {
        PhpClass[] implementedInterfaces = psiElement.getImplementedInterfaces();
        for (PhpClass implementedInterface: implementedInterfaces) {
            if (!implementedInterface.getFQN().equals(GraphQlResolver.RESOLVER_INTERFACE)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
