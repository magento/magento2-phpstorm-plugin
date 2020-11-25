/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.graphqls;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.jsgraphql.psi.GraphQLValue;
import com.intellij.lang.jsgraphql.psi.GraphQLVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.graphqls.fix.CreateResolverClassQuickFix;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;

public class SchemaResolverInspection extends LocalInspectionTool {

    private final InspectionBundle inspectionBundle = new InspectionBundle();

    @NotNull
    @Override
    public GraphQLVisitor buildVisitor(
            @NotNull final ProblemsHolder holder,
            final boolean isOnTheFly
    ) {
        return new GraphQLVisitor() {
            @Override
            public void visitValue(@NotNull final GraphQLValue element) {
                final String getVisitedElementValue = element.getText();
                if (getVisitedElementValue == null) {
                    return;
                }

                final String resolverFQN
                        = GraphQlUtil.resolverStringToPhpFQN(getVisitedElementValue);
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
