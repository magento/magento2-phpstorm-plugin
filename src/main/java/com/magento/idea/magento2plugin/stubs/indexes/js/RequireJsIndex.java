/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.js;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.util.magento.js.MagentoJsParser;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsConfig;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class RequireJsIndex extends FileBasedIndexExtension<String, String> {

    public static final ID<String, String> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.require_js"
    );

    @Override
    public @NotNull ID<String, String> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, String> map = new HashMap<>();
            final RequireJsConfig config =
                    MagentoJsParser.parseRequireJsConfig(inputData.getContentAsText());

            for (final RequireJsConfig.MapEntry entry : config.mapEntries()) {
                map.put(entry.alias(), entry.rawValueText());
            }
            for (final RequireJsConfig.PathEntry entry : config.pathEntries()) {
                map.put(entry.alias(), entry.rawValueText());
            }

            return map;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> "requirejs-config.js".equals(virtualFile.getName());
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }
}
