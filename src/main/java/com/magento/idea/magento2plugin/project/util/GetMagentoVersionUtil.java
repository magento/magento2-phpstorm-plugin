/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.util;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2plugin.magento.files.ComposerLock;
import com.magento.idea.magento2plugin.magento.packages.code.MagentoVersion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GetMagentoVersionUtil {
    private static final String BASE_PACKAGE_NAME = "magento/magento2-base";

    private GetMagentoVersionUtil() {
    }

    /** Reads root metadata of a Magento source checkout, not a Composer project template. */
    public static @Nullable Pair<String, String> getSourceVersion(final @NotNull JsonObject object) {
        final String name = stringProperty(object, "name");
        final MagentoVersion edition;
        if ("magento/magento2ce".equals(name)) {
            edition = MagentoVersion.COMMUNITY_EDITION;
        } else if ("magento/magento2ee".equals(name)) {
            edition = MagentoVersion.ENTERPRISE_EDITION;
        } else {
            return null;
        }
        final String version = stringProperty(object, "version");
        return Pair.create(version, edition.getDisplayName());
    }

    private static @Nullable String stringProperty(final JsonObject object, final String name) {
        final JsonProperty property = object.findProperty(name);
        return property != null && property.getValue() instanceof JsonStringLiteral
                ? ((JsonStringLiteral) property.getValue()).getValue() : null;
    }

    /**
     * Find Magento Package version in composer.lock json object.
     *
     * @param object JsonObject
     *
     * @return Pair[String, String]
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public static @Nullable Pair<String, String> getVersion(final @NotNull JsonObject object) {
        final JsonProperty packagesProperty = object.findProperty(ComposerLock.PACKAGES_PROP);

        if (packagesProperty == null || !(packagesProperty.getValue() instanceof JsonArray)) {
            return null;
        }
        final List<JsonValue> packages = ((JsonArray) packagesProperty.getValue()).getValueList();
        final List<MagentoVersion> versions = MagentoVersion.getVersions();
        final List<String> versionNames = MagentoVersion.getVersionsNames();
        versionNames.add(BASE_PACKAGE_NAME);
        final Map<String, String> foundMagentoPackages = new HashMap<>();

        for (final JsonValue value : packages) {
            if (!(value instanceof JsonObject)) {
                continue;
            }
            final @Nullable ImmutablePair<String, String> magentoPackage = findMagentoPackage(
                    (JsonObject) value,
                    versionNames);

            if (magentoPackage == null) {
                continue;
            }

            if (foundMagentoPackages.put(magentoPackage.getLeft(), magentoPackage.getRight()) != null) {
                return null;
            }
        }

        if (foundMagentoPackages.containsKey(MagentoVersion.MAGEOS_COMMUNITY_EDITION.getName())
                && (foundMagentoPackages.containsKey(MagentoVersion.COMMUNITY_EDITION.getName())
                    || foundMagentoPackages.containsKey(MagentoVersion.ENTERPRISE_EDITION.getName()))) {
            return null;
        }

        for (final MagentoVersion version : versions) {
            if (foundMagentoPackages.containsKey(version.getName())) {
                return new Pair<>(
                        foundMagentoPackages.get(version.getName()),
                        version.getDisplayName()
                );
            }
        }

        final String baseVersion = foundMagentoPackages.get(BASE_PACKAGE_NAME);
        return baseVersion == null ? null : new Pair<>(baseVersion, null);
    }

    private static @Nullable ImmutablePair<String, String> findMagentoPackage(
            final JsonObject packageItem,
            final List<String> versionNames
    ) {
        final JsonProperty nameProperty = packageItem.findProperty(
                ComposerLock.PACKAGE_NAME_PROP
        );

        if (nameProperty == null || nameProperty.getValue() == null) {
            return null;
        }
        final String name = StringUtils.strip(nameProperty.getValue().getText(), "\"");

        if (!versionNames.contains(name)) {
            return null;
        }

        final JsonProperty versionProperty = packageItem.findProperty(
                ComposerLock.PACKAGE_VERSION_PROP
        );

        if (versionProperty == null || versionProperty.getValue() == null) {
            return null;
        }

        final String value = StringUtils.strip(versionProperty.getValue().getText(), "\"");

        return ImmutablePair.of(name, value);
    }
}
