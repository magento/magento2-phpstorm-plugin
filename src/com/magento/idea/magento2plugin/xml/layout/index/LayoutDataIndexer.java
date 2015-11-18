package com.magento.idea.magento2plugin.xml.layout.index;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class LayoutDataIndexer implements DataIndexer<String, Void, FileContent> {
    private String indexTag;

    public LayoutDataIndexer(String indexTag) {
        this.indexTag = indexTag;
    }

    @NotNull
    @Override
    public Map<String, Void> map(@NotNull FileContent fileContent) {
        Map<String, Void> map = new HashMap<>();

        PsiFile psiFile = fileContent.getPsiFile();
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
                String blockName = childTag.getAttributeValue("name");
                if (blockName != null) {
                    resultMap.put(blockName, null);
                }
            }
            fillResultMap(childTag, resultMap);
        }
    }
}
