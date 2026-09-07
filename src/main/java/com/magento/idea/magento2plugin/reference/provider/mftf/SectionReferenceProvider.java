/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.mftf;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.*;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.SectionIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SectionReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        List<PsiReference> psiReferences = new ArrayList<>();

        String origValue = StringUtil.unquoteString(element.getText());
        String modifiedValue = origValue.replaceAll(".*\\{{2}([_A-Za-z0-9.]+)(\\([^}]+\\))?\\}{2}.*", "$1").toString();

        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
            .getContainingFiles(
                SectionIndex.KEY,
                modifiedValue,
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                    GlobalSearchScope.allScope(element.getProject()),
                    XmlFileType.INSTANCE
                )
            );

        PsiManager psiManager = PsiManager.getInstance(element.getProject());

        List<PsiElement> psiElements = new ArrayList<>();

        for (VirtualFile virtualFile: containingFiles) {
            XmlFile xmlFile = (XmlFile) psiManager.findFile(virtualFile);

            if (xmlFile == null) {
                continue;
            }

            String[] parts = modifiedValue.split("\\.");

            String sectionName = parts[0];
            String elementName;

            boolean isSectionNameOnly = parts.length == 1;

            if (isSectionNameOnly) {
                elementName = "";
            } else {
                elementName = parts[1];
            }

            XmlTag rootTag = xmlFile.getRootTag();

            for (XmlTag sectionTag: rootTag.findSubTags("section")) {
                if (sectionTag == null) {
                    continue;
                }

                XmlAttribute sectionNameAttribute = sectionTag.getAttribute("name");

                if (sectionNameAttribute == null ||
                    sectionNameAttribute.getValueElement() == null ||
                    !sectionNameAttribute.getValueElement().getValue().equals(sectionName)
                ) {
                    continue;
                }

                if (isSectionNameOnly) {
                    psiElements.add(sectionNameAttribute.getValueElement());
                    continue;
                }

                for (XmlTag elementTag: sectionTag.findSubTags("element")) {
                    XmlAttribute elementNameAttribute = elementTag.getAttribute("name");

                    if (elementNameAttribute != null &&
                        elementNameAttribute.getValueElement() != null &&
                        elementNameAttribute.getValueElement().getValue().equals(elementName)
                    ) {
                        psiElements.add(elementNameAttribute.getValueElement());
                    }
                }
            }
        }

        if (psiElements.size() > 0) {
            psiReferences.add(new PolyVariantReferenceBase(element, psiElements));
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }
}
