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

public class ApiCoverageStateIndex implements VersionStateIndex {

    private static final String RESOURCE_DIR = "api";

    private final Map<String, Map<String, Boolean>> versioningData;
    private final Map<String, Boolean> targetVersionData;
    private final Map<String, Boolean> codebaseSet;
    private String projectBasePath;

    /**
     * Api coverage state index constructor.
     */
    public ApiCoverageStateIndex() {
        this(new HashMap<>());
    }

    /**
     * Api coverage state index constructor.
     *
     * @param targetVersionCodebaseData Map
     */
    public ApiCoverageStateIndex(final Map<String, Boolean> targetVersionCodebaseData) {
        versioningData = new LinkedHashMap<>();
        targetVersionData = new HashMap<>();
        codebaseSet = new HashMap<>(targetVersionCodebaseData);
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
     * Check if the specified FQN exists in the existence index.
     *
     * @param fqn String
     *
     * @return boolean
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized boolean has(final @NotNull String fqn) {
        groupLoadedData();

        if (targetVersionData.containsKey(fqn)) {
            return true;
        }

        return !codebaseSet.containsKey(fqn);
    }

    /**
     * Get API coverage index data.
     *
     * @return Map[String, Boolean]
     */
    public Map<String, Boolean> getIndexData() {
        groupLoadedData();

        return targetVersionData;
    }

    @Override
    public void load(final @NotNull List<SupportedVersion> versions) {
        final IndexLoader<String, Map<String, Boolean>> loader = new ResourceLoader<>(RESOURCE_DIR);
        processLoading(versions, loader);
    }

    @Override
    public void loadFromFile(final @NotNull List<SupportedVersion> versions) {
        if (projectBasePath == null) {
            throw new IllegalArgumentException(
                    "Project base path is mandatory for loading index data from the file."
            );
        }
        final IndexLoader<String, Map<String, Boolean>> loader = new FileLoader<>(projectBasePath);
        processLoading(versions, loader);
    }

    /**
     * Process index loading.
     *
     * @param versions List[SupportedVersion]
     * @param loader IndexLoader
     */
    private void processLoading(
            final @NotNull List<SupportedVersion> versions,
            final IndexLoader<String, Map<String, Boolean>> loader
    ) {
        final IndexRegistry apiIndexInfo = IndexRegistry.getRegistryInfoByClass(
                ApiCoverageStateIndex.class
        );
        assert apiIndexInfo != null;

        final String apiIndexesFileName = VersionStateIndex.SINGLE_FILE_NAME_PATTERN
                .replace("%key", apiIndexInfo.getKey());

        try {
            final Map<String, Map<String, Boolean>> loadedData = loader.load(apiIndexesFileName);

            if (loadedData == null) {
                return;
            }

            for (final Map.Entry<String, Map<String, Boolean>> loadedEntry
                    : loadedData.entrySet()) {
                final SupportedVersion loadedVersion = SupportedVersion.getVersion(
                        loadedEntry.getKey()
                );

                if (loadedVersion == null || !versions.contains(loadedVersion)) {
                    continue;
                }
                putIndexData(loadedVersion.getVersion(), loadedEntry.getValue());
            }
        } catch (IOException | ClassNotFoundException exception) { //NOPMD
            // Just go for the next version.
        }
    }

    /**
     * Put index for version.
     *
     * @param version String
     * @param indexData Map
     */
    private void putIndexData(
            final @NotNull String version,
            final Map<String, Boolean> indexData
    ) {
        if (indexData != null) {
            versioningData.put(version, indexData);
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
        }
    }
}
