/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.graphql;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLStringValue;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.graphql.GraphQlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class GraphQlResolverClassLineMarkerProvider implements LineMarkerProvider {
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
            if (psiElement instanceof GraphQLArgument) {
                Collection<? extends PsiElement> results;

                GraphQlResolverClassCollector collector = new GraphQlResolverClassCollector();
                results = collector.getGraphQLResolverClasses((GraphQLArgument) psiElement);
                GraphQLStringValue argumentStringValue = GraphQlUtil.fetchResolverQuotedStringFromArgument(psiElement);
                if (argumentStringValue == null) {
                    continue;
                }

                if (results.size() > 0 ) {
                    collection.add(NavigationGutterIconBuilder
                            .create(PhpIcons.CLASS)
                            .setTargets(results)
                            .setTooltipText("Navigate to class")
                            .createLineMarkerInfo(PsiTreeUtil.getDeepestLast(argumentStringValue))
                    );
                }
            }
        }
    }

    private static class GraphQlResolverClassCollector {

        private HashMap<String, Collection<PhpClass>> routesCache = new HashMap<>();


        Collection<PhpClass> getGraphQLResolverClasses(@NotNull GraphQLArgument graphQLArgument) {
            List<PhpClass> graphQLResolverClasses = new ArrayList<>();
            GraphQLStringValue argumentStringValue = GraphQlUtil.fetchResolverQuotedStringFromArgument(graphQLArgument);
            if (argumentStringValue == null) {
                return graphQLResolverClasses;
            }

            graphQLResolverClasses.addAll(getUsages(argumentStringValue));

            return graphQLResolverClasses;
        }

        Collection<PhpClass> getUsages(@NotNull GraphQLStringValue graphQLStringValue) {
            String phpClassFQN = GraphQlUtil.resolverStringToPhpFQN(graphQLStringValue.getText());
            if (!routesCache.containsKey(phpClassFQN)) {

                Collection<PhpClass> phpClasses = extractClasses(phpClassFQN, graphQLStringValue.getProject());
                routesCache.put(phpClassFQN, phpClasses);
            }
            return routesCache.get(phpClassFQN);
        }

        Collection<PhpClass> extractClasses(@NotNull String phpClassFQN, Project project) {
            PhpIndex phpIndex = PhpIndex.getInstance(project);
            return phpIndex.getClassesByFQN(phpClassFQN);
        }
    }
}
