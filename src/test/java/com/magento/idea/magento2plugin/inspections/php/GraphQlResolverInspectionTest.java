/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php;

public class GraphQlResolverInspectionTest extends InspectionPhpFixtureTestCase {

    private final String errorMessage =  inspectionBundle.message(
        "inspection.graphql.resolver.mustImplement"
    );

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(GraphQlResolverInspection.class);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testWithInvalidResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasHighlighting(errorMessage);
    }

    public void testWithValidResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }

    public void testWithValidBatchResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }

    public void testWithValidBatchServiceContractResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }
}
