/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.js;

import com.magento.idea.magento2plugin.reference.provider.KnockoutTemplateReferenceProvider;

public class KnockoutTemplateReferenceRegistrarTest extends ReferenceJsFixtureTestCase {
    private static final String FIXTURE_PATH = "component.js";

    /**
     * Component template declarations should reference Knockout template files.
     */
    public void testTemplatePropertyMustHaveReference() {
        myFixture.configureByFile(getFixturePath(FIXTURE_PATH));

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/template.html",
                KnockoutTemplateReferenceProvider.class
        );
    }

    /**
     * Template shorthand properties should reference Knockout template files.
     */
    public void testTmplPropertyMustHaveReference() {
        myFixture.configureByFile(getFixturePath(FIXTURE_PATH));

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/template2.html",
                KnockoutTemplateReferenceProvider.class
        );
    }
}
