/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex;
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EventNameReferenceProvider  extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final String value = StringUtil.unquoteString(element.getText());
        final List<PsiReference> psiReferences = new ArrayList<>();
        final Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        EventNameIndex.KEY,
                        value,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(element.getProject()),
                                PhpFileType.INSTANCE
                        )
                );

        final PsiManager psiManager = PsiManager.getInstance(element.getProject());
        final List<PsiElement> psiElements = new ArrayList<>();
        for (final VirtualFile virtualFile: containingFiles) {
            final PhpFile phpFile = (PhpFile) psiManager.findFile(virtualFile);
            if (phpFile != null) {
                recursiveFill(psiElements, phpFile, value);
                if (!psiElements.isEmpty()) {
                    psiReferences.add(new PolyVariantReferenceBase(element, psiElements));//NOPMD
                    break;
                }
            }
        }

        return psiReferences.toArray(new PsiReference[0]);
    }

    private void recursiveFill(
            final List<PsiElement> psiElements,
            final PsiElement psiElement,
            final String typeName
    ) {
        if (PhpPatternsHelper.STRING_METHOD_ARGUMENT.accepts(psiElement)) {
            if (StringUtil.unquoteString(psiElement.getText()).equals(typeName)) {
                psiElements.add(psiElement);
            }
            return;
        }

        for (final PsiElement child: psiElement.getChildren()) {
            recursiveFill(psiElements, child, typeName);
        }
    }
}
