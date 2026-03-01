/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import org.junit.jupiter.api.Test;

public class MftfNameReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @Test
    public void testExtendsMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestVerifyTinyMCEv4IsNativeWYSIWYGOnProductTest");
    }

    @Test
    public void testExtendsInActionGroupMustBeEmpty() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }
}
