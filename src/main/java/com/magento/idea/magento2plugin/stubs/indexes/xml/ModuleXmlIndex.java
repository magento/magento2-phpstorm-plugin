/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ModuleXmlIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.module_xml"
    );

    @Override
    public @NotNull ID<String, String> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, String> map = new HashMap<>();
            final PsiFile psiFile = inputData.getPsiFile();

            if (!Settings.isEnabled(psiFile.getProject()) || !(psiFile instanceof XmlFile)) {
                return map;
            }

            final XmlTag rootTag = ((XmlFile) psiFile).getRootTag();

            if (rootTag == null || !"config".equals(rootTag.getName())) {
                return map;
            }

            for (final XmlTag moduleTag : rootTag.findSubTags("module")) {
                final String moduleName = moduleTag.getAttributeValue("name");
                final VirtualFile moduleRoot = getModuleRoot(inputData.getFile());

                if (moduleName != null && moduleRoot != null) {
                    map.put(moduleName, moduleRoot.getPath());
                }
            }

            return map;
        };
    }

    private VirtualFile getModuleRoot(final @NotNull VirtualFile moduleXmlFile) {
        final VirtualFile etcDirectory = moduleXmlFile.getParent();

        return etcDirectory == null ? null : etcDirectory.getParent();
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType().equals(XmlFileType.INSTANCE)
                && "module.xml".equals(virtualFile.getName());
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
