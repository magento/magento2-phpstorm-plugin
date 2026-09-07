/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.indexes.LayoutIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.data.StringSetDataExternalizer;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class LayoutBlockTemplateIndex extends FileBasedIndexExtension<String, Set<String>> {
    public static final ID<String, Set<String>> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.layout_block_template"
    );

    @Override
    public @NotNull ID<String, Set<String>> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> LayoutBlockTemplateIndexParser.getInstance()
                .parse(inputData)
                .getBlockTemplates();
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull DataExternalizer<Set<String>> getValueExternalizer() {
        return StringSetDataExternalizer.INSTANCE;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return LayoutIndex::isLayoutFile;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
