/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleNameReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

        List<PsiReference> psiReferences = new ArrayList<>();
        String origValue = element.getText();

        Pattern pattern = Pattern.compile("(([A-Z][a-zA-Z0-9]+)_([A-Z][a-zA-Z0-9]+))");
        Matcher matcher = pattern.matcher(element.getText());
        if (!matcher.find()) {
            return PsiReference.EMPTY_ARRAY;
        }

        String moduleName = matcher.group(1);

        Collection<VirtualFile> moduleFiles = FileBasedIndex.getInstance()
                .getContainingFiles(ModuleNameIndex.KEY, moduleName,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(element.getProject()),
                                PhpFileType.INSTANCE
                        )
                );

        PsiManager psiManager = PsiManager.getInstance(element.getProject());
        List<PsiElement> psiElements = new ArrayList<>();

        for (VirtualFile moduleVf : moduleFiles) {
            if (moduleVf.getParent() == null) {
                continue;
            }
            VirtualFile moduleSourceVf = moduleVf.getParent();
            if (!moduleSourceVf.isDirectory()) {
                continue;
            }

            PsiDirectory moduleSourceDirectory = psiManager.findDirectory(moduleSourceVf);
            if (null == moduleSourceDirectory) {
                continue;
            }

            psiElements.add(moduleSourceDirectory);
        }

        if (psiElements.size() > 0) {
            TextRange range = new TextRange(
                origValue.indexOf(moduleName), origValue.indexOf(moduleName) + moduleName.length()
            );
            psiReferences.add(new PolyVariantReferenceBase(element, range, psiElements));
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }
}
