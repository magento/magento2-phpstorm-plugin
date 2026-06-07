/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.js;

import com.magento.idea.magento2plugin.reference.provider.KnockoutRegionReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.KnockoutTemplateReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.KnockoutTemplateUsageReferenceProvider;

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
     * Parent template region declarations should resolve displayArea declarations in layout XML.
     */
    public void testGetRegionMustHaveReferenceToLayoutXmlDisplayArea() {
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
                "app/code/Foo/Bar/view/frontend/layout/checkout_index_index.xml",
                KnockoutRegionReferenceProvider.class
        );
        assertHasNoReferenceToFile(
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

    /**
     * XML displayArea declarations should resolve only getRegion usages in their parent template.
     */
    public void testXmlDisplayAreaMustHaveScopedReferenceToGetRegion() {
        addTemplate("scoped-parent-one", "<!-- ko foreach: getRegion('scopedXmlRegion') -->\n<!-- /ko -->");
        addTemplate("scoped-parent-two", "<!-- ko foreach: getRegion('scopedXmlRegion') -->\n<!-- /ko -->");
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/layout/scoped_one.xml",
                layoutXml("one", "Foo_Bar/template/scoped-parent-one", "scopedXml<caret>Region")
        );
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/layout/scoped_two.xml",
                layoutXml("two", "Foo_Bar/template/scoped-parent-two", "scopedXmlRegion")
        );
        myFixture.configureFromTempProjectFile("app/code/Foo/Bar/view/frontend/layout/scoped_one.xml");

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/scoped-parent-one.html",
                KnockoutRegionReferenceProvider.class
        );
        assertHasNoReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/scoped-parent-two.html",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * getRegion usages should resolve XML displayArea declarations in the owning component scope only.
     */
    public void testGetRegionMustHaveScopedReferenceToXmlDisplayArea() {
        addTemplate("scoped-parent-one", "<!-- ko foreach: getRegion('scopedXml<caret>Region') -->\n<!-- /ko -->");
        addTemplate("scoped-parent-two", "<!-- ko foreach: getRegion('scopedXmlRegion') -->\n<!-- /ko -->");
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/layout/scoped_one.xml",
                layoutXml("one", "Foo_Bar/template/scoped-parent-one", "scopedXmlRegion")
        );
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/layout/scoped_two.xml",
                layoutXml("two", "Foo_Bar/template/scoped-parent-two", "scopedXmlRegion")
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/scoped-parent-one.html"
        );

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/layout/scoped_one.xml",
                KnockoutRegionReferenceProvider.class
        );
        assertHasNoReferenceToFile(
                "app/code/Foo/Bar/view/frontend/layout/scoped_two.xml",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * JavaScript configuration displayArea declarations should resolve parent template regions.
     */
    public void testJsConfigDisplayAreaMustHaveScopedReferenceToGetRegion() {
        addTemplate("js-config-parent-one", "<!-- ko foreach: getRegion('jsConfigRegion') -->\n<!-- /ko -->");
        addTemplate("js-config-parent-two", "<!-- ko foreach: getRegion('jsConfigRegion') -->\n<!-- /ko -->");
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/scoped-config.js",
                "var config = {\n"
                        + "    components: {\n"
                        + "        parentOne: {\n"
                        + "            template: 'Foo_Bar/template/js-config-parent-one',\n"
                        + "            children: {\n"
                        + "                childOne: { displayArea: 'jsConfig<caret>Region' }\n"
                        + "            }\n"
                        + "        },\n"
                        + "        parentTwo: {\n"
                        + "            template: 'Foo_Bar/template/js-config-parent-two',\n"
                        + "            children: {\n"
                        + "                childTwo: { displayArea: 'jsConfigRegion' }\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }\n"
                        + "};"
        );
        myFixture.configureFromTempProjectFile("app/code/Foo/Bar/view/frontend/web/js/scoped-config.js");

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/js-config-parent-one.html",
                KnockoutRegionReferenceProvider.class
        );
        assertHasNoReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/js-config-parent-two.html",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * getRegion usages should resolve JavaScript configuration displayArea declarations.
     */
    public void testGetRegionMustHaveScopedReferenceToJsDisplayArea() {
        addTemplate("js-config-parent-one", "<!-- ko foreach: getRegion('jsConfig<caret>Region') -->\n<!-- /ko -->");
        addTemplate("js-config-parent-two", "<!-- ko foreach: getRegion('jsConfigRegion') -->\n<!-- /ko -->");
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/scoped-config.js",
                "var config = {\n"
                        + "    components: {\n"
                        + "        parentOne: {\n"
                        + "            template: 'Foo_Bar/template/js-config-parent-one',\n"
                        + "            children: { childOne: { displayArea: 'jsConfigRegion' } }\n"
                        + "        },\n"
                        + "        parentTwo: {\n"
                        + "            template: 'Foo_Bar/template/js-config-parent-two',\n"
                        + "            children: { childTwo: { displayArea: 'jsConfigRegion' } }\n"
                        + "        }\n"
                        + "    }\n"
                        + "};"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/js-config-parent-one.html"
        );

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/js/scoped-config.js",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * childTemplate declarations should reference Knockout template files.
     */
    public void testChildTemplatePropertyMustHaveReference() {
        addTemplate("child-template-target", "<span>child</span>");
        myFixture.configureByText(
                "child-template-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    return Component.extend({\n"
                        + "        defaults: { childTemplate: 'Foo_Bar/template/child-template-<caret>target' }\n"
                        + "    });\n"
                        + "});"
        );

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/child-template-target.html",
                KnockoutTemplateReferenceProvider.class
        );
    }

    /**
     * JavaScript templates collection entries should reference Knockout template files.
     */
    public void testTemplatesCollectionPropertyMustHaveReference() {
        addTemplate("templates-collection-target", "<span>collection</span>");
        myFixture.configureByText(
                "templates-collection-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            templates: { custom: 'Foo_Bar/template/templates-collection-<caret>target' }\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/templates-collection-target.html",
                KnockoutTemplateReferenceProvider.class
        );
    }

    /**
     * Knockout template: childTemplate usages should navigate back to childTemplate declarations.
     */
    public void testTemplateChildTemplateUsageMustHaveReferenceToDeclaration() {
        addTemplate("owner-child-template", "<span>child</span>");
        addTemplate("owner-template", "<!-- ko template: child<caret>Template --><!-- /ko -->");
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/template-usage-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            template: 'Foo_Bar/template/owner-template',\n"
                        + "            childTemplate: 'Foo_Bar/template/owner-child-template'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.configureFromTempProjectFile("app/code/Foo/Bar/view/frontend/web/template/owner-template.html");

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/js/template-usage-component.js",
                KnockoutTemplateUsageReferenceProvider.class
        );
    }

    /**
     * Named getTemplate usages should navigate back to matching templates collection declarations.
     */
    public void testNamedGetTemplateUsageMustHaveReferenceToTemplatesDeclaration() {
        addTemplate("named-owner-template", "<!-- ko template: getTemplate('cus<caret>tom') --><!-- /ko -->");
        addTemplate("named-template-target", "<span>named</span>");
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/named-template-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            template: 'Foo_Bar/template/named-owner-template',\n"
                        + "            templates: { custom: 'Foo_Bar/template/named-template-target' }\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.configureFromTempProjectFile("app/code/Foo/Bar/view/frontend/web/template/named-owner-template.html");

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/js/named-template-component.js",
                KnockoutTemplateUsageReferenceProvider.class
        );
    }

    private void addTemplate(final String name, final String content) {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/" + name + ".html",
                content
        );
    }

    private String layoutXml(
            final String rootName,
            final String parentTemplate,
            final String displayArea
    ) {
        return "<?xml version=\"1.0\"?>\n"
                + "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "    <body>\n"
                + "        <referenceBlock name=\"checkout.root\">\n"
                + "            <arguments>\n"
                + "                <argument name=\"jsLayout\" xsi:type=\"array\">\n"
                + "                    <item name=\"components\" xsi:type=\"array\">\n"
                + "                        <item name=\"" + rootName + "\" xsi:type=\"array\">\n"
                + "                            <item name=\"template\" xsi:type=\"string\">"
                + parentTemplate + "</item>\n"
                + "                            <item name=\"children\" xsi:type=\"array\">\n"
                + "                                <item name=\"child\" xsi:type=\"array\">\n"
                + "                                    <item name=\"displayArea\" xsi:type=\"string\">"
                + displayArea + "</item>\n"
                + "                                </item>\n"
                + "                            </item>\n"
                + "                        </item>\n"
                + "                    </item>\n"
                + "                </argument>\n"
                + "            </arguments>\n"
                + "        </referenceBlock>\n"
                + "    </body>\n"
                + "</page>";
    }
}
