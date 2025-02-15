/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.storage;

import java.io.IOException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface IndexLoader<K, V> {

    /**
     * Load index data from storage.
     *
     * @param fileName String
     *
     * @return Map[K, V]
     */
    Map<K, V> load(final @NotNull String fileName) throws IOException, ClassNotFoundException;
}
