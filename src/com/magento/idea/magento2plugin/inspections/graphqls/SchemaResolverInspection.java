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
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;

public class SchemaResolverInspection extends LocalInspectionTool {

    private final InspectionBundle inspectionBundle = new InspectionBundle();

    @NotNull
    @Override
    public GraphQLVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new GraphQLVisitor() {
            @Override
            public void visitValue(@NotNull GraphQLValue element) {
                String getVisitedElementValue = element.getText();
                if (getVisitedElementValue == null) {
                    return;
                }

                String resolverFQN = GraphQlUtil.resolverStringToPhpFQN(getVisitedElementValue);
                GetPhpClassByFQN getPhpClassByFQN = GetPhpClassByFQN.getInstance(holder.getProject());
                PhpClass resolverClass = getPhpClassByFQN.execute(resolverFQN);
                if (resolverClass == null) {
                    holder.registerProblem(element,
                        inspectionBundle.message(
                            "inspection.graphql.resolver.notExist"
                        ),
                        ProblemHighlightType.ERROR);
                    return;
                }
                if (!GraphQlUtil.isResolver(resolverClass)) {
                    holder.registerProblem(element,
                        inspectionBundle.message(
                            "inspection.graphql.resolver.mustImplement"
                        ),
                        ProblemHighlightType.ERROR);
                }
            }
        };
    }
}
