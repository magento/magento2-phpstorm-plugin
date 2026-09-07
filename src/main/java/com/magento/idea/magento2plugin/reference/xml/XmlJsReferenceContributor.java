/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.magento.files.UiComponentXml;
import com.magento.idea.magento2plugin.reference.provider.KnockoutRegionReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.KnockoutTemplatePathReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.RequireJsPathReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.UiComponentItemReferenceProvider;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.XmlPatterns.xmlFile;

public class XmlJsReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
        registerComponentReferences(registrar);
        registerTemplateReferences(registrar);
    }

    private void registerComponentReferences(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName(UiComponentXml.XML_ATTRIBUTE_COMPONENT)
                ).inFile(xmlFile()),
                new RequireJsPathReferenceProvider()
        );

        registerItemValueReferences(registrar);
    }

    private void registerTemplateReferences(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName(UiComponentXml.XML_ATTRIBUTE_TEMPLATE)
                ).inFile(xmlFile()),
                new KnockoutTemplatePathReferenceProvider()
        );

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("childTemplate")
                ).inFile(xmlFile()),
                new KnockoutTemplatePathReferenceProvider()
        );

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("displayArea")
                ).inFile(xmlFile()),
                new KnockoutRegionReferenceProvider()
        );
    }

    private void registerItemValueReferences(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.psiElement().withParent(
                        XmlPatterns.xmlText().withParent(
                                XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM)
                        )
                ).inFile(xmlFile()),
                new UiComponentItemReferenceProvider()
        );
    }
}
