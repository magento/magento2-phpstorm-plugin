package com.magento.idea.magento2plugin.stubs.indexes.mftf;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.magento.files.MftfTest;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class TestNameIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY = ID.create(
        "com.magento.idea.magento2plugin.stubs.indexes.mftf.test_name_index"
    );

    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> map = new THashMap<>();
            PsiFile psiFile = inputData.getPsiFile();
            Project project = psiFile.getProject();

            if (!Settings.isEnabled(project) || !Settings.isMftfSupportEnabled(project)) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }

            XmlDocument xmlDocument = ((XmlFile) psiFile).getDocument();

            if (xmlDocument == null) {
                return map;
            }

            XmlTag xmlRootTag = xmlDocument.getRootTag();

            if (xmlRootTag == null || !xmlRootTag.getName().equals(MftfTest.ROOT_TAG)) {
                return map;
            }

            XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);

            if (xmlTags == null) {
                return map;
            }

            for (XmlTag childTag : xmlRootTag.getSubTags()) {
                String name = childTag.getAttributeValue(MftfTest.NAME_ATTRIBUTE);
                if (childTag.getAttributeValue(MftfTest.NAME_ATTRIBUTE) == null ||
                    childTag.getAttributeValue(MftfTest.NAME_ATTRIBUTE).isEmpty()
                ) {
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
                file.getFileType() == XmlFileType.INSTANCE &&
                file.getPath().contains(MftfTest.FILE_DIR_PARENTS);
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
