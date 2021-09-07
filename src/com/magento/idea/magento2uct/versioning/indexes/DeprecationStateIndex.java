/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes;

import com.intellij.openapi.util.Pair;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2uct.versioning.IndexRegistry;
import com.magento.idea.magento2uct.versioning.IndexRepository;
import com.magento.idea.magento2uct.versioning.Storage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class DeprecationStateIndex implements VersionStateIndex {

    private final Map<String, Map<String, Boolean>> versioningData;
    private String versionState;

    public DeprecationStateIndex() {
        versioningData = new LinkedHashMap<>();
    }

    /**
     * Check if PHP class exists in the deprecation index.
     *
     * @param phpClass PhpClass
     *
     * @return boolean
     */
    public boolean has(final @NotNull PhpClass phpClass) {
        return has(phpClass.getFQN());
    }

    /**
     * Check if PHP method exists in the deprecation index.
     *
     * @param method Method
     *
     * @return boolean
     */
    public boolean has(final @NotNull Method method) {
        final String methodFQN = method.getFQN();

        return has(methodFQN);
    }

    /**
     * Check if PHP field exists in the deprecation index.
     *
     * @param field Field
     *
     * @return boolean
     */
    public boolean has(final @NotNull Field field) {
        final String fieldFQN = field.getFQN();

        return has(fieldFQN);
    }

    /**
     * Check if the specified key exists in the deprecation index.
     *
     * @param key String
     *
     * @return boolean
     */
    public boolean has(final @NotNull String key) {
        final Pair<String, Boolean> lookupResult = isDeprecatedLookup(key);

        if (lookupResult.getSecond() != null && lookupResult.getSecond()) {
            versionState = lookupResult.getFirst();
            return true;
        }

        return false;
    }

    /**
     * Put index for version.
     *
     * @param version String
     * @param data Map
     */
    public void putIndexData(final @NotNull String version, final Map<String, Boolean> data) {
        versioningData.put(version, data);
    }

    public String getVersion() {
        return versionState;
    }

    /**
     * Lookup if key is deprecated.
     *
     * @param lookupKey String
     *
     * @return Pair[String, Boolean]
     */
    private Pair<String, Boolean> isDeprecatedLookup(final @NotNull String lookupKey) {
        if (versioningData.isEmpty()) {
            load();
        }
        for (final Map.Entry<String, Map<String, Boolean>> versionIndexEntry
                : versioningData.entrySet()) {
            final String version = versionIndexEntry.getKey();

            for (final Map.Entry<String, Boolean> indexEntry
                    : versionIndexEntry.getValue().entrySet()) {
                final String indexKey = indexEntry.getKey();
                final Boolean indexValue = indexEntry.getValue();

                if (lookupKey.equals(indexKey)) {
                    return new Pair<>(version, indexValue);
                }
            }
        }

        return new Pair<>(null, false);
    }

    /**
     * Load index from storage.
     */
    private void load() {
        final Storage<String, Boolean> storage = new IndexRepository<>();
        final IndexRegistry registrationInfo =
                IndexRegistry.getRegistryInfoByClass(DeprecationStateIndex.class);
        assert registrationInfo != null;

        for (final String version : registrationInfo.getVersions()) {
            final String indexName = VersionStateIndex.FILE_NAME_PATTERN
                    .replace("%version", version)
                    .replace("%key", registrationInfo.getKey());
            try {
                putIndexData(
                        version,
                        storage.get(File.separator + "uct" + File.separator + indexName)
                );
            } catch (IOException | ClassNotFoundException exception) {
                //
            }
        }
    }
}
