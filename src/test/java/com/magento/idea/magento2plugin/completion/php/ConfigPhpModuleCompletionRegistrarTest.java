/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.php;

public class ConfigPhpModuleCompletionRegistrarTest extends CompletionPhpFixtureTestCase {
    private static final String[] LOOKUP_MODULE_NAMES = {
            "Magento_Catalog",
            "Magento_Config"
    };

    /**
     * Tests for module name completion under array key 'modules' in config.php
     */
    public void testModuleNameMustHaveCompletion() {
        final String filePath = this.getFixturePath("config.php");
        myFixture.copyFileToProject(filePath);

        assertFileContainsCompletions(filePath, LOOKUP_MODULE_NAMES);
    }

    /**
     * Tests for no module name completion under a different array key in config.php
     */
    public void testModuleNameMustNotHaveCompletion() {
        final String filePath = this.getFixturePath("config.php");
        myFixture.copyFileToProject(filePath);

        assertCompletionNotShowing(filePath);
    }
}
