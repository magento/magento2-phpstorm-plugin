/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.linemarker.LinemarkerFixtureTestCase;

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
     * Child component display areas should navigate back to parent templates.
     */
    public void testDisplayAreaShouldHaveParentTemplateLinemarker() {
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/js/knockout-child.js"
        );

        assertHasLinemarkerWithTooltipAndIcon("Navigate to region templates", "");
    }

    private void assertProviderHasLinemarker(final String tooltip) {
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());
        final LineMarkerInfo<?> lineMarker = new KnockoutTemplateLineMarkerProvider()
                .getLineMarkerInfo(anchor);

        assertNotNull("No line marker returned by provider", lineMarker);
        assertEquals(tooltip, lineMarker.getLineMarkerTooltip());
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
}
