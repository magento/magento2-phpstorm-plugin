/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;
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
     * Parent Knockout template regions should navigate to child component templates.
     */
    public void testParentTemplateShouldHaveChildTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/parent.html"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to child Knockout templates", "");
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

        assertLinemarkerCountWithTooltip("Navigate to child Knockout templates", 1);
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

        assertLinemarkerCountWithTooltip("Navigate to child Knockout templates", 1);
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
     * Layout XML display areas should navigate back to parent templates.
     */
    public void testXmlDisplayAreaShouldHaveParentTemplateLinemarker() {
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

        assertHasLinemarkerWithTooltipAndIcon("Navigate to region templates", "");
    }

    /**
     * PHP jsLayout display areas should navigate back to parent templates.
     */
    public void testPhpDisplayAreaShouldHaveParentTemplateLinemarker() {
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

        assertProviderCollectsSlowLinemarker("Navigate to region templates");
    }

    /**
     * Layout XML template declarations should navigate to Knockout template files.
     */
    public void testXmlTemplateDeclarationShouldHaveTemplateLinemarker() {
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

        assertHasLinemarkerWithTooltipAndIcon("Navigate to Knockout template", "");
    }

    /**
     * PHP jsLayout template declarations should navigate to Knockout template files.
     */
    public void testPhpTemplateDeclarationShouldHaveTemplateLinemarker() {
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

        assertProviderCollectsSlowLinemarker("Navigate to Knockout template");
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
