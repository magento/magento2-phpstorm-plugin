/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class MftfNameCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsEntities = new String[] {
        "TestAddOutOfStockProductToCompareListTest",
        "TestVerifyTinyMCEv4IsNativeWYSIWYGOnProductTest"
      };

    @org.junit.jupiter.api.Test
    public void testExtendsMustProvideCompletion() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @org.junit.jupiter.api.Test
    public void testExtendsInActionGroupMustBeEmpty() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.jupiter.api.Test
    public void testExtendsSameNameMustBeEmpty() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @org.junit.jupiter.api.Test
    public void testExtendsSameNameMustBeEmptyForActionGroup() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }
}
