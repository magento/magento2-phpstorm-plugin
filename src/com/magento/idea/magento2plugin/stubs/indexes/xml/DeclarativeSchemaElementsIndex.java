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
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Index to store table names and column names from db_schema.xml files.
 */
public class DeclarativeSchemaElementsIndex extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.db_schema.tables_and_columns");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();
            final PsiFile psiFile = inputData.getPsiFile();
            final XmlDocument document = ((XmlFile) psiFile).getDocument();
            final XmlTag root = ((XmlFile) psiFile).getRootTag();

            if (!Settings.isEnabled(psiFile.getProject()) || document == null || root == null) {
                return map;
            }

            for (final XmlTag tableTag : root.getSubTags()) {
                if (tableTag.getName().equals("table")) {
                    final String tableName = tableTag.getAttributeValue("name");
                    map.put(tableName, null);

                    for (final XmlTag columnTag : tableTag.getSubTags()) {
                        if (columnTag.getName().equals("column")) {
                            map.put(tableName + "." + columnTag.getAttributeValue("name"), null);
                        }
                    }
                }
            }
            return map;
        };
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
        return VoidDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> (virtualFile.getFileType() == XmlFileType.INSTANCE
                && virtualFile.getNameWithoutExtension().equals("db_schema"));
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
