/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.graphqls;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLArguments;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;

public class SchemaResolverInspection extends LocalInspectionTool {

    public static final String GraphQlResolverProblemDescription = "Class must implements \\Magento\\Framework\\GraphQl\\Query\\ResolverInterface";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                PsiElement[] directiveChildren = element.getChildren();

                for (PsiElement directiveChild : directiveChildren) {
                    if (!(directiveChild instanceof GraphQLArguments)) {
                        continue;
                    }

                    PsiElement[] argumentsChildren = directiveChild.getChildren();
                    for (PsiElement argumentsChild : argumentsChildren) {
                        if (!(argumentsChild instanceof GraphQLArgument)) {
                            continue;
                        }

                        PsiElement argumentStringValue = GraphQlUtil.fetchResolverQuotedStringFromArgument(argumentsChild);
                        if (argumentStringValue == null) continue;

                        String resolverFQN = argumentStringValue.getText();
                        if (resolverFQN == null) {
                            continue;
                        }

                        resolverFQN = GraphQlUtil.resolverStringToPhpFQN(resolverFQN);
                        GetPhpClassByFQN getPhpClassByFQN = GetPhpClassByFQN.getInstance(holder.getProject());
                        PhpClass resolverClass = getPhpClassByFQN.execute(resolverFQN);
                        if (resolverClass == null) {
                            continue;
                        }
                        if (GraphQlUtil.isNotResolver(resolverClass)) {
                            holder.registerProblem(argumentStringValue,
                                    GraphQlResolverProblemDescription,
                                    ProblemHighlightType.ERROR);
                        }
                    }
                }
            }
        };
    }
}
