/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.js;

import com.magento.idea.magento2plugin.reference.provider.KnockoutRegionReferenceProvider;
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

    /**
     * Parent template region declarations should reference matching child components.
     */
    public void testGetRegionMustHaveReference() {
        myFixture.configureByFile(getFixturePath("component.html"));

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/js/knockout-child.js",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * Child component display areas should reference matching parent template regions.
     */
    public void testDisplayAreaMustHaveReference() {
        myFixture.configureByFile(getFixturePath(FIXTURE_PATH));

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/parent.html",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * Parent template region declarations should resolve matching child components from vendor modules.
     */
    public void testGetRegionMustHaveReferenceToVendorComponent() {
        myFixture.addFileToProject(
                "vendor/magento/module-checkout/view/frontend/web/js/vendor-knockout-child.js",
                "define([], function () {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            displayArea: 'vendorChildRegion'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.configureByText(
                "vendor-parent.html",
                "<!-- ko foreach: getRegion('vendorChild<caret>Region') -->\n"
                        + "<!-- /ko -->"
        );

        assertHasReferenceToFile(
                "vendor/magento/module-checkout/view/frontend/web/js/vendor-knockout-child.js",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * Parent template region declarations should resolve child components declared in layout XML.
     */
    public void testGetRegionMustHaveReferenceToLayoutXmlComponent() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/layout-knockout-child.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({});\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/layout/checkout_index_index.xml",
                "<?xml version=\"1.0\"?>\n"
                        + "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <body>\n"
                        + "        <referenceBlock name=\"checkout.root\">\n"
                        + "            <arguments>\n"
                        + "                <argument name=\"jsLayout\" xsi:type=\"array\">\n"
                        + "                    <item name=\"components\" xsi:type=\"array\">\n"
                        + "                        <item name=\"child\" xsi:type=\"array\">\n"
                        + "                            <item name=\"component\" xsi:type=\"string\">"
                        + "Foo_Bar/js/layout-knockout-child</item>\n"
                        + "                            <item name=\"displayArea\" xsi:type=\"string\">"
                        + "xmlChildRegion</item>\n"
                        + "                        </item>\n"
                        + "                    </item>\n"
                        + "                </argument>\n"
                        + "            </arguments>\n"
                        + "        </referenceBlock>\n"
                        + "    </body>\n"
                        + "</page>"
        );
        myFixture.configureByText(
                "xml-parent.html",
                "<!-- ko foreach: getRegion('xmlChild<caret>Region') -->\n"
                        + "<!-- /ko -->"
        );

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/js/layout-knockout-child.js",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * Child component display areas should resolve matching parent template regions from vendor modules.
     */
    public void testDisplayAreaMustHaveReferenceToVendorTemplate() {
        myFixture.addFileToProject(
                "vendor/magento/module-checkout/view/frontend/web/template/vendor-parent.html",
                "<!-- ko foreach: getRegion('vendorDisplayRegion') -->\n"
                        + "<!-- /ko -->"
        );
        myFixture.configureByText(
                "vendor-component.js",
                "define([], function () {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            displayArea: 'vendorDisplay<caret>Region'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );

        assertHasReferenceToFile(
                "vendor/magento/module-checkout/view/frontend/web/template/vendor-parent.html",
                KnockoutRegionReferenceProvider.class
        );
    }
}
