/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class MftfPageUrlCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStrings = new String[] {
        "TestPage.url",
        "TestPage2.url"
      };

    @org.junit.jupiter.api.Test
    public void testPageUrlInActionGroupMustProvideCompletion() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStrings);
    }
    @org.junit.jupiter.api.Test
    public void testPageUrlInActionGroupMustBeEmptyForSection() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.jupiter.api.Test
    public void testPageUrlInActionGroupMustBeEmptyForTestDocument() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.jupiter.api.Test
    public void testPageUrlInActionGroupMustBeEmptyForEntity() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.jupiter.api.Test
    public void testPageUrlInTestMustProvideCompletion() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStrings);
    }

    @org.junit.jupiter.api.Test
    public void testPageUrlBeforeInTestMustProvideCompletion() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStrings);
    }

    @org.junit.jupiter.api.Test
    public void testPageUrlInTestMustBeEmptyForSection() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.jupiter.api.Test
    public void testPageUrlInTestMustBeEmptyForActionGroup() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }
}
