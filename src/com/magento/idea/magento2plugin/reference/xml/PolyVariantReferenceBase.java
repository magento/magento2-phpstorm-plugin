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
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class PolyVariantReferenceBase extends PsiPolyVariantReferenceBase<PsiElement> {

    /**
     * Target elements.
     */
    private Collection<? extends PsiElement> targets;

    public PolyVariantReferenceBase(PsiElement element, Collection<? extends PsiElement> targets) {
        super(element);
        this.targets = targets;
    }

    public PolyVariantReferenceBase(
            PsiElement element,
            TextRange range,
            Collection<? extends PsiElement> targets
    ) {
        super(element, range);
        this.targets = targets;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        ResolveResult[] resolveResults = new ResolveResult[targets.size()];

        int i = 0;
        for (PsiElement target : targets) {
            resolveResults[i++] = new PsiElementResolveResult(target);
        }
        return resolveResults;
    }
}
