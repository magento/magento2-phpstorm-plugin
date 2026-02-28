/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class RequireJsCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    @org.junit.jupiter.api.Test
    public void testMappedComponentAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "testFile",
            "testFile2"
        });
    }

    @org.junit.jupiter.api.Test
    public void testMappedComponentItemAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "testFile",
            "testFile2"
        });
    }

    @org.junit.jupiter.api.Test
    public void testFileComponentAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/js/file"
        });
    }

    @org.junit.jupiter.api.Test
    public void testFileComponentItemAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/js/file"
        });
    }
}
