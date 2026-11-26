/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.magento.idea.magento2plugin.util.magento.js.RequireJsConfig.MapEntry;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsConfig.MixinEntry;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsConfig.PathEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extracts Magento {@code requirejs-config.js} {@code map}, {@code paths}, and {@code config.mixins}
 * sections from a parsed {@link JsAst}.
 */
final class RequireJsConfigParser {
    private RequireJsConfigParser() {
    }

    static @NotNull RequireJsConfig parse(final @NotNull CharSequence source) {
        final JsAst ast = JsObjectParser.parse(source);
        final List<MapEntry> mapEntries = new ArrayList<>();
        final List<PathEntry> pathEntries = new ArrayList<>();
        final List<MixinEntry> mixinEntries = new ArrayList<>();

        for (final JsObject config : findConfigObjects(ast)) {
            collectMapEntries(config, mapEntries);
            collectPathEntries(config, pathEntries);
            collectMixinEntries(config, mixinEntries);
        }

        return new RequireJsConfig(mapEntries, pathEntries, mixinEntries);
    }

    private static @NotNull List<JsObject> findConfigObjects(final @NotNull JsAst ast) {
        final List<JsObject> configObjects = ast.getVariableObjects("config");
        if (!configObjects.isEmpty()) {
            return configObjects;
        }
        // Fall back to the first top-level object with map/paths/config when var/let/const is omitted
        for (final JsObject candidate : ast.getObjects()) {
            if (candidate.getParentProperty() == null
                    && (candidate.findProperty("map") != null
                    || candidate.findProperty("paths") != null
                    || candidate.findProperty("config") != null)) {
                return Collections.singletonList(candidate);
            }
        }
        return Collections.emptyList();
    }

    private static void collectMapEntries(
            final @NotNull JsObject rootConfig,
            final @NotNull List<MapEntry> results
    ) {
        final JsObject mapObj = findObjectProperty(rootConfig, "map");
        if (mapObj == null) {
            return;
        }
        for (final JsProperty contextProp : mapObj.getProperties()) {
            final JsObject mappingsObj = contextProp.getValueObject();
            if (mappingsObj == null) {
                continue;
            }
            final String context = contextProp.getName();
            for (final JsProperty mapping : mappingsObj.getProperties()) {
                final String rawValue = mapping.getRawValueText();
                final String target = extractMappingTarget(mapping.getName(), rawValue);
                if (target != null && rawValue != null) {
                    results.add(new MapEntry(
                            context,
                            mapping.getName(),
                            target,
                            rawValue,
                            mapping.getKeyStartOffset(),
                            mapping.getValueStartOffset()
                    ));
                }
            }
        }
    }

    private static void collectPathEntries(
            final @NotNull JsObject rootConfig,
            final @NotNull List<PathEntry> results
    ) {
        final JsObject pathsObj = findObjectProperty(rootConfig, "paths");
        if (pathsObj == null) {
            return;
        }
        for (final JsProperty mapping : pathsObj.getProperties()) {
            final String rawValue = mapping.getRawValueText();
            final String target = extractMappingTarget(mapping.getName(), rawValue);
            if (target != null && rawValue != null) {
                results.add(new PathEntry(
                        mapping.getName(),
                        target,
                        rawValue,
                        mapping.getKeyStartOffset(),
                        mapping.getValueStartOffset()
                ));
            }
        }
    }

    private static void collectMixinEntries(
            final @NotNull JsObject rootConfig,
            final @NotNull List<MixinEntry> results
    ) {
        final JsObject configObj = findObjectProperty(rootConfig, "config");
        final JsObject mixinsObj = configObj == null ? null : findObjectProperty(configObj, "mixins");
        if (mixinsObj == null) {
            return;
        }
        for (final JsProperty targetProp : mixinsObj.getProperties()) {
            final String target = JsProperty.normalizeRequirePath(targetProp.getName());
            final JsObject declarations = targetProp.getValueObject();
            if (target == null || declarations == null) {
                continue;
            }
            for (final JsProperty mixinProp : declarations.getProperties()) {
                final String mixin = JsProperty.normalizeRequirePath(mixinProp.getName());
                if (mixin == null) {
                    continue;
                }
                final String rawVal = mixinProp.getRawValueText();
                final boolean enabled = rawVal == null || !"false".equalsIgnoreCase(rawVal.trim());
                results.add(new MixinEntry(
                        target,
                        mixin,
                        enabled,
                        mixinProp.getKeyStartOffset(),
                        mixinProp.getValueStartOffset()
                ));
            }
        }
    }

    private static @Nullable JsObject findObjectProperty(
            final @NotNull JsObject owner,
            final @NotNull String propertyName
    ) {
        final JsProperty prop = owner.findProperty(propertyName);
        return prop == null ? null : prop.getValueObject();
    }

    private static @Nullable String extractMappingTarget(
            final @NotNull String alias,
            final @Nullable String rawValue
    ) {
        if (alias.isBlank() || rawValue == null || rawValue.isBlank()) {
            return null;
        }
        final String target = JsProperty.unquoteOrTrim(rawValue);
        return target == null || target.isBlank() ? null : target;
    }
}
