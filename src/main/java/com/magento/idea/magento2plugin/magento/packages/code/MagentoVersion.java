/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum MagentoVersion {

    ENTERPRISE_EDITION("magento/product-enterprise-edition", 1, "Adobe Commerce"),
    COMMUNITY_EDITION("magento/product-community-edition", 2, "Magento Open Source"),
    MAGEOS_COMMUNITY_EDITION("mage-os/product-community-edition", 3, "Mage-OS Community Edition");

    private final String name;
    private final int priority;
    private final String displayName;

    /**
     * Magento version Enum constructor.
     *
     * @param name String
     * @param priority int
     * @param displayName String
     */
    MagentoVersion(final String name, final int priority, final String displayName) {
        this.name = name;
        this.priority = priority;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get Magento Versions List.
     *
     * @return List[MagentoVersion]
     */
    public static List<MagentoVersion> getVersions() {
        final List<MagentoVersion> versions = new ArrayList<>(
                Arrays.asList(MagentoVersion.values())
        );
        versions.sort(Comparator.comparingInt(MagentoVersion::getPriority));

        return versions;
    }

    /**
     * Get Magento Packages names.
     *
     * @return List[String]
     */
    public static List<String> getVersionsNames() {
        final List<String> names = new ArrayList<>();

        for (final MagentoVersion version : MagentoVersion.values()) {
            names.add(version.getName());
        }

        return names;
    }
}
