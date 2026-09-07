/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class GraphQlResolverFileData {
    private String graphQlResolverDirectory;
    private String graphQlResolverClassName;
    private String graphQlResolverModule;
    private String graphQlResolverClassFqn;
    private String namespace;

    public GraphQlResolverFileData(
            String graphQlResolverDirectory,
            String graphQlResolverClassName,
            String graphQlResolverModule,
            String graphQlResolverClassFqn,
            String namespace
    ) {
        this.graphQlResolverDirectory = graphQlResolverDirectory;
        this.graphQlResolverClassName = graphQlResolverClassName;
        this.graphQlResolverModule = graphQlResolverModule;
        this.graphQlResolverClassFqn = graphQlResolverClassFqn;
        this.namespace = namespace;
    }

    public String getGraphQlResolverClassName() {
        return graphQlResolverClassName;
    }

    public String getGraphQlResolverDirectory() {
        return graphQlResolverDirectory;
    }

    public String getGraphQlResolverModule() {
        return graphQlResolverModule;
    }

    public String getGraphQlResolverClassFqn() { return graphQlResolverModule; }

    public String getNamespace() {
        return namespace;
    }
}
