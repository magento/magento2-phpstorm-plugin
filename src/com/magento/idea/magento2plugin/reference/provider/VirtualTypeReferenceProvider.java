/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.DiIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class VirtualTypeReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

        String value = StringUtil.unquoteString(element.getText());

        DiIndex index = DiIndex.getInstance(element.getProject());
        Collection<PsiElement> targets = index.getVirtualTypeElements(value, element.getResolveScope());

        if (!(targets.size() > 0)) {
            return PsiReference.EMPTY_ARRAY;
        }

        return new PsiReference[] {
                new PolyVariantReferenceBase(element, targets)
        };
    }
}
