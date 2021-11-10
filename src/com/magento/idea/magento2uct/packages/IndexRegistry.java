/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.packages;

import com.magento.idea.magento2uct.versioning.indexes.data.ApiCoverageStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.data.DeprecationStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.data.ExistenceStateIndex;
import com.magento.idea.magento2uct.versioning.processors.ApiCoverageIndexProcessor;
import com.magento.idea.magento2uct.versioning.processors.DeprecationIndexProcessor;
import com.magento.idea.magento2uct.versioning.processors.ExistenceIndexProcessor;
import com.magento.idea.magento2uct.versioning.processors.IndexProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public enum IndexRegistry {

    DEPRECATION(
            DeprecationStateIndex.class,
            new DeprecationIndexProcessor(),
            SupportedVersion.getSupportedVersions().toArray(new String[0])
    ),
    EXISTENCE(
            ExistenceStateIndex.class,
            new ExistenceIndexProcessor(),
            SupportedVersion.getSupportedVersions().toArray(new String[0])
    ),
    API_COVERAGE(
            ApiCoverageStateIndex.class,
            new ApiCoverageIndexProcessor(),
            SupportedVersion.getSupportedVersions().toArray(new String[0])
    );

    private final String key;
    private final Class<?> type;
    private final IndexProcessor processor;
    private final String[] versions;

    IndexRegistry(
            final Class<?> type,
            final IndexProcessor processor,
            final String... versions
    ) {
        this.type = type;
        this.processor = processor;
        this.versions = Arrays.copyOf(versions, versions.length);
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
     * Get index processor.
     *
     * @return IndexProcessor
     */
    public IndexProcessor getProcessor() {
        return processor;
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

    /**
     * Get registry record by index key.
     *
     * @param key String
     *
     * @return IndexRegistry
     */
    public static IndexRegistry getRegistryInfoByKey(final @NotNull String key) {
        try {
            return IndexRegistry.valueOf(key);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Get list of available indexes names.
     *
     * @return List[String]
     */
    public static List<String> getIndexList() {
        final List<String> list = new ArrayList<>();

        for (final IndexRegistry index : IndexRegistry.values()) {
            list.add(index.getKey());
        }

        return list;
    }
}
