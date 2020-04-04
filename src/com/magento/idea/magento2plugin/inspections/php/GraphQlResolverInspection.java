package com.magento.idea.magento2plugin.inspections.php;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.jsgraphql.psi.GraphQLQuotedString;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.stubs.indexes.graphql.GraphQlResolverIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphQlResolverInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new PhpElementVisitor() {
            public void visitPhpClass(PhpClass resolverClass) {
                List<? extends PsiElement> results;
                GraphQlUsagesCollector collector = new GraphQlUsagesCollector();
                results = collector.getGraphQLUsages(resolverClass);
                if (results.size() > 0 ) {
                    if (!isResolver(resolverClass)) {
                        PsiElement currentClassNameIdentifier = ((PhpClass) resolverClass).getNameIdentifier();
                        assert currentClassNameIdentifier != null;
                        problemsHolder.registerProblem(currentClassNameIdentifier,
                                "Must implements Magento\\Framework\\GraphQl\\Query\\ResolverInterface",
                                ProblemHighlightType.ERROR);
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
        };
    }
    private class GraphQlUsagesCollector {

        private HashMap<String, List<GraphQLQuotedString>> graphQlCache = new HashMap<>();

        List<GraphQLQuotedString> getGraphQLUsages(@NotNull PhpClass phpClass) {
            List<GraphQLQuotedString> graphQLQuotedStrings = new ArrayList<>();

            graphQLQuotedStrings.addAll(getUsages(phpClass));

            return graphQLQuotedStrings;
        }

        List<GraphQLQuotedString> getUsages(@NotNull PhpClass phpClass) {
            String phpClassFQN = phpClass.getFQN();
            if (!graphQlCache.containsKey(phpClassFQN)) {
                List<GraphQLQuotedString> graphQLStringValues = extractGraphQLQuotesStringsForClass(phpClass);
                graphQlCache.put(phpClassFQN, graphQLStringValues);
            }
            return graphQlCache.get(phpClassFQN);
        }

        List<GraphQLQuotedString> extractGraphQLQuotesStringsForClass(@NotNull PhpClass phpClass) {
            return GraphQlResolverIndex.getGraphQLUsages(phpClass);
        }
    }
}
