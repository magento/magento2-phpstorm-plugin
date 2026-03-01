/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import org.junit.jupiter.api.Test;

public class RequireJsReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    @Test
    public void testMappedComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    @Test
    public void testPathComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file2.js");
    }

    @Test
    public void testFileComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    @Test
    public void testLibComponentMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("/lib/web/testjs.js");
    }
}
