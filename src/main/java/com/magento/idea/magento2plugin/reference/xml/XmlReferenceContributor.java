/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import static com.intellij.patterns.XmlPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlFile;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.reference.provider.CompositeReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.EventNameReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.ObserverNameReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.PhpClassMemberReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.PhpClassReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.PhpConstructorArgumentReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.PhpJobMethodReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.PhpServiceMethodReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.PluginReferenceProvider;
import com.magento.idea.magento2plugin.reference.provider.VirtualTypeReferenceProvider;
import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

/**
 * XML references that require PHP PSI support.
 */
public class XmlReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withValue(
                        string().matches(RegExUtil.XmlRegex.CLASS_ELEMENT)),
                new CompositeReferenceProvider(
                        new PhpClassReferenceProvider(),
                        new PhpClassMemberReferenceProvider()
                )
        );
        registrar.registerReferenceProvider(
                XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withText(string().matches(RegExUtil.XmlRegex.CLASS_ELEMENT)),
                new CompositeReferenceProvider(
                        new PhpClassReferenceProvider(),
                        new PhpClassMemberReferenceProvider()
                )
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("type"))
                        .inFile(xmlFile().withName(string().endsWith("di.xml"))),
                new VirtualTypeReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withParent(
                        XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withChild(
                                XmlPatterns.xmlAttribute().withName("xsi:type"))))
                        .inFile(xmlFile().withName(string().endsWith("di.xml"))),
                new VirtualTypeReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("argument").withParent(
                                        XmlPatterns.xmlTag().withName("arguments"))))
                        .inFile(xmlFile().withName(string().endsWith("di.xml"))),
                new PhpConstructorArgumentReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("method").withParent(
                                XmlPatterns.xmlTag().withName("service")))
                        .inFile(xmlFile().withName(string().endsWith("webapi.xml"))),
                new PhpServiceMethodReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("method").withParent(
                                XmlPatterns.xmlTag().withName("job")))
                        .inFile(xmlFile().withName(string().matches("crontab.xml"))),
                new PhpJobMethodReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("event"))),
                new EventNameReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("observer"))),
                new ObserverNameReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("plugin").withChild(
                                        XmlPatterns.xmlAttribute().withName("disabled"))))
                        .inFile(xmlFile().withName(string().matches(ModuleDiXml.FILE_NAME))),
                new PluginReferenceProvider()
        );
    }
}
