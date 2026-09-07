/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.lang.php;

import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.PhpMultipleDeclarationFilter;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class MagentoProxyDeclarationFilter implements PhpMultipleDeclarationFilter {

    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.ConfusingTernary"})
    @Override
    public <E extends PhpNamedElement> Collection<E> filter(
            final @NotNull PsiElement psiElement,
            final Collection<E> candidates
    ) {
        if (!Settings.isEnabled(psiElement.getProject())) {
            return candidates;
        } else if (!Registry.is("php.use.multiproject.ref.resolver", true)) {
            return candidates;
        } else if (candidates.size() == 1) { // NOPMD
            return candidates;
        } else if (psiElement.getContainingFile() == null) {
            return candidates;
        }

        return ContainerUtil.filter(candidates,
                (candidate) -> {
                    final PsiFile file = candidate.getContainingFile();

                    if (file == null) {
                        return false;
                    }
                    final VirtualFile virtualFile = file.getVirtualFile();

                    if (virtualFile == null) {
                        return false;
                    }

                    return !virtualFile.getPath().contains("/generated/") && isValidFqn(candidate);
                });
    }

    private <E extends PhpNamedElement> boolean isValidFqn(final E candidate) {
        PhpClass targetClass = null;

        if (candidate instanceof Method) {
            targetClass = ((Method) candidate).getContainingClass();
        } else if (candidate instanceof PhpClass) {
            targetClass = (PhpClass) candidate;
        }

        if (targetClass == null) {
            return true;
        }

        return PhpLangUtil.isFqn(targetClass.getFQN()) && !targetClass.getFQN().endsWith("\\");
    }
}
