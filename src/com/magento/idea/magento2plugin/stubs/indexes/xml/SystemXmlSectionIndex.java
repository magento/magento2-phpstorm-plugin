/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.ide.highlighter.XmlFileType;
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
import com.intellij.util.io.VoidDataExternalizer;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.xml.SystemConfigurationParserUtil;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SystemXmlSectionIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.xml.SystemXmlSectionIndex"
    );
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
    private final DataExternalizer<Void> myDataExternalizer = new VoidDataExternalizer();

    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();
            final PsiFile psiFile = inputData.getPsiFile();

            if (!Settings.isEnabled(psiFile.getProject())
                    || !(psiFile instanceof XmlFile)
                    || !ModuleSystemXmlFile.FILE_NAME.equals(psiFile.getName())) {
                return map;
            }
            final XmlFile xmlFile = (XmlFile) psiFile;
            final XmlTag rootTag = xmlFile.getRootTag();

            if (rootTag == null) {
                return map;
            }

            SystemConfigurationParserUtil.parseConfigurationTags(
                    rootTag,
                    SystemConfigurationParserUtil.ParsingDepth.SECTION_ID,
                    (sectionId, groupId, fieldId) -> {
                        if (sectionId == null) {
                            return;
                        }
                        map.put(sectionId, null);
                    }
            );

            return map;
        };
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType() == XmlFileType.INSTANCE
                && virtualFile.getNameWithoutExtension().equals("system");
    }

    @Override
    public @NotNull ID<String, Void> getName() {
        return KEY;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @Override
    public @NotNull DataExternalizer<Void> getValueExternalizer() {
        return myDataExternalizer;
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
