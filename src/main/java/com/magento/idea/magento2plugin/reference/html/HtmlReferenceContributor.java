/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.html;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlComment;
import com.magento.idea.magento2plugin.reference.provider.KnockoutRegionReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.KnockoutTemplateUsageReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.MagentoInitHostRequireJsReferenceProvider;
import org.jetbrains.annotations.NotNull;

public class HtmlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(),
                new MagentoInitHostRequireJsReferenceProvider()
        );

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(XmlComment.class)
                        .withText(StandardPatterns.string().contains("getRegion")),
                new KnockoutRegionReferenceProvider()
        );
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(XmlAttributeValue.class)
                        .withText(StandardPatterns.string().contains("getRegion")),
                new KnockoutRegionReferenceProvider()
        );

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement().withText(StandardPatterns.string().matches(
                        "(?s).*(template:|getTemplate|childTemplate).*"
                )),
                new KnockoutTemplateUsageReferenceProvider()
        );
    }
}
