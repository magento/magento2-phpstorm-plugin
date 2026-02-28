/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class MftfEntityNameCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsEntities = new String[] {
        "TestAdminMenuCatalog",
        "TestAdminMenuCatalog.pageTitle",
        "TestDefaultAttributeSet",
        "TestDefaultAttributeSet.attribute_set_id"
      };

    @org.junit.Test
    public void testCreateDataActionGroupMustProvideCompletion() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.Test
    public void testCreateDataInTestMustProvideCompletion () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.Test
    public void testCreateDataInTestWithSectionMustBeEmpty () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.Test
    public void testUpdateDataActionGroupMustProvideCompletion () {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.Test
    public void testUpdateDataActionGroupMustBeEmpty () {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.Test
    public void testUpdateDataInTestMustProvideCompletion () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.Test
    public void testUpdateDataInTestMustBeEmpty () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.Test
    public void testUserInputInActionGroupMustProvideCompletion () {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.Test
    public void testUserInputInActionGroupMustBeEmpty () {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.Test
    public void testUserInputInTestMustProvideCompletion () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.Test
    public void testUserInputInTestMustBeEmpty () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.Test
    public void testEntityExtendsInDataMustProvideCompletion () {
        String filePath = this.getFixturePath("TestData.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.Test
    public void testEntityExtendsInDataMustBeEmpty () {
        String filePath = this.getFixturePath("TestData.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }
}
