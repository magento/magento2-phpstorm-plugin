/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourceLoader<K, V> implements IndexLoader<K, V> {

    @Override
    public @Nullable Map<K, V> load(final @NotNull String resourceName)
            throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = null;

        try (InputStream inputStream = getClass().getResourceAsStream(resourceName)) { // NOPMD
            if (inputStream == null) {
                return null;
            }
            objectInputStream = new ObjectInputStream(inputStream);

            return (HashMap<K, V>) objectInputStream.readObject();
        } catch (ClassCastException exception) {
            return null;
        } finally {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        }
    }
}
