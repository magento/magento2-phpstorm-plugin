/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

public class MftfSelectorReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    public void testSelectorInActionGroupMustHaveReference() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("testaddSelectedProducts");
    }

    public void testSelectorInTestMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("testaddSelectedProducts");
    }

    public void testSelectorInActionGroupMustBeEmpty() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    public void testSelectorInTestMustBeEmpty() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }
}
