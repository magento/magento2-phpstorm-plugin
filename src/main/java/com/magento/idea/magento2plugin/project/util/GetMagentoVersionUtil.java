/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.util;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.magento.files.ComposerLock;
import com.magento.idea.magento2plugin.magento.packages.code.MagentoVersion;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GetMagentoVersionUtil {

    private GetMagentoVersionUtil() {
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

        if (packagesProperty == null) {
            return null;
        }
        final Collection<JsonObject> packages = PsiTreeUtil.findChildrenOfType(
                packagesProperty,
                JsonObject.class
        );
        final List<MagentoVersion> versions = MagentoVersion.getVersions();
        final List<String> versionNames = MagentoVersion.getVersionsNames();
        final Map<String, String> foundMagentoPackages = new HashMap<>();

        for (final JsonObject packageItem : packages) {
            final @Nullable ImmutablePair<String, String> magentoPackage = findMagentoPackage(
                    packageItem,
                    versionNames);

            if (magentoPackage == null) {
                continue;
            }

            foundMagentoPackages.put(magentoPackage.getLeft(), magentoPackage.getRight());

            if (foundMagentoPackages.containsKey(MagentoVersion.ENTERPRISE_EDITION.getName())
                    || foundMagentoPackages.containsKey(
                            MagentoVersion.MAGEOS_COMMUNITY_EDITION.getName())) {
                break;
            }
        }

        for (final MagentoVersion version : versions) {
            if (foundMagentoPackages.containsKey(version.getName())) {
                return new Pair<>(
                        foundMagentoPackages.get(version.getName()),
                        version.getDisplayName()
                );
            }
        }

        return null;
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
