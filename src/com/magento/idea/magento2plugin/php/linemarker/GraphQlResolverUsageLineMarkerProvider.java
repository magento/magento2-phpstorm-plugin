package com.magento.idea.magento2plugin.php.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.jsgraphql.GraphQLIcons;
import com.intellij.lang.jsgraphql.psi.GraphQLQuotedString;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.graphql.GraphQlResolverIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GraphQlResolverUsageLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> collection) {
        if (psiElements.size() > 0) {
            if (!Settings.isEnabled(psiElements.get(0).getProject())) {
                return;
            }
        }

        for (PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass) {
                List<? extends PsiElement> results;

                if (!isResolver((PhpClass) psiElement)) {
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

    private boolean isResolver(PhpClass psiElement) {
        PhpClass[] implementedInterfaces = psiElement.getImplementedInterfaces();
        for (PhpClass implementedInterface: implementedInterfaces) {
            if (!implementedInterface.getFQN().equals("\\Magento\\Framework\\GraphQl\\Query\\ResolverInterface")) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static class GraphQlUsagesCollector {

        private HashMap<String, List<GraphQLQuotedString>> routesCache = new HashMap<>();

        List<GraphQLQuotedString> getGraphQLUsages(@NotNull PhpClass phpClass) {
            List<GraphQLQuotedString> graphQLQuotedStrings = new ArrayList<>();

            graphQLQuotedStrings.addAll(getUsages(phpClass));

            return graphQLQuotedStrings;
        }

        List<GraphQLQuotedString> getUsages(@NotNull PhpClass phpClass) {
            String phpClassFQN = phpClass.getFQN();
            if (!routesCache.containsKey(phpClassFQN)) {
                List<GraphQLQuotedString> graphQLStringValues = extractGraphQLQuotesStringsForClass(phpClass);
                routesCache.put(phpClassFQN, graphQLStringValues);
            }
            return routesCache.get(phpClassFQN);
        }

        List<GraphQLQuotedString> extractGraphQLQuotesStringsForClass(@NotNull PhpClass phpClass) {
            return GraphQlResolverIndex.getGraphQLUsages(phpClass);
        }
    }
}
