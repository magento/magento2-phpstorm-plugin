/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.stubs.indexes.xml.PhpClassNameIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class XmlIndex {

    private XmlIndex() {
    }

    /**
     * Get PHP class declarations in the *.xml files.
     *
     * @param phpClass PhpClass
     *
     * @return List[XmlTag]
     */
    public static List<XmlTag> getPhpClassDeclarations(final PhpClass phpClass) {
        
        final List<XmlTag> result = new ArrayList<>();
        final String fqn = phpClass.getPresentableFQN();
        final PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());
        final Collection<VirtualFile> vfs = FileBasedIndex.getInstance()
                .getContainingFiles(
                        PhpClassNameIndex.KEY,
                        fqn,
                        GlobalSearchScope.allScope(phpClass.getProject())
                );

        for (final VirtualFile vf : vfs) {
            final PsiFile psiFile = psiManager.findFile(vf);

            if (!(psiFile instanceof XmlFile)) {
                continue;
            }
            final XmlFile xmlFile = (XmlFile) psiFile;
            final XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(
                    xmlFile.getFirstChild(),
                    XmlTag.class
            );

            if (xmlTags == null) {
                continue;
            }
            for (final XmlTag xmlTag: xmlTags) {
                fillList(xmlTag, fqn, result);
            }
        }

        return result;
    }

    private static void fillList(
            final XmlTag parentTag,
            final String fqn,
            final List<XmlTag> list
    ) {
        for (final XmlTag childTag: parentTag.getSubTags()) {
            for (final XmlAttribute xmlAttribute: childTag.getAttributes()) {
                String xmlAttributeValue = xmlAttribute.getValue();
                if (xmlAttributeValue != null) {
                    xmlAttributeValue = xmlAttributeValue.startsWith("\\")
                            ? xmlAttributeValue.substring(1) : xmlAttributeValue;
                    if (xmlAttributeValue.startsWith(fqn)) {
                        list.add(childTag);
                    }
                }
            }
            final XmlTagValue childTagValue = childTag.getValue();
            String tagValue = childTagValue.getTrimmedText();
            tagValue = tagValue.startsWith("\\") ? tagValue.substring(1) : tagValue;

            if (!tagValue.isEmpty() && tagValue.startsWith(fqn)) {
                list.add(childTag);
            }
            fillList(childTag, fqn, list);
        }
    }
}
