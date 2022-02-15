/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes.storage;

import com.magento.idea.magento2uct.packages.IndexRegistry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class FileWriter<K, V> implements IndexWriter<K, V> {

    private static final String DEFAULT_RELATIVE_PATH = File.separator + "uct"
            + File.separator;

    private final String basePath;
    private final IndexRegistry index;

    /**
     * Index repository constructor.
     *
     * @param basePath String
     * @param index IndexRegistry
     */
    public FileWriter(
            final @NotNull String basePath,
            final @NotNull IndexRegistry index
    ) {
        this.basePath = basePath;
        this.index = index;
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void put(
            final @NotNull Map<K, V> data,
            final String version,
            final @NotNull String indexName
    ) {
        try {
            final Path path = Paths.get(basePath + DEFAULT_RELATIVE_PATH);

            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            if (!index.getVersions().contains(version)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Specified version `%s` must be declared for the index `%s`",
                                version,
                                index.getKey()
                        )
                );
            }
            final File file = new File(path.toAbsolutePath() + File.separator + indexName);
            final Map<K, V> finalizedData = new HashMap<>();

            if (file.exists()) {
                try (
                        InputStream inputStream = Files.newInputStream(Path.of(file.getPath()));
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                ) {
                    final @NotNull Map<K, V> existedData =
                            (HashMap<K, V>) objectInputStream.readObject();

                    if (!existedData.isEmpty()) {
                        finalizedData.putAll(existedData);
                    }
                } catch (ClassNotFoundException readException) { //NOPMD
                    // If an exception is occurred just go to file storing.
                }
            }
            try (
                    OutputStream outputStream = Files.newOutputStream(Path.of(file.getPath()));
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
            ) {
                finalizedData.putAll(data);
                objectOutputStream.writeObject(finalizedData);
            } catch (IOException ioException) { //NOPMD
                // No need to check, it runs only during development.
            }
        } catch (IOException exception) { //NOPMD
            // No need to check, it runs only during development.
        }
    }
}
