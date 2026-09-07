/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.text;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.reference.provider.MagentoInitPlainTextRequireJsReferenceProvider;
import org.jetbrains.annotations.NotNull;

public class TextReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(),
                new MagentoInitPlainTextRequireJsReferenceProvider()
        );
    }
}
