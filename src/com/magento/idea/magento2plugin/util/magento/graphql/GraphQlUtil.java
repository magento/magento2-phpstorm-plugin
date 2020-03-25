/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util.magento.graphql;

import com.intellij.lang.jsgraphql.psi.GraphQLStringValue;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQlUtil {

    @NotNull
    public static String resolverStringToPhpFQN(String resolverFQN) {
        resolverFQN = resolverFQN.replace("\\\\", "\\").replace("\"","");
        if (!resolverFQN.startsWith("\\")) {
            resolverFQN = "\\".concat(resolverFQN);
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

        if (!argumentIdentifier.getText().equals("class")) {
            return null;
        }

        return argumentStringValue;
    }
}
