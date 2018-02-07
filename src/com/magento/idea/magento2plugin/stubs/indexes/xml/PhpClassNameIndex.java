package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.impl.DomApplicationComponent;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.php.util.PhpRegex;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PhpClassNameIndex extends ScalarIndexExtension<String> {
    private static final String CLASS_NAME_PATTERN =
            PhpRegex.CLASS_NAME + "(\\\\" + PhpRegex.CLASS_NAME + ")+";

    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.xml.php_class_name");

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> map = new THashMap<>();
            PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }

            XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
            if (xmlTags == null) {
                return map;
            }

            for (XmlTag xmlTag: xmlTags) {
                fillMap(xmlTag, map);
            }

            return map;
        };
    }

    private void fillMap(XmlTag parentTag, Map<String, Void> resultMap) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            for (XmlAttribute xmlAttribute: childTag.getAttributes()) {
                String xmlAttributeValue = xmlAttribute.getValue();
                if (xmlAttributeValue != null
                        && !xmlAttributeValue.isEmpty()
                        && xmlAttributeValue.matches(CLASS_NAME_PATTERN)
                ) {
                    resultMap.put(PhpLangUtil.toPresentableFQN(xmlAttributeValue), null);
                }
            }
            XmlTagValue childTagValue = childTag.getValue();
            String tagValue = childTagValue.getTrimmedText();
            if (!tagValue.isEmpty() && tagValue.matches(CLASS_NAME_PATTERN)) {
                resultMap.put(PhpLangUtil.toPresentableFQN(tagValue), null);
            }

            fillMap(childTag, resultMap);
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
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file ->
                file.getFileType() == XmlFileType.INSTANCE;
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
