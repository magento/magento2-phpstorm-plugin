/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.graphqls;

import com.magento.idea.magento2plugin.magento.files.SchemaGraphQLsFile;

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

    /**
     * Inspection with valid schema resolver.
     */
    public void testWithValidSchemaResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(SchemaGraphQLsFile.FILE_NAME));
        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection with invalid schema resolver.
     */
    public void testWithInvalidSchemaResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(SchemaGraphQLsFile.FILE_NAME));
        assertHasHighlighting(errorMessage);
    }

    /**
     * Inspection with valid batch resolver.
     */
    public void testWithValidBatchResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(SchemaGraphQLsFile.FILE_NAME));

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection with valid batch service contract resolver.
     */
    public void testWithValidBatchServiceContractResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath(SchemaGraphQLsFile.FILE_NAME));

        assertHasNoHighlighting(errorMessage);
    }
}
