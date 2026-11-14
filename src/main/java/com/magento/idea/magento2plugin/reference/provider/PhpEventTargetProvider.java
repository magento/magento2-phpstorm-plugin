/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex;
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves event references to PHP dispatch method calls when the PHP plugin is present.
 */
public class PhpEventTargetProvider implements EventTargetProvider {

    @Override
    public void collectEventTargets(
            final @NotNull Project project,
            final @NotNull String eventName,
            final @NotNull Collection<PsiElement> targets
    ) {
        final Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        EventNameIndex.KEY,
                        eventName,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(project),
                                PhpFileType.INSTANCE
                        )
                );

        final PsiManager psiManager = PsiManager.getInstance(project);
        for (final VirtualFile virtualFile : containingFiles) {
            final PsiFile psiFile = psiManager.findFile(virtualFile);
            if (psiFile instanceof PhpFile) {
                recursiveFill(targets, psiFile, eventName);
            }
        }
    }

    private void recursiveFill(
            final Collection<PsiElement> psiElements,
            final PsiElement psiElement,
            final String typeName
    ) {
        if (PhpPatternsHelper.STRING_METHOD_ARGUMENT.accepts(psiElement)) {
            if (StringUtil.unquoteString(psiElement.getText()).equals(typeName)) {
                psiElements.add(psiElement);
            }
            return;
        }

        for (final PsiElement child : psiElement.getChildren()) {
            recursiveFill(psiElements, child, typeName);
        }
    }
}
