/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import com.magento.idea.magento2uct.versioning.indexes.data.ExistenceStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.data.VersionStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.storage.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class ExistenceIndexProcessor implements IndexProcessor {

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
        data.put(phpClass.getFQN(), true);

        for (final Field field : phpClass.getOwnFields()) {
            if (field.getModifier().isPrivate()) {
                continue;
            }
            data.put(field.getFQN(), true);
        }

        for (final Method method : phpClass.getOwnMethods()) {
            if (method.getModifier().isPrivate()
                    || MagentoPhpClass.CONSTRUCT_METHOD_NAME.equals(method.getName())) {
                continue;
            }
            data.put(method.getFQN(), true);
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
            final FileWriter<String, Map<String, Boolean>> storage = new FileWriter<>(//NOPMD
                    basePath,
                    IndexRegistry.EXISTENCE
            );
            final String indexName = VersionStateIndex.SINGLE_FILE_NAME_PATTERN.replace(
                    "%key",
                    IndexRegistry.EXISTENCE.getKey()
            );
            final Map<String, Map<String, Boolean>> versionedData = new HashMap<>();
            versionedData.put(indexedVersion.getVersion(), new HashMap<>(data));
            storage.put(versionedData, indexedVersion.getVersion(), indexName);
        }
    }

    /**
     * Filter existence data to not store duplicates.
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
        final ExistenceStateIndex existenceStateIndex = new ExistenceStateIndex();
        existenceStateIndex.setProjectBasePath(basePath);
        existenceStateIndex.loadFromFile(previousVersions);
        final Map<String, Boolean> previousData = existenceStateIndex.getIndexData();

        final Map<String, Boolean> added = new HashMap<>(data);
        added.entrySet().removeAll(previousData.entrySet());

        final Map<String, Boolean> removed = new HashMap<>(previousData);
        removed.entrySet().removeAll(data.entrySet());
        final Map<String, Boolean> revertedValueForRemoved = removed.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        element -> false
                )
        );

        data.clear();
        data.putAll(added);
        data.putAll(revertedValueForRemoved);
    }
}
