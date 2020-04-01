/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.impl.DomApplicationComponent;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VirtualTypeIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.virtual_type");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Map<String, String> map = new THashMap<>();
            PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (psiFile instanceof XmlFile) {
                XmlDocument xmlDocument = ((XmlFile) psiFile).getDocument();
                if (xmlDocument != null) {
                    XmlTag xmlRootTag = xmlDocument.getRootTag();
                    if (xmlRootTag != null) {
                        for (XmlTag virtualTypeTag : xmlRootTag.findSubTags("virtualType")) {
                            String name = virtualTypeTag.getAttributeValue("name");
                            String type = virtualTypeTag.getAttributeValue("type");

                            if (name != null && type != null && !name.isEmpty() && !type.isEmpty()) {
                                map.put(name, type);
                            }
                        }
                    }
                }
            }
            return map;
        };
    }

    @NotNull
    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file ->
                file.getFileType() == XmlFileType.INSTANCE && file.getName().equalsIgnoreCase("di.xml");
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
