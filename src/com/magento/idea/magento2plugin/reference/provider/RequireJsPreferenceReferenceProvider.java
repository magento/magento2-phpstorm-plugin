/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.JsIndex;
import org.jetbrains.annotations.NotNull;

public class RequireJsPreferenceReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        JsIndex index = JsIndex.getInstance();
        return index.getRequireJsPreferences(element, element.getResolveScope());
    }
}
