/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import org.junit.jupiter.api.Test;

public class RequireJsCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    @Test
    public void testMappedComponentAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "testFile",
            "testFile2"
        });
    }

    @Test
    public void testMappedComponentItemAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "testFile",
            "testFile2"
        });
    }

    @Test
    public void testFileComponentAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/js/file"
        });
    }

    @Test
    public void testFileComponentItemAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.copyFileToProject(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/js/file"
        });
    }
}
