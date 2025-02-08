/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUsagesCollector;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQlResolverUsageLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (!psiElements.isEmpty() && !Settings.isEnabled(psiElements.get(0).getProject())) {
            return;
        }

        for (final PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass) {
                if (!GraphQlUtil.isResolver((PhpClass) psiElement)) {
                    return;
                }
                final GraphQlUsagesCollector collector = new GraphQlUsagesCollector();//NOPMD
                final List<? extends PsiElement> results = collector.getGraphQLUsages(
                        (PhpClass) psiElement
                );

                if (!results.isEmpty()) {
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
