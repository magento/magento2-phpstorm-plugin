/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.js;

public class RequireJsReferenceRegistrarTest extends ReferenceJsFixtureTestCase {

    public void testMappedInjectionParameterMustHaveReference() {
        String filePath = this.getFixturePath("test.js");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    public void testPathInjectionParameterMustHaveReference() {
        String filePath = this.getFixturePath("test.js");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file2.js");
    }

    public void testFileInjectionParameterMustHaveReference() {
        String filePath = this.getFixturePath("test.js");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/js/file.js");
    }

    public void testLibInjectionParameterMustHaveReference() {
        String filePath = this.getFixturePath("test.js");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("/lib/web/testjs.js");
    }
}
