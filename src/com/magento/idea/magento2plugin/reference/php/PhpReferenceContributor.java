/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.php;

import com.intellij.openapi.ui.playback.PlaybackCommand;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.magento.idea.magento2plugin.reference.provider.DataFixtureReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.EventDispatchReferenceProvider;
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;
import org.jetbrains.annotations.NotNull;

public class PhpReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // ->dispatch("event_name")
        registrar.registerReferenceProvider(
                PhpPatternsHelper.STRING_METHOD_ARGUMENT,
                new EventDispatchReferenceProvider()
        );

        /*registrar.registerReferenceProvider(
                PlatformPatterns.psiElement().withText(StandardPatterns.string().contains("hello")),
                new DataFixtureReferenceProvider()
        );*/

        registrar.registerReferenceProvider(
               PlatformPatterns.psiElement()
                   .withSuperParent(2, PlatformPatterns.psiElement(PhpDocTag.class)),
                new DataFixtureReferenceProvider()
        );
    }
}
