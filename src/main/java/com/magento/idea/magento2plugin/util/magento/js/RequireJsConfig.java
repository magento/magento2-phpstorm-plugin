/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Parsed Magento RequireJS configuration entries extracted from a {@code requirejs-config.js} file.
 */
public record RequireJsConfig(
        @NotNull List<MapEntry> mapEntries,
        @NotNull List<PathEntry> pathEntries,
        @NotNull List<MixinEntry> mixinEntries
) {
    public RequireJsConfig {
        mapEntries = List.copyOf(mapEntries);
        pathEntries = List.copyOf(pathEntries);
        mixinEntries = List.copyOf(mixinEntries);
    }

    public record MapEntry(
            @NotNull String context,
            @NotNull String alias,
            @NotNull String target,
            @NotNull String rawValueText,
            int keyOffset,
            int valueOffset
    ) { }

    public record PathEntry(
            @NotNull String alias,
            @NotNull String target,
            @NotNull String rawValueText,
            int keyOffset,
            int valueOffset
    ) { }

    public record MixinEntry(
            @NotNull String target,
            @NotNull String mixin,
            boolean enabled,
            int keyOffset,
            int valueOffset
    ) { }
}
