/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.data;

import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2plugin.magento.packages.File;
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
import org.jetbrains.annotations.NotNull;

public class DeprecationStateIndex implements VersionStateIndex {

    private static final String RESOURCE_PATH = File.separator + "uct"
            + File.separator + "deprecation" + File.separator;

    private final Map<String, Map<String, Boolean>> versioningData;
    private String projectBasePath;
    private String versionState;

    /**
     * Deprecation state index constructor.
     */
    public DeprecationStateIndex() {
        versioningData = new LinkedHashMap<>();
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
    public boolean has(final @NotNull String key) {
        final Pair<String, Boolean> lookupResult = checkIfDeprecated(key);

        if (lookupResult.getSecond() != null && lookupResult.getSecond()) {
            versionState = lookupResult.getFirst();
            return true;
        }

        return false;
    }

    /**
     * Get version state after lookup.
     *
     * @return String
     */
    public String getVersion() {
        return versionState;
    }

    @Override
    public void load(final @NotNull List<SupportedVersion> versions) {
        final IndexLoader<String, Boolean> loader = new ResourceLoader<>();
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
        final Map<String, Boolean> data = new HashMap<>();

        for (final Map.Entry<String, Map<String, Boolean>> vData : versioningData.entrySet()) {
            data.putAll(vData.getValue());
        }

        return data;
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
                putIndexData(version.getVersion(), loader.load(RESOURCE_PATH + indexName));
            } catch (IOException | ClassNotFoundException exception) { //NOPMD
                // Just go for the next version.
            }
        }
    }

    /**
     * Lookup if key is deprecated.
     *
     * @param lookupKey String
     *
     * @return Pair[String, Boolean]
     */
    private Pair<String, Boolean> checkIfDeprecated(final @NotNull String lookupKey) {
        for (final Map.Entry<String, Map<String, Boolean>> versionIndexEntry
                : versioningData.entrySet()) {
            final String version = versionIndexEntry.getKey();

            if (versionIndexEntry.getValue().containsKey(lookupKey)) {
                return new Pair<>(version, versionIndexEntry.getValue().get(lookupKey));
            }
        }

        return new Pair<>(null, false);
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
}
