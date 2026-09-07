/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.json;

import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.reference.provider.FilePathReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.MagentoInitRequireJsReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.RequireJsPreferenceReferenceProvider;
import org.jetbrains.annotations.NotNull;

public class JsonReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(JsonStringLiteral.class),
                new MagentoInitRequireJsReferenceProvider(
                        new FilePathReferenceProvider(),
                        new RequireJsPreferenceReferenceProvider()
                )
        );
    }
}
