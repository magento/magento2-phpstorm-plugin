/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class PolyVariantReferenceBase extends PsiPolyVariantReferenceBase<PsiElement> {

    /**
     * Target elements.
     */
    private final Collection<? extends PsiElement> targets;

    public PolyVariantReferenceBase(
            final PsiElement element,
            final Collection<? extends PsiElement> targets
    ) {
        super(element);
        this.targets = targets;
    }

    public PolyVariantReferenceBase(
            final PsiElement element,
            final TextRange range,
            final Collection<? extends PsiElement> targets
    ) {
        super(element, range);
        this.targets = targets;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        ResolveResult[] resolveResults = new ResolveResult[targets.size()];

        int index = 0;
        for (final PsiElement target : targets) {
            resolveResults[index++] = new PsiElementResolveResult(target);//NOPMD
        }
        return resolveResults;
    }

    @SuppressWarnings("PMD.AvoidUncheckedExceptionsInSignatures")
    @Override
    public PsiElement bindToElement(
            final @NotNull PsiElement element
    ) throws IncorrectOperationException {
        return null;
    }
}
