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
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventNameReferenceProvider  extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String value = StringUtil.unquoteString(element.getText());
        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
            .getContainingFiles(EventNameIndex.KEY, value,
                    GlobalSearchScope.getScopeRestrictedByFileTypes(
                            GlobalSearchScope.allScope(element.getProject()),
                            PhpFileType.INSTANCE
                    )
            );

        PsiManager psiManager = PsiManager.getInstance(element.getProject());
        for (VirtualFile virtualFile: containingFiles) {
            PhpFile phpFile = (PhpFile) psiManager.findFile(virtualFile);
            if (phpFile != null) {
                List<PsiElement> psiElements = new ArrayList<>();
                recursiveFill(psiElements, phpFile, value);
                if (psiElements.size() > 0) {
                    return new PsiReference[] {new PolyVariantReferenceBase(element, psiElements)};
                }
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }

    private void recursiveFill(List<PsiElement> psiElements, PsiElement psiElement, String typeName) {
        if (PhpPatternsHelper.STRING_METHOD_ARGUMENT.accepts(psiElement)) {
            if (StringUtil.unquoteString(psiElement.getText()).equals(typeName)) {
                psiElements.add(psiElement);
            }
            return;
        }

        for (PsiElement child: psiElement.getChildren()) {
            recursiveFill(psiElements, child, typeName);
        }
    }
}
