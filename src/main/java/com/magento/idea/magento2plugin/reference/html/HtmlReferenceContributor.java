/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.html;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.reference.provider.KnockoutRegionReferenceProvider;
import org.jetbrains.annotations.NotNull;

public class HtmlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(),
                new KnockoutRegionReferenceProvider()
        );
    }
}
