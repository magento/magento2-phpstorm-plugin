/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class LayoutContainerReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String value = StringUtil.unquoteString(element.getText());
        List<XmlTag> targets = LayoutIndex.getContainerDeclarations(value, element.getProject());
        if (targets.size() > 0) {
            return new PsiReference[] {new PolyVariantReferenceBase(element, targets)};
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
