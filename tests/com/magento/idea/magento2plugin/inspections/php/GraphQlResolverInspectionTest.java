/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php;

import com.magento.idea.magento2plugin.bundles.InspectionBundle;

public class GraphQlResolverInspectionTest extends InspectionPhpFixtureTestCase {

    private final InspectionBundle inspectionBundle = new InspectionBundle();
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
        myFixture.configureByFile(getFixturePath("InvalidResolverTest.php"));

        assertHasHighlighting(errorMessage);
    }

    public void testWithValidResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ValidResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }

    public void testWithValidBatchResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ValidResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }

    public void testWithValidBatchServiceContractResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ValidResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }
}
