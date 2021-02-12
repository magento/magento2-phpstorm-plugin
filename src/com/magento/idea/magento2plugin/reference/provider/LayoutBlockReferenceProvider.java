/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.LayoutIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class LayoutBlockReferenceProvider  extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final String value = StringUtil.unquoteString(element.getText());
        final List<XmlTag> targets = LayoutIndex.getBlockDeclarations(value, element.getProject());
        final List<PsiReference> psiReferences = new ArrayList<>();

        if (!targets.isEmpty()) {
            psiReferences.add(new PolyVariantReferenceBase(element, targets));
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
