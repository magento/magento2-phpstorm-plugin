/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.php;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.magento.idea.magento2plugin.reference.provider.DataFixtureReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.EventDispatchReferenceProvider;
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;
import org.jetbrains.annotations.NotNull;

public class PhpReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        // ->dispatch("event_name")
        registrar.registerReferenceProvider(
                PhpPatternsHelper.STRING_METHOD_ARGUMENT,
                new EventDispatchReferenceProvider()
        );

        // @magentoApiDataFixture Vendor/Module/_files/data_fixture.php
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement()
                        .withSuperParent(2, PlatformPatterns.psiElement(PhpDocTag.class)),
                new DataFixtureReferenceProvider()
        );
    }
}
