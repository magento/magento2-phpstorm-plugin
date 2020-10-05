/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.impl.DomApplicationComponent;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.menu");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new THashMap<>();//NOPMD
            final PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }
            final XmlDocument xmlDocument = ((XmlFile) psiFile).getDocument();
            if (xmlDocument != null) {
                final XmlTag xmlRootTag = xmlDocument.getRootTag();
                if (xmlRootTag != null) {
                    parseRootTag(map, xmlRootTag);
                }
            }

            return map;
        };
    }

    protected void parseRootTag(final Map<String, Void> map, final XmlTag xmlRootTag) {
        @Nullable final XmlTag menuTag = xmlRootTag.findFirstSubTag(ModuleMenuXml.menuTag);
        if (menuTag == null) {
            return;
        }

        parseMenuTag(map, menuTag);
    }

    private void parseMenuTag(final Map<String, Void> map, final XmlTag menuTag) {
        for (final XmlTag addTag : menuTag.findSubTags(ModuleMenuXml.addTag)) {
            final String identifier = addTag.getAttributeValue(ModuleMenuXml.idTagAttribute);

            if (identifier != null && !identifier.isEmpty()) {
                map.putIfAbsent(identifier, null);
            }
        }
    }

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file ->
                file.getFileType() == XmlFileType.INSTANCE
                    && file.getName().equalsIgnoreCase(ModuleMenuXml.fileName);
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
