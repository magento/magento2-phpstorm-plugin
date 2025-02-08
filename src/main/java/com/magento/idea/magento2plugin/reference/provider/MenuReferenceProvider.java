/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        final List<PsiReference> psiReferences = new ArrayList<>();
        final String identifier = ((XmlAttributeValueImpl) element).getValue();

        final Collection<VirtualFile> moduleFiles = FileBasedIndex.getInstance()
                .getContainingFiles(MenuIndex.KEY, identifier,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(element.getProject()),
                                XmlFileType.INSTANCE
                        )
                );

        final List<PsiElement> psiElements = new ArrayList<>();

        final PsiManager psiManager = PsiManager.getInstance(element.getProject());
        for (final VirtualFile moduleVf : moduleFiles) {

            final XmlDocument xmlDocument = ((XmlFile) Objects.requireNonNull(
                    psiManager.findFile(moduleVf))
            ).getDocument();
            if (xmlDocument != null) {
                final XmlTag xmlRootTag = xmlDocument.getRootTag();
                if (xmlRootTag != null) {
                    final XmlTag addTag = getAddTagByIdentifier(identifier, xmlRootTag);
                    psiElements.add(addTag);
                }
            }
        }

        if (!psiElements.isEmpty()) {
            psiReferences.add(
                    new PolyVariantReferenceBase(element, psiElements)
            );
        }

        return psiReferences.toArray(new PsiReference[0]);
    }

    protected XmlTag getAddTagByIdentifier(final String identifier, final XmlTag xmlRootTag) {
        if (identifier == null) {
            return null;
        }
        @Nullable final XmlTag menuTag = xmlRootTag.findFirstSubTag(ModuleMenuXml.menuTag);

        return parseMenuTag(identifier, menuTag);
    }

    private XmlTag parseMenuTag(final String identifier, final XmlTag menuTag) {
        for (final XmlTag addTag : menuTag.findSubTags(ModuleMenuXml.addTag)) {
            if (addTag.getAttributeValue(ModuleMenuXml.idTagAttribute).equals(identifier)) {
                return addTag;
            }
        }
        return null;
    }
}
