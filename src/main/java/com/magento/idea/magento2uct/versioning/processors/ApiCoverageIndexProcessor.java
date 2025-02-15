/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import com.magento.idea.magento2uct.versioning.indexes.data.ApiCoverageStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.data.VersionStateIndex;
import com.magento.idea.magento2uct.versioning.indexes.storage.FileWriter;
import com.magento.idea.magento2uct.versioning.processors.util.VersioningDataOperationsUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ApiCoverageIndexProcessor implements IndexProcessor {

    private final Map<String, Boolean> data = new HashMap<>();

    @Override
    public void clearData() {
        data.clear();
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void process(final @NotNull PsiFile file) {
        final PhpClass phpClass = PhpClassLocatorUtil.locate(file);

        if (phpClass == null) {
            return;
        }
        boolean isApiClass = false;

        if (hasApiDeclaration(phpClass.getDocComment())) {
            data.put(phpClass.getFQN(), true);
            isApiClass = true;
        }

        for (final Field field : phpClass.getOwnFields()) {
            if (field.getModifier().isPrivate()) {
                continue;
            }

            if (isApiClass) {
                data.put(field.getFQN(), true);
            } else if (hasApiDeclaration(field.getDocComment())) {
                data.put(field.getFQN(), true);
            }
        }

        for (final Method method : phpClass.getOwnMethods()) {
            if (method.getModifier().isPrivate()
                    || MagentoPhpClass.CONSTRUCT_METHOD_NAME.equals(method.getName())) {
                continue;
            }
            if (isApiClass) {
                data.put(method.getFQN(), true);
            } else if (hasApiDeclaration(method.getDocComment())) {
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
        commuteSymmetricDifference(indexedVersion, basePath);

        if (!data.isEmpty()) {
            final FileWriter<String, Map<String, Boolean>> storage = new FileWriter<>(//NOPMD
                    basePath,
                    IndexRegistry.API_COVERAGE
            );
            final String indexName = VersionStateIndex.SINGLE_FILE_NAME_PATTERN.replace(
                    "%key",
                    IndexRegistry.API_COVERAGE.getKey()
            );
            final Map<String, Map<String, Boolean>> versionedData = new HashMap<>();
            versionedData.put(indexedVersion.getVersion(), new HashMap<>(data));
            storage.put(versionedData, indexedVersion.getVersion(), indexName);
        }
    }

    /**
     * Process new coming data via symmetric difference operation under sets.
     * Removed data must have negative (FALSE) flag for future using.
     *
     * @param version SupportedVersion
     * @param basePath String
     */
    private void commuteSymmetricDifference(
            final @NotNull SupportedVersion version,
            final @NotNull String basePath
    ) {
        final List<SupportedVersion> priorVersions = SupportedVersion.getPriorVersions(version);

        final ApiCoverageStateIndex apiStateIndex = new ApiCoverageStateIndex();
        apiStateIndex.setProjectBasePath(basePath);
        apiStateIndex.loadFromFile(priorVersions);
        final Map<String, Boolean> previousData = apiStateIndex.getIndexData();

        final Map<String, Boolean> diff = VersioningDataOperationsUtil.getDiff(
                previousData,
                new HashMap<>(data)
        );
        data.clear();
        data.putAll(diff);
    }

    /**
     * Check if specified comment has an API declaration.
     *
     * @param comment PhpDocComment
     *
     * @return boolean
     */
    private boolean hasApiDeclaration(final @Nullable PhpDocComment comment) {
        if (comment == null) {
            return false;
        }

        return PhpDocUtil.hasDocTagWithName(comment, "@api");
    }
}
