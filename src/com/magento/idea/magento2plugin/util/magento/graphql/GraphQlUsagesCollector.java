package com.magento.idea.magento2plugin.util.magento.graphql;

import com.intellij.lang.jsgraphql.psi.GraphQLQuotedString;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.stubs.indexes.graphql.GraphQlResolverIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphQlUsagesCollector {

    public HashMap<String, List<GraphQLQuotedString>> graphQlCache = new HashMap<>();

    public List<GraphQLQuotedString> getGraphQLUsages(@NotNull PhpClass phpClass) {
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
