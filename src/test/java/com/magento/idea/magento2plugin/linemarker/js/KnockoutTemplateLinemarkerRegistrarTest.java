/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnockoutTemplateLinemarkerRegistrarTest extends LinemarkerFixtureTestCase {
    /**
     * Knockout component JS files should navigate to their declared templates.
     */
    public void testComponentJsShouldHaveTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/knockout-component.js"
        );

        assertLinemarkerCountWithTooltip("Navigate to Knockout template", 1);
        assertHasLinemarkerWithTooltipAndIcon("Navigate to Knockout template", "");
        assertProviderHasLinemarker("Navigate to Knockout template");
    }

    /**
     * Knockout template files should navigate back to declaring component JS files.
     */
    public void testTemplateShouldHaveComponentLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/template.html"
        );

        assertLinemarkerCountWithTooltip("Navigate to Knockout component", 1);
        assertHasLinemarkerWithTooltipAndIcon("Navigate to Knockout component", "");
        assertProviderHasLinemarker("Navigate to Knockout component");
    }

    /**
     * Knockout templates should navigate back to JS components declared together with templates in layout XML.
     */
    public void testTemplateShouldHaveComponentFromLayoutXmlLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/xml-template-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({});\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/xml-template-child.html",
                "<span>xml template child</span>"
        );
        addLayoutXmlRegion(
                "xmlTemplateRegion",
                "Foo_Bar/js/xml-template-component",
                "Foo_Bar/template/xml-template-child"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/xml-template-child.html"
        );

        assertLinemarkerCountWithTooltip("Navigate to Knockout component", 1);
    }

    /**
     * XML-declared templates should navigate back to components even without module root index data.
     */
    public void testTemplateShouldHaveComponentFromNestedLayoutXmlPathFallbackLinemarker() {
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/js/view/shipping-methods/shipment.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({});\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/template/shipping-methods/shipment.html",
                "<span>shipment child</span>"
        );
        addNestedLayoutXmlRegion(
                "shipments",
                "No_Index/js/view/shipping-methods/shipment",
                "No_Index/shipping-methods/shipment"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/No/Index/view/frontend/web/template/shipping-methods/shipment.html"
        );

        assertLinemarkerCountWithTooltip("Navigate to Knockout component", 1);
    }

    /**
     * Knockout templates should navigate back to JS components declared together with templates in PHP jsLayout.
     */
    public void testTemplateShouldHaveComponentFromPhpLayoutLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/php-template-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({});\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/php-template-child.html",
                "<span>php template child</span>"
        );
        addPhpLayoutRegion(
                "phpTemplateRegion",
                "Foo_Bar/js/php-template-component",
                "Foo_Bar/template/php-template-child"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/php-template-child.html"
        );

        assertLinemarkerCountWithTooltip("Navigate to Knockout component", 1);
    }

    /**
     * Template files should compute RequireJS aliases from app/code paths even if the module index is missing.
     */
    public void testTemplateShouldHaveComponentLinemarkerFromPathFallback() {
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/js/path-fallback-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            template: 'No_Index/path-fallback-template'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/template/path-fallback-template.html",
                "<span>path fallback template</span>"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/No/Index/view/frontend/web/template/path-fallback-template.html"
        );

        assertLinemarkerCountWithTooltip("Navigate to Knockout component", 1);
    }

    /**
     * Template files should navigate back to JS components even when Magento VFS roots are unavailable.
     */
    public void testTemplateShouldHaveComponentLinemarkerFromProjectJsFallback() {
        Settings.getInstance(myFixture.getProject()).magentoPath = "/missing-magento-root";
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/js/project-js-fallback-component.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            template: 'No_Index/project-js-fallback-template'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/template/project-js-fallback-template.html",
                "<span>project js fallback template</span>"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/No/Index/view/frontend/web/template/project-js-fallback-template.html"
        );

        assertLinemarkerCountWithTooltip("Navigate to Knockout component", 1);
    }

    /**
     * Parent Knockout template regions should navigate to child component templates.
     */
    public void testParentTemplateShouldHaveChildTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/parent.html"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to displayArea declaration", "");
    }

    /**
     * Parent Knockout template regions should navigate to child templates declared by layout XML components.
     */
    public void testParentTemplateShouldHaveChildTemplateFromLayoutXmlComponentLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/js/layout-knockout-child.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            template: 'Foo_Bar/template/layout-child'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/layout-child.html",
                "<span>layout child</span>"
        );
        addLayoutXmlRegion(
                "layoutComponentRegion",
                "Foo_Bar/js/layout-knockout-child",
                null
        );
        myFixture.configureByText(
                "layout-parent.html",
                "<!-- ko foreach: getRegion('layoutComponentRegion') -->\n"
                        + "<!-- /ko -->"
        );

        assertLinemarkerCountWithTooltip("Navigate to displayArea declaration", 1);
    }

    /**
     * Parent Knockout template regions should navigate to child templates declared directly in layout XML.
     */
    public void testParentTemplateShouldHaveChildTemplateFromLayoutXmlConfigLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/config-child.html",
                "<span>config child</span>"
        );
        addLayoutXmlRegion(
                "layoutConfigRegion",
                "uiComponent",
                "Foo_Bar/template/config-child"
        );
        myFixture.configureByText(
                "layout-config-parent.html",
                "<!-- ko foreach: getRegion('layoutConfigRegion') -->\n"
                        + "<!-- /ko -->"
        );

        assertLinemarkerCountWithTooltip("Navigate to displayArea declaration", 1);
    }

    /**
     * Parent templates should support Magento's <each args="getRegion(...)"> syntax.
     */
    public void testEachArgsGetRegionShouldHaveChildTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/each-child.html",
                "<span>each child</span>"
        );
        addLayoutXmlRegion(
                "eachRegion",
                "uiComponent",
                "Foo_Bar/template/each-child"
        );
        myFixture.configureByText(
                "each-parent.html",
                "<each args=\"getRegion('eachRegion')\" render=\"\" />"
        );

        assertLinemarkerCountWithTooltip("Navigate to displayArea declaration", 1);
    }

    /**
     * getRegion should navigate to layout-declared templates even without module root index data.
     */
    public void testGetRegionShouldHaveLayoutChildTemplateFromPathFallback() {
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/template/shipping-methods/shipment.html",
                "<span>shipment child</span>"
        );
        addLayoutXmlRegion(
                "shipments",
                "uiComponent",
                "No_Index/shipping-methods/shipment"
        );
        myFixture.configureByText(
                "shipping-methods.html",
                "<!-- ko foreach: { data: $parent.getRegion('shipments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->"
        );

        assertLinemarkerCountWithTooltip("Navigate to displayArea declaration", 1);
    }

    /**
     * Nested checkout jsLayout XML declarations should wire getRegion to direct multiline template items.
     */
    public void testGetRegionShouldHaveNestedLayoutXmlDirectTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/web/template/shipping-methods/shipment.html",
                "<span>shipment child</span>"
        );
        addNestedLayoutXmlRegion(
                "shipments",
                "uiComponent",
                "No_Index/shipping-methods/shipment"
        );
        myFixture.configureByText(
                "shipping-methods.html",
                "<!-- ko foreach: { data: $parent.getRegion('shipments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->"
        );

        assertLinemarkerCountWithTooltip("Navigate to displayArea declaration", 1);
    }

    /**
     * Parent templates should support getRegion calls through renderer aliases.
     */
    public void testRendererGetRegionShouldHaveChildTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/renderer-child.html",
                "<span>renderer child</span>"
        );
        addLayoutXmlRegion(
                "rendererRegion",
                "uiComponent",
                "Foo_Bar/template/renderer-child"
        );
        myFixture.configureByText(
                "renderer-parent.html",
                "<!-- ko foreach: { data: renderer.getRegion('rendererRegion'), as: 'childRenderer' } -->\n"
                        + "<!-- /ko -->"
        );

        assertLinemarkerCountWithTooltip("Navigate to displayArea declaration", 1);
    }

    /**
     * Encoded quotes in <each args="..."> should still resolve getRegion targets.
     */
    public void testEachArgsGetRegionWithEncodedQuotesShouldHaveChildTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/encoded-each-child.html",
                "<span>encoded each child</span>"
        );
        addLayoutXmlRegion(
                "encodedEachRegion",
                "uiComponent",
                "Foo_Bar/template/encoded-each-child"
        );
        myFixture.configureByText(
                "encoded-each-parent.html",
                "<each args=\"getRegion(&quot;encodedEachRegion&quot;)\" render=\"\" />"
        );

        assertLinemarkerCountWithTooltip("Navigate to displayArea declaration", 1);
    }

    /**
     * Parent Knockout template regions should navigate to child templates declared directly in PHP jsLayout.
     */
    public void testParentTemplateShouldHaveChildTemplateFromPhpLayoutConfigLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/php-config-child.html",
                "<span>php config child</span>"
        );
        addPhpLayoutRegion(
                "phpConfigRegion",
                "uiComponent",
                "Foo_Bar/template/php-config-child"
        );
        myFixture.configureByText(
                "php-layout-parent.html",
                "<!-- ko foreach: getRegion('phpConfigRegion') -->\n"
                        + "<!-- /ko -->"
        );

        assertLinemarkerCountWithTooltip("Navigate to child Knockout templates", 1);
    }

    /**
     * Child component display areas should navigate back to parent templates.
     */
    public void testDisplayAreaShouldHaveParentTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/knockout-child.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to region templates", "");
    }

    /**
     * Layout XML display areas should not expose Knockout gutter markers.
     */
    public void testXmlDisplayAreaShouldNotHaveParentTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/xml-display-parent.html",
                "<!-- ko foreach: getRegion('xmlDisplayRegion') -->\n"
                        + "<!-- /ko -->"
        );
        addLayoutXmlRegion(
                "xmlDisplayRegion",
                "uiComponent",
                null
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/layout/checkout_index_index.xml"
        );

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to region templates", "");
        assertProviderHasNoLinemarker();
    }

    /**
     * Layout XML display areas should not expose Knockout gutter markers for <each args="getRegion(...)">.
     */
    public void testXmlDisplayAreaShouldNotHaveEachArgsParentTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/xml-display-each-parent.html",
                "<each args=\"getRegion('xmlDisplayEachRegion')\" render=\"\" />"
        );
        addLayoutXmlRegion(
                "xmlDisplayEachRegion",
                "uiComponent",
                null
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/layout/checkout_index_index.xml"
        );

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to region templates", "");
        assertProviderHasNoLinemarker();
    }

    /**
     * PHP jsLayout display areas should not expose Knockout gutter markers.
     */
    public void testPhpDisplayAreaShouldNotHaveParentTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/php-display-parent.html",
                "<!-- ko foreach: getRegion('phpDisplayRegion') -->\n"
                        + "<!-- /ko -->"
        );
        addPhpLayoutRegion(
                "phpDisplayRegion",
                "uiComponent",
                null
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/Block/PhpLayoutProvider.php"
        );

        assertProviderDoesNotCollectSlowLinemarker("Navigate to region templates");
        assertProviderHasNoLinemarker();
    }

    /**
     * Layout XML template declarations should use references, not Knockout gutter markers.
     */
    public void testXmlTemplateDeclarationShouldNotHaveTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/xml-declared-template.html",
                "<span>xml declared template</span>"
        );
        addLayoutXmlRegion(
                "xmlDeclaredTemplateRegion",
                "uiComponent",
                "Foo_Bar/template/xml-declared-template"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/layout/checkout_index_index.xml"
        );

        assertHasNoLinemarkerWithTooltipAndIcon("Navigate to Knockout template", "");
        assertProviderHasNoLinemarker();
    }

    /**
     * PHP jsLayout template declarations should not expose Knockout gutter markers.
     */
    public void testPhpTemplateDeclarationShouldNotHaveTemplateLinemarker() {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/php-declared-template.html",
                "<span>php declared template</span>"
        );
        addPhpLayoutRegion(
                "phpDeclaredTemplateRegion",
                "uiComponent",
                "Foo_Bar/template/php-declared-template"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/Block/PhpLayoutProvider.php"
        );

        assertProviderDoesNotCollectSlowLinemarker("Navigate to Knockout template");
        assertProviderHasNoLinemarker();
    }

    private void assertProviderHasLinemarker(final String tooltip) {
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());
        final LineMarkerInfo<?> lineMarker = new KnockoutTemplateLineMarkerProvider()
                .getLineMarkerInfo(anchor);

        assertNotNull("No line marker returned by provider", lineMarker);
        assertEquals(tooltip, lineMarker.getLineMarkerTooltip());
    }

    private void assertProviderCollectsSlowLinemarker(final String tooltip) {
        final List<LineMarkerInfo<?>> lineMarkers = new ArrayList<>();

        new KnockoutTemplateLineMarkerProvider().collectSlowLineMarkers(
                Arrays.asList(PsiTreeUtil.collectElements(myFixture.getFile(), ignored -> true)),
                lineMarkers
        );

        for (final LineMarkerInfo<?> lineMarker : lineMarkers) {
            if (tooltip.equals(lineMarker.getLineMarkerTooltip())) {
                return;
            }
        }
        fail("No slow line marker with tooltip `" + tooltip + "` found");
    }

    private void assertProviderDoesNotCollectSlowLinemarker(final String tooltip) {
        final List<LineMarkerInfo<?>> lineMarkers = new ArrayList<>();

        new KnockoutTemplateLineMarkerProvider().collectSlowLineMarkers(
                Arrays.asList(PsiTreeUtil.collectElements(myFixture.getFile(), ignored -> true)),
                lineMarkers
        );

        for (final LineMarkerInfo<?> lineMarker : lineMarkers) {
            if (tooltip.equals(lineMarker.getLineMarkerTooltip())) {
                fail("Unexpected slow line marker with tooltip `" + tooltip + "` found");
            }
        }
    }

    private void assertProviderHasNoLinemarker() {
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());
        final LineMarkerInfo<?> lineMarker = new KnockoutTemplateLineMarkerProvider()
                .getLineMarkerInfo(anchor);

        assertNull("Unexpected line marker returned by provider", lineMarker);
    }

    private void addLayoutXmlRegion(
            final String displayArea,
            final String component,
            final String template
    ) {
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
                        + component + "</item>\n"
                        + "                            <item name=\"displayArea\" xsi:type=\"string\">"
                        + displayArea + "</item>\n"
                        + getTemplateXml(template)
                        + "                        </item>\n"
                        + "                    </item>\n"
                        + "                </argument>\n"
                        + "            </arguments>\n"
                        + "        </referenceBlock>\n"
                        + "    </body>\n"
                        + "</page>"
        );
    }

    private void addNestedLayoutXmlRegion(
            final String displayArea,
            final String component,
            final String template
    ) {
        myFixture.addFileToProject(
                "app/code/No/Index/view/frontend/layout/checkout_index_index.xml",
                "<?xml version=\"1.0\"?>\n"
                        + "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <body>\n"
                        + "        <referenceBlock name=\"checkout.root\">\n"
                        + "            <arguments>\n"
                        + "                <argument name=\"jsLayout\" xsi:type=\"array\">\n"
                        + "                    <item name=\"components\" xsi:type=\"array\">\n"
                        + "                        <item name=\"checkout\" xsi:type=\"array\">\n"
                        + "                            <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                <item name=\"shipping-step\" xsi:type=\"array\">\n"
                        + "                                    <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                        <item name=\"shippingAddress\" xsi:type=\"array\">\n"
                        + "                                            <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                                <item name=\"shipping-methods\" xsi:type=\"array\">\n"
                        + "                                                    <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                                        <item name=\"shipment\" xsi:type=\"array\">\n"
                        + "                                                            <item name=\"component\" xsi:type=\"string\">\n"
                        + "                                                                " + component + "\n"
                        + "                                                            </item>\n"
                        + "                                                            <item name=\"displayArea\" xsi:type=\"string\">"
                        + displayArea + "</item>\n"
                        + "                                                            <item name=\"template\" xsi:type=\"string\">\n"
                        + "                                                                " + template + "\n"
                        + "                                                            </item>\n"
                        + "                                                        </item>\n"
                        + "                                                    </item>\n"
                        + "                                                </item>\n"
                        + "                                            </item>\n"
                        + "                                        </item>\n"
                        + "                                    </item>\n"
                        + "                                </item>\n"
                        + "                            </item>\n"
                        + "                        </item>\n"
                        + "                    </item>\n"
                        + "                </argument>\n"
                        + "            </arguments>\n"
                        + "        </referenceBlock>\n"
                        + "    </body>\n"
                        + "</page>"
        );
    }

    private String getTemplateXml(final String template) {
        if (template == null) {
            return "";
        }

        return "                            <item name=\"config\" xsi:type=\"array\">\n"
                + "                                <item name=\"template\" xsi:type=\"string\">"
                + template + "</item>\n"
                + "                            </item>\n";
    }

    private void addPhpLayoutRegion(
            final String displayArea,
            final String component,
            final String template
    ) {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/Block/PhpLayoutProvider.php",
                "<?php\n"
                        + "$jsLayout = [\n"
                        + "    'components' => [\n"
                        + "        'child' => [\n"
                        + "            'component' => '" + component + "',\n"
                        + "            'displayArea' => '" + displayArea + "'"
                        + getTemplatePhp(template)
                        + "\n"
                        + "        ],\n"
                        + "    ],\n"
                        + "];\n"
        );
    }

    private String getTemplatePhp(final String template) {
        if (template == null) {
            return "";
        }

        return ",\n"
                + "            'template' => '" + template + "'";
    }
}
