/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateGraphQLSchema;
import java.util.Properties;

public class GraphQLSchemaGenerator extends FileGenerator {

    private final String moduleName;

    private final FindOrCreateGraphQLSchema findOrCreateGraphQlSchema;


    /**
     * Constructor for graphQL schema file generator.
     *
     * @param moduleName String
     * @param project String
     */
    public GraphQLSchemaGenerator(
            final String moduleName,
            final Project project) {
        super(project);
        this.moduleName = moduleName;
        findOrCreateGraphQlSchema = new FindOrCreateGraphQLSchema(project);
    }

    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile graphQLSchema = findOrCreateGraphQlSchema.execute(
                actionName,
                moduleName
        );

        if (graphQLSchema == null) {
            return null;
        }
        return graphQLSchema;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {} //NOPMD
}
