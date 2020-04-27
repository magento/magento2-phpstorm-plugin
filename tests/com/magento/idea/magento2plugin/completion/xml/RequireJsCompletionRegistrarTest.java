/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class RequireJsCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    public void testMappedComponentMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "testFile",
            "testFile2"
        });
    }
}
