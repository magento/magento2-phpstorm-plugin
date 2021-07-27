/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.magento.idea.magento2plugin.project.Settings;

public class ClassParamsNotSortedAlphabeticallyInspectionTest
        extends InspectionPhpFixtureTestCase {

    private static final String FILE_PATH = "app/code/Foo/Bar/Test.php";
    private static final String ROOT_PATH =
            "/src/php/ClassParamsNotSortedAlphabeticallyInspection/";
    private final String warningMessage =  inspectionBundle.message(
            "inspection.weak.warning.php.class.fields.should.be.sorted"
    );

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(ClassParamsNotSortedAlphabeticallyInspection.class);
    }

    /**
     * Inspection highlights weak warning for wrong fields modifier order.
     */
    public void testHasHighlightingForWrongModifierOrder() {
        Settings.getInstance(myFixture.getProject())
                .setMagentoPath(ROOT_PATH + "hasHighlightingForWrongModifierOrder");
        myFixture.configureByFile(getFixturePath(FILE_PATH));
        assertHasHighlighting(warningMessage);
    }

    /**
     * Inspection should not highlight weak warning for right fields modifier order.
     */
    public void testHasNoHighlightingForRightModifierOrder() {
        Settings.getInstance(myFixture.getProject())
                .setMagentoPath(ROOT_PATH + "hasNoHighlightingForRightModifierOrder");
        myFixture.configureByFile(getFixturePath(FILE_PATH));
        assertHasNoHighlighting(warningMessage);
    }

    /**
     * Inspection highlights weak warning for wrong fields names order.
     */
    public void testHasHighlightingForWrongNameOrder() {
        Settings.getInstance(myFixture.getProject())
                .setMagentoPath(ROOT_PATH + "hasHighlightingForWrongNameOrder");
        myFixture.configureByFile(getFixturePath(FILE_PATH));
        assertHasHighlighting(warningMessage);
    }

    /**
     * Inspection should not highlight weak warning for right fields names order.
     */
    public void testHasNoHighlightingForRightNameOrder() {
        Settings.getInstance(myFixture.getProject())
                .setMagentoPath(ROOT_PATH + "hasNoHighlightingForRightNameOrder");
        myFixture.configureByFile(getFixturePath(FILE_PATH));
        assertHasNoHighlighting(warningMessage);
    }
}
