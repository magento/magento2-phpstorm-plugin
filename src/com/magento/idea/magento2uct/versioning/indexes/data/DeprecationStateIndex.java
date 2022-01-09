/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.data;

import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import com.magento.idea.magento2uct.versioning.indexes.storage.FileLoader;
import com.magento.idea.magento2uct.versioning.indexes.storage.IndexLoader;
import com.magento.idea.magento2uct.versioning.indexes.storage.ResourceLoader;
import com.magento.idea.magento2uct.versioning.processors.util.VersioningDataOperationsUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class DeprecationStateIndex implements VersionStateIndex {

    private static final String RESOURCE_DIR = "deprecation";

    private final Map<String, Map<String, Boolean>> versioningData;
    private final Map<String, Boolean> targetVersionData;
    private final Map<String, String> changelog;
    private String projectBasePath;

    /**
     * Deprecation state index constructor.
     */
    public DeprecationStateIndex() {
        versioningData = new LinkedHashMap<>();
        targetVersionData = new HashMap<>();
        changelog = new HashMap<>();
    }

    /**
     * Set project base path.
     *
     * @param projectBasePath String
     */
    public void setProjectBasePath(final @NotNull String projectBasePath) {
        this.projectBasePath = projectBasePath;
    }

    /**
     * Check if the specified key exists in the deprecation index.
     *
     * @param key String
     *
     * @return boolean
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized boolean has(final @NotNull String key) {
        groupLoadedData();

        return targetVersionData.containsKey(key);
    }

    /**
     * Get version state after lookup.
     *
     * @param fqn String
     *
     * @return String
     */
    public String getVersion(final @NotNull String fqn) {
        final String version = changelog.get(fqn);

        return version == null ? "2.3.0 or before" : version;
    }

    @Override
    public void load(final @NotNull List<SupportedVersion> versions) {
        final IndexLoader<String, Boolean> loader = new ResourceLoader<>(RESOURCE_DIR);
        processLoading(versions, loader);
    }

    @Override
    public void loadFromFile(final @NotNull List<SupportedVersion> versions) {
        if (projectBasePath == null) {
            throw new IllegalArgumentException(
                    "Project base path is mandatory for loading index data from the file."
            );
        }
        final IndexLoader<String, Boolean> loader = new FileLoader<>(projectBasePath);
        processLoading(versions, loader);
    }

    /**
     * Get deprecation index data.
     *
     * @return Map[String, Boolean]
     */
    public Map<String, Boolean> getIndexData() {
        groupLoadedData();

        return targetVersionData;
    }

    /**
     * Process index loading.
     *
     * @param versions List[SupportedVersion]
     * @param loader IndexLoader
     */
    private void processLoading(
            final @NotNull List<SupportedVersion> versions,
            final IndexLoader<String, Boolean> loader
    ) {
        final IndexRegistry registrationInfo = IndexRegistry.getRegistryInfoByClass(
                DeprecationStateIndex.class
        );
        assert registrationInfo != null;

        for (final SupportedVersion version : versions) {
            final String indexName = VersionStateIndex.FILE_NAME_PATTERN
                    .replace("%version", version.getVersion())
                    .replace("%key", registrationInfo.getKey());
            try {
                putIndexData(version.getVersion(), loader.load(indexName));
            } catch (IOException | ClassNotFoundException exception) { //NOPMD
                // Just go for the next version.
            }
        }
    }

    /**
     * Put index for version.
     *
     * @param version String
     * @param data Map
     */
    private void putIndexData(final @NotNull String version, final Map<String, Boolean> data) {
        if (data != null) {
            versioningData.put(version, data);
        }
    }

    /**
     * Group data according to purpose.
     */
    private void groupLoadedData() {
        if (targetVersionData.isEmpty() && !versioningData.isEmpty()) {
            final Pair<Map<String, Boolean>, Map<String, String>> gatheredData =
                    VersioningDataOperationsUtil.unionVersionDataWithChangelog(
                            versioningData,
                            new ArrayList<>(Collections.singletonList(
                                    SupportedVersion.V230.getVersion()
                            )),
                            true
                    );
            targetVersionData.putAll(gatheredData.getFirst());
            changelog.putAll(gatheredData.getSecond());
        }
    }
}
