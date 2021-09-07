/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning;

import com.magento.idea.magento2uct.versioning.indexes.VersionStateIndex;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class IndexRepository<K, V> implements Storage<K, V> {

    private static final String DEFAULT_RELATIVE_PATH = File.separator + "uct"
            + File.separator;

    private final String basePath;
    private final IndexRegistry index;

    public IndexRepository() {
        this("", null);
    }

    public IndexRepository(
            final @NotNull String basePath,
            final IndexRegistry index
    ) {
        this.basePath = basePath;
        this.index = index;
    }

    @Override
    public void put(final @NotNull Map<K, V> data, final String version) {
        try {
            final Path path = Paths.get(basePath + DEFAULT_RELATIVE_PATH);

            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            if (!index.getVersions().contains(version)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Specified version `%s` must be declared for the index `%s`",
                                version, index.getKey()
                        )
                );
            }
            final String indexName = VersionStateIndex.FILE_NAME_PATTERN
                    .replace("%version", version)
                    .replace("%key", index.getKey());
            final File file = new File(path.toAbsolutePath() + File.separator + indexName);

            if (file.exists()) {
                try {
                    final InputStream inputStream = new FileInputStream(file);
                    final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    final @NotNull Map<K, V> existedData =
                            (HashMap<K, V>) objectInputStream.readObject();

                    if (!existedData.isEmpty()) {
                        data.putAll(existedData);
                    }
                } catch (ClassNotFoundException readException) {
                    //
                }
            }
            final FileOutputStream outputStream = new FileOutputStream(file);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
        } catch (IOException exception) {
            //
        }
    }

    @Override
    public Map<K, V> get(final @NotNull String resourceName)
            throws IOException, ClassNotFoundException {
        InputStream inputStream = getClass().getResourceAsStream(resourceName);

        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return (HashMap<K, V>) objectInputStream.readObject();
        } catch (ClassCastException exception) {
            return null;
        }
    }
}
