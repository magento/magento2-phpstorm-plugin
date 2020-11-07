/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

public class ModuleNameCompletionRegistrarTest extends CompletionXmlFixtureTestCase {
    private static final String[] LOOKUP_MODULE_NAMES = {
            "Magento_Catalog",
            "Magento_Config"
    };

    /**
     * Tests for module name completion in module.xml
     */
    public void testModuleNameMustHaveCompletion() {
        final String filePath = this.getFixturePath("module.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, LOOKUP_MODULE_NAMES);
    }

    /**
     * Tests for module name completion under the sequence node in module.xml
     */
    public void testSequenceModuleNameMustHaveCompletion() {
        final String filePath = this.getFixturePath("module.xml");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, LOOKUP_MODULE_NAMES);
    }
}
