/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileLoader<K, V> implements IndexLoader<K, V> {

    private static final String DEFAULT_RELATIVE_PATH = File.separator + "uct"
            + File.separator;

    private final String basePath;

    /**
     * Index repository constructor.
     *
     * @param basePath String
     */
    public FileLoader(final @NotNull String basePath) {
        this.basePath = basePath;
    }

    @Override
    public @Nullable Map<K, V> load(final @NotNull String indexName) throws IOException {
        final Path path = Paths.get(basePath + DEFAULT_RELATIVE_PATH);
        final File file = new File(path.toAbsolutePath() + File.separator + indexName);

        if (!file.exists()) {
            throw new IOException("Could not find index file.");
        }
        final Map<K,V> data = new HashMap<>();

        try (
                InputStream inputStream = Files.newInputStream(Path.of(file.getPath()));
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        ) {
            final @NotNull Map<K, V> loadedData = (HashMap<K, V>) objectInputStream.readObject();

            if (!loadedData.isEmpty()) {
                data.putAll(loadedData);
            }
        } catch (ClassNotFoundException | IOException readException) { //NOPMD
            // If an exception is occurred just go to file storing.
        }

        return data;
    }
}
