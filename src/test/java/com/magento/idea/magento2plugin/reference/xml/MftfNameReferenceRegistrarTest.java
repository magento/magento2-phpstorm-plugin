/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

public class MftfNameReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @org.junit.Test
    public void testExtendsMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestVerifyTinyMCEv4IsNativeWYSIWYGOnProductTest");
    }

    @org.junit.Test
    public void testExtendsInActionGroupMustBeEmpty() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }
}
