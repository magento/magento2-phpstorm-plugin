/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php;

public class DoubleQuotesPhpInspectionTest extends InspectionPhpFixtureTestCase {

    private final String errorMessage =  inspectionBundle.message(
        "inspection.displayName.DoubleQuotesPhpInspection"
    );

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(DoubleQuotesPhpInspection.class);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testDoubleQuotesWithoutVariables() throws Exception {
        myFixture.configureByFile(getFixturePath("ClassIndex.php"));

        assertHasHighlighting(errorMessage);
    }

    public void testDoubleQuotesWithVariables() throws Exception {
        myFixture.configureByFile(getFixturePath("ClassIndex.php"));

        assertHasNoHighlighting(errorMessage);
    }

    public void testSingleQuotes() throws Exception {
        myFixture.configureByFile(getFixturePath("ClassIndex.php"));

        assertHasNoHighlighting(errorMessage);
    }
}
