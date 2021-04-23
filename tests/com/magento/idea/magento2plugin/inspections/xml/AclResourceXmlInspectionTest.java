/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleAclXml;

public class AclResourceXmlInspectionTest extends InspectionXmlFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(AclResourceXmlInspection.class);
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    /**
     * ACL resource should have a title.
     */
    public void testAclResourceWithNoTitleShouldHaveWarning() {
        myFixture.configureByFile(getFixturePath(ModuleAclXml.FILE_NAME));

        final String errorMessage =  inspectionBundle.message(
                "inspection.error.missingAttribute",
                "title"
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Override/Reference for ACL resource may not have a title.
     */
    public void testOverrideAclResourceWithNoTitleShouldNotHaveWarning() {
        myFixture.configureByFile(getFixturePath(ModuleAclXml.FILE_NAME));

        final String errorMessage =  inspectionBundle.message(
                "inspection.error.missingAttribute",
                "title"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * ID attribute of ACL resource should have a value.
     */
    public void testAclResourceWithEmptyIdShouldHaveWarning() {
        myFixture.configureByFile(getFixturePath(ModuleAclXml.FILE_NAME));

        final String errorMessage =  inspectionBundle.message(
                "inspection.error.idAttributeCanNotBeEmpty",
                "id"
        );

        assertHasHighlighting(errorMessage);
    }
}
