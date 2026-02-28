/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

public class MftfPageUrlReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @org.junit.Test
    public void testPageUrlInActionGroupMustHaveReference() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestPage2");
    }

    @org.junit.Test
    public void testPageUrlInActionGroupMustBeEmptyForSection() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    @org.junit.Test
    public void testPageUrlInActionGroupMustBeEmptyForTestDocument() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    @org.junit.Test
    public void testPageUrlInTestMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestPage");
    }

    @org.junit.Test
    public void testPageUrlBeforeInTestMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestPage");
    }

    @org.junit.Test
    public void testPageUrlInTestMustBeEmptyForSection() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    @org.junit.Test
    public void testPageUrlInTestMustBeEmptyForActionGroup() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    @org.junit.Test
    public void testPageUrlInActionGroupPluginDisabled() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);
        disablePluginAndReindex();

        assertEmptyReference();
    }

    @org.junit.Test
    public void testPageUrlInTestMftfSupportDisabled() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);
        disableMftfSupportAndReindex();

        assertEmptyReference();
    }
}
