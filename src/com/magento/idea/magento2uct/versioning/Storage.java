/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning;

import java.io.IOException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface Storage<K, V> {

    void put(final Map<K, V> data, final String version);

    Map<K, V> get(final @NotNull String resourceName) throws IOException, ClassNotFoundException;
}
