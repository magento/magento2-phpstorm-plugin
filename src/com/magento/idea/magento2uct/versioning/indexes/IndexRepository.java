/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.indexes;

import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.versioning.indexes.data.VersionStateIndex;
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
import org.jetbrains.annotations.Nullable;

public class IndexRepository<K, V> implements Storage<K, V> {

    private static final String DEFAULT_RELATIVE_PATH = File.separator + "uct"
            + File.separator;

    private final String basePath;
    private final IndexRegistry index;

    /**
     * Index repository constructor.
     */
    public IndexRepository() {
        this("", null);
    }

    /**
     * Index repository constructor.
     *
     * @param basePath String
     * @param index IndexRegistry
     */
    public IndexRepository(
            final @NotNull String basePath,
            final IndexRegistry index
    ) {
        this.basePath = basePath;
        this.index = index;
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void put(final @NotNull Map<K, V> data, final String version) {
        if (basePath.isEmpty() || index == null) {
            throw new IllegalArgumentException(
                    "Project bath path and index type are required params for storing indexes."
            );
        }
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
            final String indexName = VersionStateIndex.FILE_NAME_PATTERN
                    .replace("%version", version)
                    .replace("%key", index.getKey());
            final File file = new File(path.toAbsolutePath() + File.separator + indexName);

            if (file.exists()) {
                try (
                        InputStream inputStream = Files.newInputStream(Path.of(file.getPath()));
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                ) {
                    final @NotNull Map<K, V> existedData =
                            (HashMap<K, V>) objectInputStream.readObject();

                    if (!existedData.isEmpty()) {
                        data.putAll(existedData);
                    }
                } catch (ClassNotFoundException readException) { //NOPMD
                    // If an exception is occurred just go to file storing.
                }
            }
            try (
                    OutputStream outputStream = Files.newOutputStream(Path.of(file.getPath()));
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
            ) {
                objectOutputStream.writeObject(data);
            } catch (IOException ioException) { //NOPMD
                // No need to check, it runs only during development.
            }
        } catch (IOException exception) { //NOPMD
            // No need to check, it runs only during development.
        }
    }

    @Override
    public @Nullable Map<K, V> load(final @NotNull String resourceName)
            throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = null;

        try (InputStream inputStream = getClass().getResourceAsStream(resourceName)) { //NOPMD
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
