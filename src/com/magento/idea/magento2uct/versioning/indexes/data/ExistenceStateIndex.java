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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
public class ExistenceStateIndex implements VersionStateIndex {

    private static final String RESOURCE_DIR = "existence";

    private final Map<String, Map<String, Boolean>> versioningData;
    private final Map<String, Boolean> targetVersionData;
    private final Map<String, String> changelog;
    private final Set<String> codebase;
    private String projectBasePath;

    /**
     * Existence state index constructor.
     */
    public ExistenceStateIndex() {
        versioningData = new LinkedHashMap<>();
        targetVersionData = new HashMap<>();
        changelog = new HashMap<>();
        codebase = new HashSet<>();
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
    public synchronized boolean has(final @NotNull String fqn) {
        groupLoadedData();

        if (targetVersionData.containsKey(fqn)) {
            return true;
        }

        return !changelog.containsKey(fqn);
    }

    /**
     * Checks if specified FQN was/is in the MBE/VBE.
     *
     * @param fqn String
     *
     * @return boolean
     */
    public synchronized boolean isPresentInCodebase(final @NotNull String fqn) {
        return codebase.contains(fqn);
    }

    /**
     * Get version for specified FQN from prepared changelog.
     *
     * @param fqn String
     *
     * @return String
     */
    public String getVersion(final @NotNull String fqn) {
        final String version = changelog.get(fqn);

        return version == null ? "2.3.0 or before" : version;
    }

    /**
     * Get versioning data.
     *
     * @return Map
     */
    public Map<String, Map<String, Boolean>> getAllData() {
        return versioningData;
    }

    /**
     * Get existence index data.
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
        final IndexRegistry registrationInfo = IndexRegistry.getRegistryInfoByClass(
                ExistenceStateIndex.class
        );
        assert registrationInfo != null;

        final String indexesFileName = VersionStateIndex.SINGLE_FILE_NAME_PATTERN
                .replace("%key", registrationInfo.getKey());

        try {
            final Map<String, Map<String, Boolean>> loadedData = loader.load(indexesFileName);

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
                            false
                    );
            targetVersionData.putAll(gatheredData.getFirst());
            changelog.putAll(gatheredData.getSecond());
            codebase.addAll(VersioningDataOperationsUtil.unionVersionData(versioningData).keySet());
        }
    }
}
