/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import java.util.List;
import junit.framework.TestCase;

public class MagentoJsParserTest extends TestCase {
    public void testParsesRequireJsMapPathsAndMixins() {
        final String source = """
                var config = {
                    map: {
                        '*': {
                            checkoutComponent: 'Vendor_Module/js/checkout',
                            "aliasTwo": "Vendor_Module/js/alias-two"
                        }
                    },
                    paths: {
                        'ui/template': 'Magento_Ui/js/lib/knockout/template'
                    },
                    config: {
                        mixins: {
                            'Magento_Checkout/js/view/summary': {
                                'Vendor_Module/js/view/summary-mixin': true,
                                'Vendor_Module/js/disabled-mixin': false
                            }
                        }
                    }
                };
                """;

        final RequireJsConfig config = MagentoJsParser.parseRequireJsConfig(source);

        assertEquals(2, config.mapEntries().size());
        assertEquals("*", config.mapEntries().get(0).context());
        assertEquals("checkoutComponent", config.mapEntries().get(0).alias());
        assertEquals("Vendor_Module/js/checkout", config.mapEntries().get(0).target());

        assertEquals(1, config.pathEntries().size());
        assertEquals("ui/template", config.pathEntries().get(0).alias());
        assertEquals("Magento_Ui/js/lib/knockout/template", config.pathEntries().get(0).target());

        assertEquals(2, config.mixinEntries().size());
        assertTrue(config.mixinEntries().get(0).enabled());
        assertFalse(config.mixinEntries().get(1).enabled());
    }

    public void testParsesNestedObjectsMethodShorthandsCommentsAndRegexes() {
        final String source = """
                define(['uiComponent'], function (Component) {
                    'use strict';
                    // ignore template: 'Vendor_Module/fake'
                    /* block template: 'Vendor_Module/fake-block' */
                    var pattern = /foo\\/bar['":}]/gi;
                    return Component.extend({
                        defaults: {
                            template: 'Vendor_Module/main',
                            displayArea: 'shippingAddress'
                        },
                        initObservable() {
                            return {
                                inner: 'value'
                            };
                        }
                    });
                });
                """;

        final JsAst ast = MagentoJsParser.parse(source);
        final List<String> propertyNames = ast.getProperties().stream().map(JsProperty::getName).toList();

        assertTrue(propertyNames.contains("defaults"));
        assertTrue(propertyNames.contains("template"));
        assertTrue(propertyNames.contains("displayArea"));
        assertTrue(propertyNames.contains("initObservable"));
        assertTrue(propertyNames.contains("inner"));

        final JsProperty templateProp = ast.getProperties().stream()
                .filter(p -> "template".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("Vendor_Module/main", templateProp.getStringValue());
        assertEquals("defaults", templateProp.getParentProperty().getName());
    }

    public void testHandlesTrailingBackslashWithoutOutOfBounds() {
        final JsAst unclosedStringAst = MagentoJsParser.parse("var x = { a: 'abc\\");
        assertNotNull(unclosedStringAst);

        final JsAst unclosedRegexAst = MagentoJsParser.parse("var r = /abc\\");
        assertNotNull(unclosedRegexAst);
    }

    public void testRejectsDynamicExpressionsAsStringLiteralsWhileKeepingRawText() {
        final String source = """
                define(['uiComponent'], function (Component) {
                    return Component.extend({
                        defaults: {
                            template: this.template,
                            displayArea: regionName,
                            childTemplate: chooseTemplate(),
                            elementTmpl: 'Vendor_Module/' + name,
                            component: config.component,
                            enabled: true,
                            timeout: 500,
                            literal: 'Vendor_Module/static'
                        }
                    });
                });
                """;

        final JsAst ast = MagentoJsParser.parse(source);
        final KnockoutTemplatePathResolver templateResolver = KnockoutTemplatePathResolver.getInstance();
        final KnockoutRegionResolver regionResolver = KnockoutRegionResolver.getInstance();

        for (final JsProperty prop : ast.getProperties()) {
            switch (prop.getName()) {
                case "template", "childTemplate", "elementTmpl" -> {
                    assertNull("Dynamic expression must not be treated as string literal: " + prop.getName(),
                            prop.getStringValue());
                    assertNotNull(prop.getRawValueText());
                    assertNull(templateResolver.getTemplatePath(prop));
                }
                case "displayArea" -> {
                    assertNull(prop.getStringValue());
                    assertEquals("regionName", prop.getRawValueText());
                    assertNull(regionResolver.getDisplayArea(prop));
                }
                case "component" -> {
                    assertNull(prop.getStringValue());
                    assertEquals("config.component", prop.getRawValueText());
                }
                case "enabled" -> {
                    assertNull(prop.getStringValue());
                    assertEquals("true", prop.getRawValueText());
                }
                case "timeout" -> {
                    assertNull(prop.getStringValue());
                    assertEquals("500", prop.getRawValueText());
                }
                case "literal" -> assertEquals("Vendor_Module/static", prop.getStringValue());
                default -> { }
            }
        }
        assertTrue(templateResolver.collectTemplatePaths(source).isEmpty());

        final RequireJsConfig requireJsConfig = MagentoJsParser.parseRequireJsConfig("""
                var config = {
                    map: { '*': { aliasA: dynamicTarget, aliasB: 'Vendor_Module/' + suffix, aliasC: 'Vendor_Module/ok' } },
                    paths: { pathA: getPath(), pathB: 'Vendor_Module/path-ok' }
                };
                """);
        assertEquals(1, requireJsConfig.mapEntries().size());
        assertEquals("aliasC", requireJsConfig.mapEntries().get(0).alias());
        assertEquals(1, requireJsConfig.pathEntries().size());
        assertEquals("pathB", requireJsConfig.pathEntries().get(0).alias());
    }

    public void testParsesComponentAfterRegexFollowingControlFlowParenthesis() {
        final String source = """
                define([], function () {
                    if (window.foo) /['"]/.test(window.foo);
                    while (cond()) /['"]/.test(bar);
                    var ratio = obj.if(x) / 2;
                    return {
                        defaults: {
                            template: 'Vendor_Module/main',
                            ratio: ratio
                        }
                    };
                });
                """;

        final JsAst ast = MagentoJsParser.parse(source);
        final JsProperty templateProp = ast.getProperties().stream()
                .filter(p -> "template".equals(p.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("Control-flow regex with quotes must not swallow subsequent properties", templateProp);
        assertEquals("Vendor_Module/main", templateProp.getStringValue());
        assertEquals("defaults", templateProp.getParentProperty().getName());
    }
}
