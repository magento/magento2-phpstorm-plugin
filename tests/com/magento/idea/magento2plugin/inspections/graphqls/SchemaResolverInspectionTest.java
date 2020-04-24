/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.graphqls;

import com.magento.idea.magento2plugin.magento.files.GraphQLSchema;

public class SchemaResolverInspectionTest extends InspectionGraphqlsFixtureTestCase {

    private final String errorMessage =  inspectionBundle.message(
            "inspection.graphql.resolver.mustImplement"
    );

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(SchemaResolverInspection.class);
    }

    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testWithValidSchemaResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(GraphQLSchema.FILE_NAME));
        assertHasNoHighlighting(errorMessage);
    }

    public void testWithInvalidSchemaResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(GraphQLSchema.FILE_NAME));
        assertHasHighlighting(errorMessage);
    }

    public void testWithValidBatchResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(GraphQLSchema.FILE_NAME));

        assertHasNoHighlighting(errorMessage);
    }

    public void testWithValidBatchServiceContractResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(GraphQLSchema.FILE_NAME));

        assertHasNoHighlighting(errorMessage);
    }
}
