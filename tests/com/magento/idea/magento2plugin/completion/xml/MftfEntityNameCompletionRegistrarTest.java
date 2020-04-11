/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class MftfEntityNameCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsEntities = new String[]{
                "TestAdminMenuCatalog",
                "TestAdminMenuCatalog.pageTitle",
                "TestDefaultAttributeSet",
                "TestDefaultAttributeSet.attribute_set_id"
        };

    public void testCreateDataActionGroupMustProvideCompletion () {
        String filePath = this.getFixturePath(
                "TestActionGroup.xml"
        );
        myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsEntities);
    }

    public void testCreateDataInTestMustProvideCompletion () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsEntities);
    }

    public void testUpdateDataActionGroupMustProvideCompletion () {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsEntities);
    }

    public void testUpdateDataInTestMustProvideCompletion () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsEntities);
    }

    public void testUserInputInActionGroupMustProvideCompletion () {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsEntities);
    }

    public void testUserInputInTestMustProvideCompletion () {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsEntities);
    }

    public void testEntityExtendsInDataMustProvideCompletion () {
        String filePath = this.getFixturePath("TestData.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionMatchWithFilePositiveCase(filePath, lookupStringsEntities);
    }
}
