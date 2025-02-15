/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

public class UiComponentTemplateCompletionRegistrarTest extends CompletionXmlFixtureTestCase {

    public void testFileTemplateAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/template",
            "Foo_Bar/template2"
        });
    }

    public void testFileTemplateItemAttributeMustHaveCompletion() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertCompletionContains(filePath, new String[] {
            "Foo_Bar/template",
            "Foo_Bar/template2"
        });
    }
}
