/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

public class RequireJsReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @org.junit.jupiter.api.Test
    public void testMappedComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    @org.junit.jupiter.api.Test
    public void testPathComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file2.js");
    }

    @org.junit.jupiter.api.Test
    public void testFileComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    @org.junit.jupiter.api.Test
    public void testLibComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("/lib/web/testjs.js");
    }
}
