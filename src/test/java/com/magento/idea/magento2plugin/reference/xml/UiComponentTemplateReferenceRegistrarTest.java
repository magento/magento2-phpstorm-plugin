/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

public class UiComponentTemplateReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @org.junit.Test
    public void testFileTemplateAttributeMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/template/template2.html");
    }
}
