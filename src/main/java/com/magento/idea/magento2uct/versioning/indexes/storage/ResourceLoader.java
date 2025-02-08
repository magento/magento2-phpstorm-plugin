/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.storage;

import com.magento.idea.magento2plugin.magento.packages.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourceLoader<K, V> implements IndexLoader<K, V> {

    private static final String BASE_PATH = File.separator + "uct";

    private final String resourcePath;

    /**
     * Resource loader constructor.
     *
     * @param baseDir String
     */
    public ResourceLoader(final @NotNull String baseDir) {
        resourcePath = BASE_PATH + File.separator + baseDir + File.separator;
    }

    @Override
    public @Nullable Map<K, V> load(final @NotNull String resourceName)
            throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = null;
        final String path = resourcePath + resourceName;

        try (InputStream inputStream = getClass().getResourceAsStream(path)) { // NOPMD
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
