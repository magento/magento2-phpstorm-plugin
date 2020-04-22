/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.php.fix.PhpImplementResolverClassQuickFix;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUsagesCollector;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GraphQlResolverInspection extends PhpInspection {

    private final InspectionBundle inspectionBundle = new InspectionBundle();

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new PhpElementVisitor() {
            public void visitPhpClass(PhpClass resolverClass) {
                List<? extends PsiElement> results;
                GraphQlUsagesCollector collector = new GraphQlUsagesCollector();
                results = collector.getGraphQLUsages(resolverClass);
                if (results.size() > 0 ) {
                    if (!GraphQlUtil.isResolver(resolverClass)) {
                        PsiElement currentClassNameIdentifier = resolverClass.getNameIdentifier();
                        assert currentClassNameIdentifier != null;
                        problemsHolder.registerProblem(currentClassNameIdentifier,
                                inspectionBundle.message(
                                    "inspection.graphql.resolver.mustImplement"
                                ),
                                ProblemHighlightType.ERROR,
                                new PhpImplementResolverClassQuickFix());
                    }
                }
            }
        };
    }
}
