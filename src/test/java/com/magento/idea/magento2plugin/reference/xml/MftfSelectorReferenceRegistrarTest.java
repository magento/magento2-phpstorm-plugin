/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import org.junit.jupiter.api.Test;

public class MftfSelectorReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @Test
    public void testSelectorInActionGroupMustHaveReference() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("testaddSelectedProducts");
    }

    @Test
    public void testSelectorInTestMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("testaddSelectedProducts");
    }

    @Test
    public void testSelectorInActionGroupMustBeEmpty() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    @Test
    public void testSelectorInTestMustBeEmpty() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }
}
