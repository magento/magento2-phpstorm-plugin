/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes;

import java.io.IOException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface Storage<K, V> {

    /**
     * Put index data to storage.
     *
     * @param data Map[K, V]
     * @param version String
     */
    void put(final Map<K, V> data, final String version);

    /**
     * Load index data from storage.
     *
     * @param resourceName String
     *
     * @return Map[K, V]
     */
    Map<K, V> load(final @NotNull String resourceName) throws IOException, ClassNotFoundException;
}
