/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class RequireJsCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    public void testMappedComponentAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "testFile",
            "testFile2"
        });
    }

    public void testMappedComponentItemAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "testFile",
            "testFile2"
        });
    }

    public void testFileComponentAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/js/file"
        });
    }

    public void testFileComponentItemAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/js/file"
        });
    }
}
