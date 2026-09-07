/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.storage;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface IndexWriter<K, V> {

    /**
     * Put index data to storage.
     *
     * @param data Map[K, V]
     * @param version String
     * @param fileName String
     */
    void put(final Map<K, V> data, final String version, final @NotNull String fileName);
}
