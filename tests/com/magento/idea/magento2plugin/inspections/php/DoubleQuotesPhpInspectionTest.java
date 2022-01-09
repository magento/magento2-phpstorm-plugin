/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

public class DoubleQuotesPhpInspectionTest extends InspectionPhpFixtureTestCase {

    private final String errorMessage =  inspectionBundle.message(
            "inspection.warning.double.quotes.misuse"
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

    /**
     * Double quotes without variables should be highlighted.
     */
    public void testDoubleQuotesWithoutVariables() throws Exception {
        myFixture.configureByFile(getFixturePath("ClassIndex.php"));

        assertHasHighlighting(errorMessage);
    }

    /**
     * Double quotes with variables should not be highlighted.
     */
    public void testDoubleQuotesWithVariables() throws Exception {
        myFixture.configureByFile(getFixturePath("ClassIndex.php"));

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Single quotes should not be highlighted.
     */
    public void testSingleQuotes() throws Exception {
        myFixture.configureByFile(getFixturePath("ClassIndex.php"));

        assertHasNoHighlighting(errorMessage);
    }
}
