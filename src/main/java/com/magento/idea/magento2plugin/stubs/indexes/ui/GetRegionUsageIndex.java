/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.ui;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationDataSetExternalizer;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class GetRegionUsageIndex extends FileBasedIndexExtension<String, Set<UiComponentNavigationData>> {
    public static final ID<String, Set<UiComponentNavigationData>> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.ui_component_get_region_usages"
    );

    @Override
    public @NotNull ID<String, Set<UiComponentNavigationData>> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, Set<UiComponentNavigationData>, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Set<UiComponentNavigationData>> map = new HashMap<>();

            if (!Settings.isEnabled(inputData.getProject())) {
                return map;
            }
            for (final UiComponentNavigationData usage : UiComponentScopeResolver.getInstance()
                    .collectGetRegionUsages(inputData.getPsiFile())) {
                map.computeIfAbsent(usage.getValue(), ignored -> new LinkedHashSet<>()).add(usage);
            }

            return map;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull DataExternalizer<Set<UiComponentNavigationData>> getValueExternalizer() {
        return UiComponentNavigationDataSetExternalizer.INSTANCE;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType().equals(HtmlFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
