/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.js;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.navigation.KnockoutRegionGotoDeclarationHandler;
import com.magento.idea.magento2plugin.reference.provider.KnockoutRegionReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.KnockoutTemplateReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.KnockoutTemplateUsageReferenceProvider;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;

public class KnockoutTemplateReferenceRegistrarTest extends ReferenceJsFixtureTestCase {
    private static final String FIXTURE_PATH = "component.js";

    /**
     * Embedded JavaScript in PHP templates must not make region reference discovery inspect the wrapping HTML tag.
     */
    public void testPhpTemplateEmbeddedJavaScriptMustNotFailRegionReferenceDiscovery() {
        myFixture.configureByText(
                "old-product_list.phtml",
                "<div class=\"qty_box\">\n"
                        + "    <button type=\"button\" class=\"qty_box__minus\"\n"
                        + "            onclick=\"var result = document.getElementById('qty"
                        + "<?= /* @noEscape */ $_product->getId() ?>.value; "
                        + "if (!isNaN(qty) && qty > 1) result.value--;return false;\">\n"
                        + "        <svg><path d=\"M14 2H0V0H14V2Z\"/></svg>\n"
                        + "    </button>\n"
                        + "    <input type=\"number\" id=\"qty"
                        + "<?= /* @noEscape */ $_product->getId() ?>\" value=\"1\" />\n"
                        + "    <button type=\"button\" class=\"qty_box__plus\"\n"
                        + "            onclick=\"var result = document.getElementById('qty"
                        + "<?= /* @noEscape */ $_product->getId() ?>'); var qty = result.value; "
                        + "if (!isNaN(qty)) result.value++;return false;\">\n"
                        + "        <svg><path d=\"M14 8H8V14H6V8H0V6H6V0H8V6H14V8Z\"/></svg>\n"
                        + "    </button>\n"
                        + "</div>"
        );

        myFixture.doHighlighting();
    }

