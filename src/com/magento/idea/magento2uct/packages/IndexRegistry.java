/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.packages;

import com.magento.idea.magento2uct.versioning.indexes.data.DeprecationStateIndex;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum IndexRegistry {

    DEPRECATION(
            DeprecationStateIndex.class,
            SupportedVersion.getSupportedVersions().toArray(new String[0])
    );

    private final String key;
    private final Class<?> type;
    private final String[] versions;

    IndexRegistry(final Class<?> type, final String[] versions) {
        this.type = type;
        this.versions = versions;
        key = this.toString();
    }

    /**
     * Get index key.
     *
     * @return String
     */
    public String getKey() {
        return key;
    }

    /**
     * Get index type.
     *
     * @return Class[?]
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Get registered versions.
     *
     * @return List[String]
     */
    public List<String> getVersions() {
        return Arrays.asList(versions);
    }

    /**
     * Get registry record by index class.
     *
     * @param indexClass Class
     *
     * @return IndexRegistry
     */
    public static IndexRegistry getRegistryInfoByClass(final @NotNull Class<?> indexClass) {
        for (final IndexRegistry registeredIndexData : IndexRegistry.values()) {
            if (indexClass.equals(registeredIndexData.getType())) {
                return registeredIndexData;
            }
        }

        return null;
    }
}
