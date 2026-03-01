/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GraphQlResolverInspectionTest extends InspectionPhpFixtureTestCase {

    private final String errorMessage =  inspectionBundle.message(
        "inspection.graphql.resolver.mustImplement"
    );

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(GraphQlResolverInspection.class);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    @Test
    public void testWithInvalidResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasHighlighting(errorMessage);
    }

    @Test
    public void testWithValidResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }

    @Test
    public void testWithValidBatchResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }

    @Test
    public void testWithValidBatchServiceContractResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("ResolverTest.php"));

        assertHasNoHighlighting(errorMessage);
    }
}
