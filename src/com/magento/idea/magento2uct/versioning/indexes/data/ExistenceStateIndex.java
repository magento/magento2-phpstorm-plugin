/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.data;

import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import com.magento.idea.magento2uct.versioning.indexes.storage.FileLoader;
import com.magento.idea.magento2uct.versioning.indexes.storage.IndexLoader;
import com.magento.idea.magento2uct.versioning.indexes.storage.ResourceLoader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ExistenceStateIndex implements VersionStateIndex {

    private static final String RESOURCE_DIR = "existence";

    private final Map<String, Map<String, Boolean>> data;
    private String projectBasePath;
    private String version;//NOPMD

    /**
     * Existence state index constructor.
     */
    public ExistenceStateIndex() {
        data = new LinkedHashMap<>();
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
     * Get version state after lookup.
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get deprecation index data.
     *
     * @return Map[String, Boolean]
     */
    public Map<String, Boolean> getIndexData() {
        final Map<String, Boolean> allData = new HashMap<>();
        final Map<String, Boolean> removed = new HashMap<>();

        for (final Map.Entry<String, Map<String, Boolean>> vData : data.entrySet()) {
            removed.putAll(
                    vData.getValue().entrySet()
                            .stream()
                            .filter(element -> !element.getValue())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            allData.putAll(vData.getValue());
        }

        return allData.entrySet()
                .stream()
                .filter(
                        element -> !removed.containsKey(element.getKey())
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
            data.put(version, indexData);
        }
    }
}
