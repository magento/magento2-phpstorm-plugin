/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

public class ModuleScopeInspectionTest extends InspectionXmlFixtureTestCase {

    private static final String WRONG_AREA =
            "inspection.config.wrong.area";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(ModuleScopeInspection.class);
    }

    /**
     * Inspection highlights warning if the area of a config file is wrong.
     */
    public void testIncorrectArea() {
        configureFixture("app/code/Test/TestModule/etc/adminhtmltypo/di.xml");

        final String errorMessage =  inspectionBundle.message(
                WRONG_AREA
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning if the area is correct.
     */
    public void testCorrectArea() {
        configureFixture("app/code/Test/TestModule/etc/adminhtml/di.xml");

        final String errorMessage =  inspectionBundle.message(
                WRONG_AREA

        );

        assertHasNoHighlighting(errorMessage);
    }

    private void configureFixture(final String fixturePath) {
        myFixture.copyFileToProject(getFixturePath("app/code/Test/TestModule/registration.php"));
        myFixture.configureByFile(getFixturePath(fixturePath));
    }
}
