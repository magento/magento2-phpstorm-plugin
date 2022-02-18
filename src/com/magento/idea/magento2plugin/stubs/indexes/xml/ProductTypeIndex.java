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
import com.magento.idea.magento2plugin.magento.files.ProductTypeXml;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ProductTypeIndex extends ScalarIndexExtension<String> {
    private static final String TEST_DIRECTORY_PATTERN = ".*\\/[Tt]ests?\\/?.*";
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.product_types");

    @Override
    public @NotNull
    ID<String, Void> getName() {
        return KEY;
    }

    @Override
    public @NotNull
    DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new THashMap<>();
            final String filePath = inputData.getFile().getPath();

            if (filePath.matches(TEST_DIRECTORY_PATTERN)) {
                return map;
            }

            final PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }

            final XmlDocument xmlDocument = ((XmlFile) psiFile).getDocument();

            if (xmlDocument == null) {
                return map;
            }

            final XmlTag xmlRootTag = xmlDocument.getRootTag();

            if (xmlRootTag != null) {
                parseRootTag(map, xmlRootTag);
            }

            return map;
        };
    }

    private void parseRootTag(final Map<String, Void> map, final XmlTag xmlRootTag) {
        for (final XmlTag productTypeTag : xmlRootTag.findSubTags(ProductTypeXml.XML_TAG_TYPE)) {
            final String productTypeName = productTypeTag.getAttributeValue(
                    ProductTypeXml.XML_ATTRIBUTE_NAME
            );
            map.put(productTypeName, null);
        }
    }

    @Override
    public @NotNull
    KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    @SuppressWarnings({"PMD.LiteralsFirstInComparisons"})
    public FileBasedIndex.InputFilter getInputFilter() {
        return file ->
                file.getFileType() == XmlFileType.INSTANCE
                        && file.getName().equalsIgnoreCase(ProductTypeXml.FILE_NAME);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