    /**
     * Region references must use source offsets even when embedded PSI cannot reconstruct its text.
     */
    public void testGetRegionReferenceMustNotReconstructHostText() {
        final String prefix = "var qty = 1;\n";
        final String call = "getRegion('childRegion')";
        final PsiFile file = myFixture.configureByText("region.js", prefix + call + ";");
        final PsiElement host = new FakePsiElement() {
            @Override
            public PsiElement getParent() {
                return file;
            }

            @Override
            public Language getLanguage() {
                return JavascriptLanguage.INSTANCE;
            }

            @Override
            public TextRange getTextRange() {
                return TextRange.from(prefix.length(), call.length());
            }

            @Override
            public String getText() {
                throw new AssertionError("Embedded JavaScript text must not be reconstructed");
            }
        };
        final PsiReference[] references = new KnockoutRegionReferenceProvider()
                .getReferencesByElement(host, new ProcessingContext());

        assertTrue("Expected a region reference", references.length > 0);
        assertEquals(TextRange.from(0, call.length()), references[0].getRangeInElement());
        assertReferenceResolvesToFile(
                references[0],
                "app/code/Foo/Bar/view/frontend/web/js/knockout-child.js"
        );
    }

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
     * Go to Declaration from a JS template value should prefer the Knockout template file over generic module refs.
     */
    public void testTemplatePropertyEditorReferenceMustResolveTemplateFileFromModulePrefix() {
        addTemplate("quote-form/item", "<span>item</span>");
        myFixture.configureByText(
                "item.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            template: 'Foo<caret>_Bar/quote-form/item'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );

        final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());

        assertNotNull("Expected editor offset reference for template property", reference);
        assertReferenceResolvesToFile(
                reference,
                "app/code/Foo/Bar/view/frontend/web/template/quote-form/item.html"
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
     * Nested jsLayout component templates should resolve child display areas declared under their owning component.
     */
    public void testNestedShipmentTemplateRegionsMustResolveXmlDisplayAreas() {
        addCaryShipmentFixture(
                "<!-- ko foreach: { data: $parent.getRegion('ship<caret>ments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->",
                "<!-- ko foreach: { data: renderer.getRegion('items'), as: 'itemsRenderer' } -->\n"
                        + "<!-- /ko -->\n"
                        + "<!-- ko foreach: { data: renderer.getRegion('met<caret>hods'), as: 'methodsRenderer' } -->\n"
                        + "<!-- /ko -->"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods.html"
        );

        assertHasReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml",
                KnockoutRegionReferenceProvider.class
        );
        assertHasReferenceToFile("app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml");
        assertHasReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment.html",
                KnockoutRegionReferenceProvider.class
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment.html"
        );
        assertHasReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml",
                KnockoutRegionReferenceProvider.class
        );
        assertHasReferenceToFile("app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml");
        assertHasReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment/methods.html",
                KnockoutRegionReferenceProvider.class
        );
        assertHasNoReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/js/view/shipping-methods/shipment/items.js",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * Go to declaration from the getRegion call itself should use the same scoped displayArea target.
     */
    public void testNestedShipmentTemplateRegionCallMustResolveXmlDisplayAreas() {
        addCaryShipmentFixture(
                "<!-- ko foreach: { data: $parent.getRegion('shipments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->",
                "<!-- ko foreach: { data: renderer.getRegion('items'), as: 'itemsRenderer' } -->\n"
                        + "<!-- /ko -->\n"
                        + "<!-- ko foreach: { data: renderer.get<caret>Region('methods'), as: 'methodsRenderer' } -->\n"
                        + "<!-- /ko -->"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment.html"
        );

        assertHasReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml",
                KnockoutRegionReferenceProvider.class
        );
        assertHasReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment/methods.html",
                KnockoutRegionReferenceProvider.class
        );
        assertHasNoReferenceToFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/js/view/shipping-methods/shipment/items.js",
                KnockoutRegionReferenceProvider.class
        );
    }

    /**
     * Go to declaration should expose both the scoped XML displayArea declaration and child template target.
     */
    public void testGetRegionGotoDeclarationMustIncludeXmlDeclarationAndChildTemplate() {
        addCaryShipmentFixture(
                "<!-- ko foreach: { data: $parent.getRegion('shipments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->",
                "<!-- ko foreach: { data: renderer.getRegion('items'), as: 'itemsRenderer' } -->\n"
                        + "<!-- /ko -->\n"
                        + "<!-- ko foreach: { data: renderer.get<caret>Region('methods'), as: 'methodsRenderer' } -->\n"
                        + "<!-- /ko -->"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment.html"
        );

        assertGotoDeclarationTargetsFile(
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml"
        );
        assertGotoDeclarationTargetsFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment/methods.html"
        );
    }

    /**
     * Collection wrappers rendered through getRegion should expose templates from descendant child components.
     */
    public void testGetRegionGotoDeclarationMustIncludeDescendantCollectionChildTemplates() {
        addTemplate("collection-parent", "<!-- ko foreach: get<caret>Region('collectionRegion') -->\n<!-- /ko -->");
        addTemplate("field", "<span>field</span>");
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/element/select.html",
                "<select></select>"
        );
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/element/input.html",
                "<input />"
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
                        + "                        <item name=\"checkout\" xsi:type=\"array\">\n"
                        + "                            <item name=\"template\" xsi:type=\"string\">"
                        + "Foo_Bar/collection-parent</item>\n"
                        + "                            <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                <item name=\"collection\" xsi:type=\"array\">\n"
                        + "                                    <item name=\"component\" xsi:type=\"string\">uiCollection</item>\n"
                        + "                                    <item name=\"displayArea\" xsi:type=\"string\">"
                        + "collectionRegion</item>\n"
                        + "                                    <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                        <item name=\"select-field\" xsi:type=\"array\">\n"
                        + "                                            <item name=\"component\" xsi:type=\"string\">"
                        + "Magento_Ui/js/form/element/select</item>\n"
                        + "                                            <item name=\"config\" xsi:type=\"array\">\n"
                        + "                                                <item name=\"template\" xsi:type=\"string\">"
                        + "Foo_Bar/template/field</item>\n"
                        + "                                                <item name=\"elementTmpl\" xsi:type=\"string\">"
                        + "Foo_Bar/element/select</item>\n"
                        + "                                            </item>\n"
                        + "                                        </item>\n"
                        + "                                        <item name=\"input-field\" xsi:type=\"array\">\n"
                        + "                                            <item name=\"component\" xsi:type=\"string\">"
                        + "Magento_Ui/js/form/element/abstract</item>\n"
                        + "                                            <item name=\"config\" xsi:type=\"array\">\n"
                        + "                                                <item name=\"template\" xsi:type=\"string\">"
                        + "Foo_Bar/template/field</item>\n"
                        + "                                                <item name=\"elementTmpl\" xsi:type=\"string\">"
                        + "Foo_Bar/element/input</item>\n"
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
        myFixture.configureFromTempProjectFile(
                "app/code/Foo/Bar/view/frontend/web/template/collection-parent.html"
        );

        assertGotoDeclarationTargetsFile("app/code/Foo/Bar/view/frontend/layout/checkout_index_index.xml");
        assertGotoDeclarationTargetsFile("app/code/Foo/Bar/view/frontend/web/template/field.html");
        assertGotoDeclarationTargetsFile("app/code/Foo/Bar/view/frontend/web/template/element/select.html");
        assertGotoDeclarationTargetsFile("app/code/Foo/Bar/view/frontend/web/template/element/input.html");
    }

    /**
     * Go to declaration should expose scoped JavaScript displayArea declarations.
     */
    public void testGetRegionGotoDeclarationMustIncludeJsDisplayArea() {
        addTemplate("js-config-parent-one", "<!-- ko foreach: get<caret>Region('jsConfigRegion') -->\n<!-- /ko -->");
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

        assertGotoDeclarationTargetsFile("app/code/Foo/Bar/view/frontend/web/js/scoped-config.js");
        assertGotoDeclarationDoesNotTargetFile("app/code/Foo/Bar/view/frontend/web/template/js-config-parent-two.html");
    }

    /**
     * Editor offset lookup inside nested HTML must resolve the clicked getRegion call, not the whole wrapping tag.
     */
    public void testNestedShipmentTemplateRegionCallMustResolveFromEditorOffset() {
        addCaryShipmentFixture(
                "<!-- ko foreach: { data: $parent.getRegion('shipments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->",
                "<div class=\"cary-shq-shipment__content\">\n"
                        + "    <!-- ko foreach: { data: renderer.get<caret>Region('methods'), as: 'methodsRenderer' } -->\n"
                        + "    <!-- /ko -->\n"
                        + "</div>"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment.html"
        );

        final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());

        assertNotNull("Expected editor offset reference for getRegion", reference);
        assertReferenceResolvesToFile(
                reference,
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml"
        );
    }

    /**
     * The Cary shipment template structure should resolve from editor offset with multiple getRegion calls.
     */
    public void testRealShipmentTemplateRegionCallMustResolveFromEditorOffset() {
        addCaryShipmentFixture(
                "<!-- ko foreach: { data: $parent.getRegion('shipments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->",
                "<!--\n"
                        + "/**\n"
                        + " * @category Cary\n"
                        + " */\n"
                        + "-->\n"
                        + "<section class=\"cary-shq-shipment\"\n"
                        + "         data-bind=\"attr: {\n"
                        + "            'data-test-shipment-id': shipment.shipment_id || shipment.name || '',\n"
                        + "            'data-test-item-ids': renderer.getShipmentItemIds(shipment),\n"
                        + "            'data-test-skus': renderer.getShipmentSkus(shipment)\n"
                        + "         }\">\n"
                        + "    <header class=\"cary-shq-shipment__header\">\n"
                        + "        <strong class=\"cary-shq-shipment__title\">\n"
                        + "            <span data-bind=\"text: renderer.getShipmentTitle(shipment)\"></span>\n"
                        + "            <span class=\"cary-shq-shipment__identity\"\n"
                        + "                  data-bind=\"text: renderer.getShipmentItemIdentityText(shipment)\"></span>\n"
                        + "        </strong>\n"
                        + "    </header>\n"
                        + "\n"
                        + "    <div class=\"cary-shq-shipment__content\">\n"
                        + "        <!-- ko foreach: { data: renderer.getRegion('items'), as: 'itemsRenderer' } -->\n"
                        + "        <!-- /ko -->\n"
                        + "        <!-- ko foreach: { data: renderer.get<caret>Region('methods'), as: 'methodsRenderer' } -->\n"
                        + "        <!-- /ko -->\n"
                        + "    </div>\n"
                        + "</section>"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment.html"
        );

        final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());

        assertNotNull("Expected editor offset reference for getRegion", reference);
        assertReferenceResolvesToFile(
                reference,
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml"
        );
    }

    /**
     * Nested child templates should prefer their owning component scope over duplicate global region names.
     */
    public void testNestedShipmentItemsTemplateMustNotResolveDuplicateGlobalDisplayArea() {
        addCaryShipmentFixture(
                "<!-- ko foreach: { data: $parent.getRegion('shipments'), as: 'shipmentRenderer' } -->\n"
                        + "<!-- /ko -->",
                "<!-- ko foreach: { data: renderer.getRegion('items'), as: 'itemsRenderer' } -->\n"
                        + "<!-- /ko -->"
        );
        myFixture.addFileToProject(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment/items.html",
                "<!-- ko foreach: { data: $parent.renderer.get<caret>Region('item'), as: 'itemRenderer' } -->\n"
                        + "<!-- /ko -->"
        );
        myFixture.addFileToProject(
                "app/code/Cary/QuoteForm/view/frontend/layout/catalog_product_view.xml",
                "<?xml version=\"1.0\"?>\n"
                        + "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <body>\n"
                        + "        <referenceBlock name=\"content\">\n"
                        + "            <arguments>\n"
                        + "                <argument name=\"jsLayout\" xsi:type=\"array\">\n"
                        + "                    <item name=\"components\" xsi:type=\"array\">\n"
                        + "                        <item name=\"cary\" xsi:type=\"array\">\n"
                        + "                            <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                <item name=\"request\" xsi:type=\"array\">\n"
                        + "                                    <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                        <item name=\"quote\" xsi:type=\"array\">\n"
                        + "                                            <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                                <item name=\"popup\" xsi:type=\"array\">\n"
                        + "                                                    <item name=\"template\" xsi:type=\"string\">"
                        + "Cary_QuoteForm/quote-form/main</item>\n"
                        + "                                                    <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                                        <item name=\"item\" xsi:type=\"array\">\n"
                        + "                                                            <item name=\"displayArea\" xsi:type=\"string\">"
                        + "item</item>\n"
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
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment/items.html"
        );

        final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());

        assertNotNull("Expected editor offset reference for nested getRegion", reference);
        assertReferenceResolvesToFile(
                reference,
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml"
        );
        assertReferenceDoesNotResolveToFile(
                reference,
                "app/code/Cary/QuoteForm/view/frontend/layout/catalog_product_view.xml"
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
     * Dynamic uiLayout child configs with parent: this.name should resolve from the owner template.
     */
    public void testGetRegionMustResolveDynamicJsLayoutDisplayArea() {
        myFixture.addFileToProject(
                "app/code/Cary/QuoteForm/view/frontend/web/template/quote-form/items.html",
                "<!-- ko foreach: get<caret>Region('items-area') -->\n"
                        + "    <!-- ko template: getTemplate() --><!-- /ko -->\n"
                        + "<!-- /ko -->"
        );
        myFixture.addFileToProject(
                "app/code/Cary/QuoteForm/view/frontend/web/template/quote-form/item.html",
                "<tr><td>item</td></tr>"
        );
        myFixture.addFileToProject(
                "app/code/Cary/QuoteForm/view/frontend/web/js/quote-form/item.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    return Component.extend({\n"
                        + "        defaults: { template: 'Cary_QuoteForm/quote-form/item' }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/Cary/QuoteForm/view/frontend/web/js/quote-form/items.js",
                "define(['uiComponent', 'uiLayout'], function (Component, layout) {\n"
                        + "    return Component.extend({\n"
                        + "        defaults: { template: 'Cary_QuoteForm/quote-form/items' },\n"
                        + "        initialize: function () {\n"
                        + "            layout([{ parent: this.name, name: 'options', displayArea: "
                        + "'new-item-options-area', component: 'Cary_QuoteForm/js/quote-form/item' }]);\n"
                        + "        },\n"
                        + "        getItemConfig: function (item) {\n"
                        + "            return {\n"
                        + "                parent: this.name,\n"
                        + "                name: item.sku + '_' + Date.now(),\n"
                        + "                displayArea: 'items-area',\n"
                        + "                component: 'Cary_QuoteForm/js/quote-form/item'\n"
                        + "            };\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.configureFromTempProjectFile(
                "app/code/Cary/QuoteForm/view/frontend/web/template/quote-form/items.html"
        );

        assertGotoDeclarationTargetsFile("app/code/Cary/QuoteForm/view/frontend/web/js/quote-form/items.js");
        assertGotoDeclarationTargetsFile("app/code/Cary/QuoteForm/view/frontend/web/template/quote-form/item.html");
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

    /**
     * Non-UI jQuery widgets may have template option keys that are not Magento UI component templates.
     */
    public void testNonUiWidgetTemplateOptionsMustNotBeCollectedAsUiComponentDeclarations() {
        myFixture.configureByText(
                "dynamic-rows.js",
                "define(['jquery', 'mage/template'], function ($, mageTemplate) {\n"
                        + "    'use strict';\n"
                        + "    $.widget('mage.amFaqWidgetDynamicRows', {\n"
                        + "        tableBody: $(),\n"
                        + "        template: {},\n"
                        + "        options: {\n"
                        + "            templateSelector: '#dynamic-rows-template',\n"
                        + "            template: '',\n"
                        + "            rowsData: []\n"
                        + "        },\n"
                        + "        _create: function () {\n"
                        + "            this.template = this.options.template.empty() ? mageTemplate(this.options.templateSelector)\n"
                        + "                : mageTemplate(this.options.template);\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );

        assertTrue(UiComponentScopeResolver.getInstance()
                .collectComponentDeclarations(myFixture.getFile())
                .isEmpty());
    }

    private void addTemplate(final String name, final String content) {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/" + name + ".html",
                content
        );
    }

    private void addCaryShipmentFixture(
            final String shippingMethodsTemplateContent,
            final String shipmentTemplateContent
    ) {
        myFixture.addFileToProject(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods.html",
                shippingMethodsTemplateContent
        );
        myFixture.addFileToProject(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment.html",
                shipmentTemplateContent
        );
        myFixture.addFileToProject(
                "app/code/Cary/ShipperHQ/view/frontend/web/js/view/shipping-methods/shipment/methods.js",
                "define(['uiComponent'], function (Component) {\n"
                        + "    'use strict';\n"
                        + "    return Component.extend({\n"
                        + "        defaults: {\n"
                        + "            template: 'Cary_ShipperHQ/shipping-methods/shipment/methods'\n"
                        + "        }\n"
                        + "    });\n"
                        + "});"
        );
        myFixture.addFileToProject(
                "app/code/Cary/ShipperHQ/view/frontend/web/template/shipping-methods/shipment/methods.html",
                "<span>methods</span>"
        );
        myFixture.addFileToProject(
                "app/code/Cary/ShipperHQ/view/frontend/layout/checkout_index_index.xml",
                "<?xml version=\"1.0\"?>\n"
                        + "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <body>\n"
                        + "        <referenceBlock name=\"checkout.root\">\n"
                        + "            <arguments>\n"
                        + "                <argument name=\"jsLayout\" xsi:type=\"array\">\n"
                        + "                    <item name=\"components\" xsi:type=\"array\">\n"
                        + "                        <item name=\"checkout\" xsi:type=\"array\">\n"
                        + "                            <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                <item name=\"shipping-methods\" xsi:type=\"array\">\n"
                        + "                                    <item name=\"component\" xsi:type=\"string\">"
                        + "Cary_ShipperHQ/js/view/shipping-methods</item>\n"
                        + "                                    <item name=\"template\" xsi:type=\"string\">"
                        + "Cary_ShipperHQ/shipping-methods</item>\n"
                        + "                                    <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                        <item name=\"shipment\" xsi:type=\"array\">\n"
                        + "                                            <item name=\"component\" xsi:type=\"string\">"
                        + "Cary_ShipperHQ/js/view/shipping-methods/shipment</item>\n"
                        + "                                            <item name=\"displayArea\" xsi:type=\"string\">"
                        + "shipments</item>\n"
                        + "                                            <item name=\"template\" xsi:type=\"string\">"
                        + "Cary_ShipperHQ/shipping-methods/shipment</item>\n"
                        + "                                            <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                                <item name=\"items\" xsi:type=\"array\">\n"
                        + "                                                    <item name=\"component\" xsi:type=\"string\">"
                        + "Cary_ShipperHQ/js/view/shipping-methods/shipment/items</item>\n"
                        + "                                                    <item name=\"displayArea\" xsi:type=\"string\">"
                        + "items</item>\n"
                        + "                                                    <item name=\"template\" xsi:type=\"string\">"
                        + "Cary_ShipperHQ/shipping-methods/shipment/items</item>\n"
                        + "                                                    <item name=\"children\" xsi:type=\"array\">\n"
                        + "                                                        <item name=\"item\" xsi:type=\"array\">\n"
                        + "                                                            <item name=\"displayArea\" xsi:type=\"string\">"
                        + "item</item>\n"
                        + "                                                        </item>\n"
                        + "                                                    </item>\n"
                        + "                                                </item>\n"
                        + "                                                <item name=\"methods\" xsi:type=\"array\">\n"
                        + "                                                    <item name=\"component\" xsi:type=\"string\">"
                        + "Cary_ShipperHQ/js/view/shipping-methods/shipment/methods</item>\n"
                        + "                                                    <item name=\"displayArea\" xsi:type=\"string\">"
                        + "methods</item>\n"
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

    private void assertReferenceResolvesToFile(
            final PsiReference reference,
            final String expectedFilePath
    ) {
        if (reference instanceof PolyVariantReferenceBase) {
            for (final ResolveResult resolveResult : ((PolyVariantReferenceBase) reference).multiResolve(true)) {
                final PsiElement resolved = resolveResult.getElement();

                if (resolved != null
                        && resolved.getContainingFile() != null
                        && resolved.getContainingFile().getVirtualFile() != null
                        && resolved.getContainingFile().getVirtualFile().getPath().endsWith(expectedFilePath)) {
                    return;
                }
            }
        } else {
            final PsiElement resolved = reference.resolve();

            if (resolved != null
                    && resolved.getContainingFile() != null
                    && resolved.getContainingFile().getVirtualFile() != null
                    && resolved.getContainingFile().getVirtualFile().getPath().endsWith(expectedFilePath)) {
                return;
            }
        }
        fail("Expected reference to resolve to " + expectedFilePath);
    }

    private void assertReferenceDoesNotResolveToFile(
            final PsiReference reference,
            final String unexpectedFilePath
    ) {
        if (reference instanceof PolyVariantReferenceBase) {
            for (final ResolveResult resolveResult : ((PolyVariantReferenceBase) reference).multiResolve(true)) {
                final PsiElement resolved = resolveResult.getElement();

                if (resolved != null
                        && resolved.getContainingFile() != null
                        && resolved.getContainingFile().getVirtualFile() != null
                        && resolved.getContainingFile().getVirtualFile().getPath().endsWith(unexpectedFilePath)) {
                    fail("Expected reference not to resolve to " + unexpectedFilePath);
                }
            }
            return;
        }
        final PsiElement resolved = reference.resolve();

        if (resolved != null
                && resolved.getContainingFile() != null
                && resolved.getContainingFile().getVirtualFile() != null
                && resolved.getContainingFile().getVirtualFile().getPath().endsWith(unexpectedFilePath)) {
            fail("Expected reference not to resolve to " + unexpectedFilePath);
        }
    }

    private void assertGotoDeclarationTargetsFile(final String expectedFilePath) {
        final PsiElement[] targets = getGotoDeclarationTargets();

        for (final PsiElement target : targets) {
            if (target != null
                    && target.getContainingFile() != null
                    && target.getContainingFile().getVirtualFile() != null
                    && target.getContainingFile().getVirtualFile().getPath().endsWith(expectedFilePath)) {
                return;
            }
        }
        fail("Expected Go to Declaration target " + expectedFilePath);
    }

    private void assertGotoDeclarationDoesNotTargetFile(final String unexpectedFilePath) {
        final PsiElement[] targets = getGotoDeclarationTargets();

        for (final PsiElement target : targets) {
            if (target != null
                    && target.getContainingFile() != null
                    && target.getContainingFile().getVirtualFile() != null
                    && target.getContainingFile().getVirtualFile().getPath().endsWith(unexpectedFilePath)) {
                fail("Unexpected Go to Declaration target " + unexpectedFilePath);
            }
        }
    }

    private PsiElement[] getGotoDeclarationTargets() {
        final int offset = myFixture.getEditor().getCaretModel().getOffset();
        final PsiElement sourceElement = myFixture.getFile().findElementAt(offset);
        final PsiElement[] targets = new KnockoutRegionGotoDeclarationHandler().getGotoDeclarationTargets(
                sourceElement,
                offset,
                myFixture.getEditor()
        );

        return targets == null ? PsiElement.EMPTY_ARRAY : targets;
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
