/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolver;
import org.jetbrains.annotations.NotNull;

public class PhpImplementResolverClassQuickFix implements LocalQuickFix {
    @NotNull
    @Override
    public String getFamilyName() {
        return "Implements Resolver interface";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement correctInterface = PhpPsiElementFactory.createImplementsList(project, GraphQlResolver.RESOLVER_INTERFACE);
        PhpClass graphQlResolverClass = (PhpClass) descriptor.getPsiElement().getParent();
        graphQlResolverClass.getImplementsList().replace(correctInterface);
    }
}
