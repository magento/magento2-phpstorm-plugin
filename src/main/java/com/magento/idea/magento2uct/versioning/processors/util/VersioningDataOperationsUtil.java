/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors.util;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.Pair;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class VersioningDataOperationsUtil {

    private VersioningDataOperationsUtil() {
    }

    /**
     * Get DIFF between two maps of data.
     *
     * @param allData Map[String, Boolean] with all previous data
     * @param newData Map[String, Boolean] with new data
     *
     * @return Map[String, Boolean]
     */
    public static Map<String, Boolean> getDiff(
            final Map<String, Boolean> allData,
            final Map<String, Boolean> newData
    ) {
        final Sets.SetView<Map.Entry<String, Boolean>> addedSet = Sets.difference(
                newData.entrySet(),
                allData.entrySet()
        );
        final Map<String, Boolean> added = addedSet.stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
        ));

        final Sets.SetView<Map.Entry<String, Boolean>> removedSet = Sets.difference(
                allData.entrySet(),
                newData.entrySet()
        );
        final Map<String, Boolean> removed = removedSet.stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                element -> false
        ));
        final Map<String, Boolean> diff = new HashMap<>(added);
        diff.putAll(removed);

        return diff;
    }

    /**
     * Union versioning data and changelog.
     *
     * @param versioningData Map[String, Map[String, Boolean]]
     * @param excludeFromChangelog List[String]
     * @param shouldKeepNew boolean
     *
     * @return Pair[Map[String, Boolean], Map[String, String]]
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static Pair<Map<String, Boolean>, Map<String, String>> unionVersionDataWithChangelog(
            final Map<String, Map<String, Boolean>> versioningData,
            final List<String> excludeFromChangelog,
            final boolean shouldKeepNew
    ) {
        final Map<String, Boolean> union = new HashMap<>();
        Map<String, Boolean> removedUnion = new HashMap<>();
        final Map<String, String> changelog = new HashMap<>();
        final List<String> versions = new LinkedList<>(versioningData.keySet());

        versions.sort((key1, key2) -> {
            final SupportedVersion version1 = SupportedVersion.getVersion(key1);
            final SupportedVersion version2 = SupportedVersion.getVersion(key2);

            if (version1 == null || version2 == null) {
                return 0;
            }

            return version1.compareTo(version2);
        });

        for (final String version : versions) {
            final Map<String, Boolean> vData = versioningData.get(version);

            final Set<Map.Entry<String, Boolean>> removedSet = Sets.filter(
                    vData.entrySet(),
                    entry -> !entry.getValue()
            );
            final Map<String, Boolean> removedData = removedSet.stream().collect(
                    Collectors.toMap(
                            Map.Entry::getKey,
                            element -> true
                    )
            );
            removedUnion.putAll(removedData);

            final Sets.SetView<Map.Entry<String, Boolean>> newDataSet = Sets.difference(
                    vData.entrySet(),
                    removedSet
            );
            final Map<String, Boolean> newData = newDataSet.stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
            ));
            union.putAll(newData);

            final Sets.SetView<Map.Entry<String, Boolean>> returnedSet = Sets.intersection(
                    newData.entrySet(),
                    removedUnion.entrySet()
            );

            if (!returnedSet.isEmpty()) {
                final Sets.SetView<Map.Entry<String, Boolean>> updatedRemovedUnionSet =
                        Sets.difference(removedUnion.entrySet(), returnedSet);
                removedUnion = new HashMap<>(updatedRemovedUnionSet.stream().collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )
                ));
            }

            if (!excludeFromChangelog.contains(version)) {
                if (shouldKeepNew) {
                    newData.forEach((key, value) -> changelog.put(key, version));
                } else {
                    removedData.forEach((key, value) -> changelog.put(key, version));
                }
            }
        }
        final Sets.SetView<Map.Entry<String, Boolean>> filteredUnionSet = Sets.difference(
                union.entrySet(),
                removedUnion.entrySet()
        );
        final Map<String, Boolean> filteredUnion = filteredUnionSet.stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )
        );

        return new Pair<>(new HashMap<>(filteredUnion), new HashMap<>(changelog));
    }

    /**
     * Union versioning data into single set.
     *
     * @param versioningData Map
     *
     * @return Map[String, Boolean]
     */
    public static Map<String, Boolean> unionVersionData(
            final Map<String, Map<String, Boolean>> versioningData
    ) {
        final Map<String, Boolean> union = new HashMap<>();

        for (final Map.Entry<String, Map<String, Boolean>> vData : versioningData.entrySet()) {
            union.putAll(vData.getValue());
        }

        return union;
    }
}
