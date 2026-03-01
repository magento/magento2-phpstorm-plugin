/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import org.junit.jupiter.api.Test;

public class MftfSelectorCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    private static final String[] lookupStringsEntities = new String[] {
        "TestAdminAddProductsToOptionPanelSection",
        "TestAdminProductsPanelSection",
        "TestAdminProductsSection",
        "TestAdminAddProductsToOptionPanelSection.testaddSelectedProducts",
        "TestAdminAddProductsToOptionPanelSection.testapplyFilters",
        "TestAdminAddProductsToOptionPanelSection.testfilters",
        "TestAdminAddProductsToOptionPanelSection.testfirstCheckbox",
        "TestAdminAddProductsToOptionPanelSection.testnameFilter",
        "TestAdminAddProductsToOptionPanelSection.testnthCheckbox",
        "TestAdminProductsPanelSection.testaddSelectedProducts",
        "TestAdminProductsSection.testaddSelectedProducts",
        "TestAdminProductsSection.testapplyFilters",
        "TestAdminProductsSection.testfilters"
      };

    @Test
    public void testSelectorInActionGroupMustProvideCompletion() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @Test
    public void testSelectorInTestMustProvideCompletion() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, lookupStringsEntities);
    }

    @Test
    public void testSelectorInActionGroupMustBeEmpty() {
        String filePath = this.getFixturePath("TestActionGroup.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }

    @Test
    public void testSelectorInTestMustBeEmpty() {
        String filePath = this.getFixturePath("TestMftfTest.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }
}
