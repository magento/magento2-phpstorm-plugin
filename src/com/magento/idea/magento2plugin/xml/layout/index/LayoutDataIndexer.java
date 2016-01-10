package com.magento.idea.magento2plugin.xml.layout.index;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class LayoutDataIndexer implements DataIndexer<String, Void, FileContent> {
    private String indexTag;
    private String indexAttribute;

    public LayoutDataIndexer(String indexTag, String indexAttribute) {
        this.indexTag = indexTag;
        this.indexAttribute = indexAttribute;
    }

    @NotNull
    @Override
    public Map<String, Void> map(@NotNull FileContent fileContent) {
        Map<String, Void> map = new HashMap<>();

        PsiFile psiFile = fileContent.getPsiFile();
        if (!Settings.isEnabled(psiFile.getProject())) {
            return map;
        }

        XmlDocumentImpl document = PsiTreeUtil.getChildOfType(psiFile, XmlDocumentImpl.class);
        if(document == null) {
            return map;
        }

        XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
        if(xmlTags == null) {
            return map;
        }

        for (XmlTag xmlTag: xmlTags) {
            fillResultMap(xmlTag, map);
        }

        return map;
    }

    private void fillResultMap(XmlTag parentTag, Map<String, Void> resultMap) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            if (indexTag.equals(childTag.getName())) {
                String attributeValue = childTag.getAttributeValue(this.indexAttribute);
                if (attributeValue != null) {
                    resultMap.put(PhpLangUtil.toPresentableFQN(attributeValue), null);
                }
            }
            fillResultMap(childTag, resultMap);
        }
    }
}
