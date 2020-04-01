/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class LayoutDataIndexer implements DataIndexer<String, Void, FileContent> {
    private String indexTag;

    private String indexAttribute;

    private Function<String, String> valueProcessor;

    LayoutDataIndexer(String indexTag, String indexAttribute) {
        this.indexTag = indexTag;
        this.indexAttribute = indexAttribute;
    }

    LayoutDataIndexer(String indexTag, String indexAttribute, Function<String, String> valueProcessor) {
        this(indexTag, indexAttribute);
        this.valueProcessor = valueProcessor;
    }

    @NotNull
    @Override
    public Map<String, Void> map(@NotNull FileContent fileContent) {
        Map<String, Void> map = new HashMap<>();

        PsiFile psiFile = fileContent.getPsiFile();
        if (!Settings.isEnabled(psiFile.getProject())) {
            return map;
        }

        if (!(psiFile instanceof XmlFile)) {
            return map;
        }

        XmlDocument document = ((XmlFile) psiFile).getDocument();
        if (document == null) {
            return map;
        }

        XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
        if (xmlTags == null) {
            return map;
        }

        for (XmlTag xmlTag: xmlTags) {
            fillResultMap(xmlTag, map);
        }

        return map;
    }

    private void fillResultMap(XmlTag parentTag, Map<String, Void> resultMap) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            if (childTag.getName().equals(indexTag)) {
                String attributeValue = childTag.getAttributeValue(indexAttribute);
                if (attributeValue != null) {
                    attributeValue = valueProcessor != null ? valueProcessor.apply(attributeValue) : attributeValue;
                    resultMap.put(attributeValue, null);
                }
            }
            fillResultMap(childTag, resultMap);
        }
    }
}
