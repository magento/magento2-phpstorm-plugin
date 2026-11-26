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
}
