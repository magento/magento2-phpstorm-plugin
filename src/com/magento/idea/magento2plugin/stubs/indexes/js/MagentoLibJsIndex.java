/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.js;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class MagentoLibJsIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.magento_lib");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    /**
     * Indexer for `web/lib` JS files.
     *
     * @return this
     */
    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();//NOPMD
            final String magentoPath = Settings.getMagentoPath(inputData.getProject());
            if (magentoPath ==  null) {
                return map;
            }
            final String libPath = magentoPath
                    + File.separator + Package.libWebRoot + File.separator;
            final VirtualFile file = inputData.getFile();

            if (!file.getPath().contains(libPath)) {
                return map;
            }

            String key = file.getPath().replace(libPath, "");
            key = key.substring(0, key.lastIndexOf('.'));
            map.put(key, null);

            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> (
                virtualFile.getFileType().equals(JavaScriptFileType.INSTANCE)//NOPMD
                    && virtualFile.getPath().contains(Package.libWebRoot)
        );
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
