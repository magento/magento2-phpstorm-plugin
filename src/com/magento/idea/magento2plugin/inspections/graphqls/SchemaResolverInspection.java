/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.graphqls;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement;
import com.intellij.lang.jsgraphql.psi.GraphQLValue;
import com.intellij.lang.jsgraphql.psi.GraphQLVisitor;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.graphqls.fix.CreateResolverClassQuickFix;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolver;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;

public class SchemaResolverInspection extends LocalInspectionTool {

    private final InspectionBundle inspectionBundle = new InspectionBundle();

    @Override
    public @NotNull GraphQLVisitor buildVisitor(
            final @NotNull ProblemsHolder holder,
            final boolean isOnTheFly
    ) {
        return new GraphQLVisitor() {
            @Override
            public void visitValue(final @NotNull GraphQLValue element) {
                final String getVisitedElementValue = element.getText();
                final PsiElement parentElementValue = element.getParent();

                if (getVisitedElementValue == null
                        || !(parentElementValue instanceof GraphQLNamedElement)) {
                    return;
                }
                final String attributeName = ((GraphQLNamedElement) parentElementValue).getName();

                if (!GraphQlResolver.CLASS_ARGUMENT.equals(attributeName)) {
                    return;
                }
                final String resolverFQN
                        = GraphQlUtil.resolverStringToPhpFQN(getVisitedElementValue);

                if (!resolverFQN.matches(RegExUtil.PhpRegex.FQN)) {
                    holder.registerProblem(
                            element,
                            inspectionBundle.message(
                                    "inspection.warning.class.invalidFormat",
                                    resolverFQN
                            ),
                            ProblemHighlightType.WARNING
                    );
                    return;
                }
                final GetPhpClassByFQN getPhpClassByFQN
                        = GetPhpClassByFQN.getInstance(holder.getProject());
                final PhpClass resolverClass = getPhpClassByFQN.execute(resolverFQN);

                if (resolverClass == null) {
                    holder.registerProblem(
                            element,
                            inspectionBundle.message(
                                    "inspection.graphql.resolver.notExist"
                            ),
                            ProblemHighlightType.ERROR,
                            new CreateResolverClassQuickFix());
                } else if (!GraphQlUtil.isResolver(resolverClass)) {
                    holder.registerProblem(
                            element,
                            inspectionBundle.message(
                                    "inspection.graphql.resolver.mustImplement"
                            ),
                            ProblemHighlightType.ERROR
                    );
                }
            }
        };
    }
}
