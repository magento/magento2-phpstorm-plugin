/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.mftf;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
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
import com.magento.idea.magento2plugin.magento.files.MftfTest;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class TestExtendsIndex extends ScalarIndexExtension<String> {

    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.mftf.test_extends_index"
    );

    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    @SuppressWarnings("PMD.CognitiveComplexity")
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new THashMap<>();
            final PsiFile psiFile = inputData.getPsiFile();
            final Project project = psiFile.getProject();

            if (!Settings.isEnabled(project)
                    || !Settings.isMftfSupportEnabled(project)
                    || !(psiFile instanceof XmlFile)
            ) {
                return map;
            }
            final XmlDocument xmlDocument = ((XmlFile) psiFile).getDocument();

            if (xmlDocument == null) {
                return map;
            }
            final XmlTag xmlRootTag = xmlDocument.getRootTag();

            if (xmlRootTag == null
                    || !xmlRootTag.getName().equals(MftfTest.ROOT_TAG)) {
                return map;
            }
            final XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(
                    psiFile.getFirstChild(),
                    XmlTag.class
            );

            if (xmlTags == null) {
                return map;
            }

            for (final XmlTag childTag : xmlRootTag.getSubTags()) {
                final String name = childTag.getAttributeValue(MftfTest.EXTENDS_ATTRIBUTE);

                if (name == null || name.isEmpty()) {
                    continue;
                }
                map.put(name, null);
            }

            return map;
        };
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
                        && file.getPath().contains(MftfTest.FILE_DIR_PARENTS);
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
