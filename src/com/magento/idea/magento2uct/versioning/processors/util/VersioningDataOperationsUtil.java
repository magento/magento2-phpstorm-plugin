/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors.util;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
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
    public static Pair<Map<String, Boolean>, Map<String, String>> unionVersionDataWithChangelog(
            final Map<String, Map<String, Boolean>> versioningData,
            final List<String> excludeFromChangelog,
            final boolean shouldKeepNew
    ) {
        final Map<String, Boolean> union = new HashMap<>();
        final List<String> removed = new ArrayList<>();
        final Map<String, String> changelog = new HashMap<>();

        for (final Map.Entry<String, Map<String, Boolean>> vData : versioningData.entrySet()) {
            final Set<Map.Entry<String, Boolean>> removedSet = Sets.filter(
                    vData.getValue().entrySet(),
                    entry -> !entry.getValue()
            );
            final Map<String, Boolean> removedData = removedSet.stream().collect(
                    Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    )
            );
            removedData.forEach((key, value) -> removed.add(key));
            final Sets.SetView<Map.Entry<String, Boolean>> newDataSet = Sets.difference(
                    vData.getValue().entrySet(),
                    removedSet
            );
            final Map<String, Boolean> newData = newDataSet.stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
            ));
            union.putAll(newData);

            if (!excludeFromChangelog.contains(vData.getKey())) {
                if (shouldKeepNew) {
                    newData.forEach((key, value) -> changelog.put(key, vData.getKey()));
                } else {
                    removedData.forEach((key, value) -> changelog.put(key, vData.getKey()));
                }
            }
        }
        final Set<Map.Entry<String, Boolean>> filteredUnionSet = Sets.filter(
                union.entrySet(),
                entry -> !removed.contains(entry.getKey())
        );
        final Map<String, Boolean> filteredUnion = filteredUnionSet.stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )
        );

        return new Pair<>(new HashMap<>(filteredUnion), new HashMap<>(changelog));
    }
}
