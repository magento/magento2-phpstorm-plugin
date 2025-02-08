/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

public class MftfEntityNameReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    public void testCreateDataActionGroupMustHaveReference() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestAdminMenuCatalog");
    }

    public void testCreateDataInTestMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestAdminMenuCatalog");
    }

    public void testUpdateDataActionGroupMustHaveReference() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestAdminMenuCatalog");
    }

    public void testUpdateDataInTestMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestAdminMenuCatalog");
    }

    public void testUserInputInActionGroupMustHaveReference() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestAdminMenuCatalog");
    }

    public void testUserInputInTestMustHaveReference() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestAdminMenuCatalog");
    }

    public void testEntityExtendsInDataMustHaveReference() {
        String filePath = this.getFixturePath("TestData.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToXmlAttributeValue("TestAdminMenuCatalog");
    }

    public void testCreateDataActionGroupPluginDisabled() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);
        disablePluginAndReindex();

        assertEmptyReference();
    }

    public void testCreateDataActionGroupMftfSupportDisabled() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.configureByFile(filePath);
        disableMftfSupportAndReindex();

        assertEmptyReference();
    }

    public void testCreateDataInTestWithSectionMustBeEmpty() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    public void testUpdateDataInTestWithSectionMustBeEmpty() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    public void testUserInputInTestMustBeEmpty() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }

    public void testEntityExtendsInDataMustBeEmpty() {
        String filePath = this.getFixturePath("TestData.xml");
        myFixture.configureByFile(filePath);

        assertEmptyReference();
    }
}
