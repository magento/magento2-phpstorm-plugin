/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import org.jetbrains.annotations.NotNull;

/**
 * Lightweight JavaScript object-literal and RequireJS config parser without any dependency on the JetBrains
 * JavaScript plugin. Used by file-based indexes, UI component / Knockout resolvers, and MCP inspection queries
 * across PhpStorm, WebStorm, IntelliJ IDEA Community, and Headless Code Engine (HCE).
 */
public final class MagentoJsParser {
    private MagentoJsParser() {
    }

    public static @NotNull JsAst parse(final @NotNull CharSequence source) {
        return JsObjectParser.parse(source);
    }

    public static @NotNull RequireJsConfig parseRequireJsConfig(final @NotNull CharSequence source) {
        return RequireJsConfigParser.parse(source);
    }
}
