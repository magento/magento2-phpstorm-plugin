/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.impl.DomApplicationComponent;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;


public class UIComponentIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.ui_component");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    /**
     * Indexer for `ui_component` files.
     *
     * @return this
     */
    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();//NOPMD

            final PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            final VirtualFile file = inputData.getFile();
            if (!file.getPath().matches("uiComponent")) {
                return map;
            }

            final String key = file.getName();
            if (!key.isEmpty()) {
                map.put(key, null);
            }

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
        return file -> file.getFileType() == XmlFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return DomApplicationComponent.getInstance().getCumulativeVersion(false);
    }
}
