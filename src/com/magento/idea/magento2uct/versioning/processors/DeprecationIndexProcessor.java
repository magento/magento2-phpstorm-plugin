/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import com.magento.idea.magento2uct.versioning.indexes.data.DeprecationStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.data.VersionStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.storage.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class DeprecationIndexProcessor implements IndexProcessor {

    private final Map<String, Boolean> data = new HashMap<>();

    @Override
    public void clearData() {
        data.clear();
    }

    @Override
    public void process(final @NotNull PsiFile file) {
        final PhpClass phpClass = PhpClassLocatorUtil.locate(file);

        if (phpClass == null) {
            return;
        }

        if (phpClass.isDeprecated()) {
            data.put(phpClass.getFQN(), true);
        }

        for (final Field field : phpClass.getOwnFields()) {
            if (field.isDeprecated()) {
                data.put(field.getFQN(), true);
            }
        }

        for (final Method method : phpClass.getOwnMethods()) {
            if (method.isDeprecated()) {
                data.put(method.getFQN(), true);
            }
        }
    }

    @Override
    public void save(
            final @NotNull String basePath,
            final @NotNull SupportedVersion indexedVersion
    ) {
        if (data.isEmpty()) {
            return;
        }
        filter(indexedVersion, basePath);

        if (!data.isEmpty()) {
            final FileWriter<String, Boolean> indexStorage = new FileWriter<>(//NOPMD
                    basePath,
                    IndexRegistry.DEPRECATION
            );
            final String indexName = VersionStateIndex.FILE_NAME_PATTERN
                    .replace("%version", indexedVersion.getVersion())
                    .replace("%key", IndexRegistry.DEPRECATION.getKey());

            indexStorage.put(data, indexedVersion.getVersion(), indexName);
        }
    }

    /**
     * Filter deprecation data to not store duplicates.
     *
     * @param indexedVersion SupportedVersion
     * @param basePath String
     */
    private void filter(
            final @NotNull SupportedVersion indexedVersion,
            final @NotNull String basePath
    ) {
        final List<SupportedVersion> previousVersions = new ArrayList<>();

        for (final SupportedVersion supportedVersion : SupportedVersion.values()) {
            if (supportedVersion.compareTo(indexedVersion) < 0) {
                previousVersions.add(supportedVersion);
            }
        }
        final DeprecationStateIndex deprecationIndex = new DeprecationStateIndex();

        deprecationIndex.setProjectBasePath(basePath);
        deprecationIndex.loadFromFile(previousVersions);
        final Map<String, Boolean> previousData = deprecationIndex.getIndexData();

        data.entrySet().removeAll(previousData.entrySet());
    }
}
