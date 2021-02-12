/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.jsgraphql.GraphQLIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUsagesCollector;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class GraphQlResolverUsageLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> psiElements, @NotNull Collection<? super LineMarkerInfo<?>> collection) {
        if (psiElements.size() > 0) {
            if (!Settings.isEnabled(psiElements.get(0).getProject())) {
                return;
            }
        }

        for (PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass) {
                List<? extends PsiElement> results;

                if (!GraphQlUtil.isResolver((PhpClass) psiElement)) {
                    return;
                }
                GraphQlUsagesCollector collector = new GraphQlUsagesCollector();
                results = collector.getGraphQLUsages((PhpClass) psiElement);

                if (results.size() > 0 ) {
                    collection.add(NavigationGutterIconBuilder
                            .create(GraphQLIcons.FILE)
                            .setTargets(results)
                            .setTooltipText("Navigate to schema")
                            .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement))
                    );
                }
            }
        }
    }
}
