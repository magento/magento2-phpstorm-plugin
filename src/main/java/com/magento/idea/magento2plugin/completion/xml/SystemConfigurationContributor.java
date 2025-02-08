/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlFilePattern;
import com.intellij.patterns.XmlNamedElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.patterns.XmlTagPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.completion.provider.xml.FieldNameCompletionProvider;
import com.magento.idea.magento2plugin.completion.provider.xml.GroupNameCompletionProvider;
import com.magento.idea.magento2plugin.completion.provider.xml.SectionNameCompletionProvider;
import com.magento.idea.magento2plugin.magento.files.ModuleConfigXmlFile;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;

public class SystemConfigurationContributor extends CompletionContributor {

    /**
     * Contributes completions to the system.xml and config.xml files.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public SystemConfigurationContributor() {
        super();
        final XmlFilePattern.Capture systemXmlFileCapture = XmlPatterns
                .xmlFile()
                .withName(StandardPatterns.string().endsWith(ModuleSystemXmlFile.FILE_NAME));
        final XmlFilePattern.Capture configXmlFileCapture = XmlPatterns
                .xmlFile()
                .withName(StandardPatterns.string().endsWith(ModuleConfigXmlFile.FILE_NAME));
        final XmlNamedElementPattern.XmlAttributePattern idAttributeCapture = XmlPatterns
                .xmlAttribute()
                .withName("id");
        final XmlTagPattern.Capture sectionTagCapture = XmlPatterns
                .xmlTag()
                .withName(ModuleSystemXmlFile.SECTION_TAG_NAME);
        final XmlTagPattern.Capture groupTagCapture = XmlPatterns
                .xmlTag()
                .withName(ModuleSystemXmlFile.GROUP_TAG_NAME);
        final XmlTagPattern.Capture fieldTagCapture = XmlPatterns
                .xmlTag()
                .withName(ModuleSystemXmlFile.FIELD_TAG_NAME);

        // <section id="completion"/> in the system.xml file
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(idAttributeCapture)
                        .inside(sectionTagCapture)
                        .inFile(systemXmlFileCapture)
                        .withSuperParent(3, sectionTagCapture),
                new SectionNameCompletionProvider()
        );

        // <group id="completion"/> in the system.xml file
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(idAttributeCapture)
                        .inside(groupTagCapture)
                        .inside(sectionTagCapture)
                        .inFile(systemXmlFileCapture)
                        .withSuperParent(3, groupTagCapture),
                new GroupNameCompletionProvider()
        );

        // <field id="completion"/> in the system.xml file
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(idAttributeCapture)
                        .inside(fieldTagCapture)
                        .inside(groupTagCapture)
                        .inside(sectionTagCapture)
                        .inFile(systemXmlFileCapture)
                        .withSuperParent(3, fieldTagCapture),
                new FieldNameCompletionProvider()
        );

        final PsiElementPattern.Capture<PsiElement> tagTextInConfigFile = PlatformPatterns
                .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .inside(XmlPatterns.xmlTag().withName(ModuleConfigXmlFile.ROOT_TAG_NAME))
                .inFile(configXmlFileCapture);
        final XmlTagPattern.Capture defaultScopeTag = XmlPatterns.xmlTag()
                .withName(ModuleConfigXmlFile.DEFAULT_SCOPE);
        final XmlTagPattern.Capture websiteScopeTag = XmlPatterns.xmlTag()
                .withName(ModuleConfigXmlFile.WEBSITE_SCOPE);
        final XmlTagPattern.Capture storeScopeTag = XmlPatterns.xmlTag()
                .withName(ModuleConfigXmlFile.STORE_SCOPE);

        // <completion/> for sections in the config.xml file
        extend(
                CompletionType.BASIC,
                PlatformPatterns.or(
                        tagTextInConfigFile.withSuperParent(2, defaultScopeTag),
                        tagTextInConfigFile.withSuperParent(3, websiteScopeTag),
                        tagTextInConfigFile.withSuperParent(3, storeScopeTag)
                ),
                new SectionNameCompletionProvider()
        );

        // <completion/> for groups in the config.xml file
        extend(
                CompletionType.BASIC,
                PlatformPatterns.or(
                        tagTextInConfigFile.withSuperParent(3, defaultScopeTag),
                        tagTextInConfigFile.withSuperParent(4, websiteScopeTag),
                        tagTextInConfigFile.withSuperParent(4, storeScopeTag)
                ),
                new GroupNameCompletionProvider()
        );

        // <completion/> for fields in the config.xml file
        extend(
                CompletionType.BASIC,
                PlatformPatterns.or(
                        tagTextInConfigFile.withSuperParent(4, defaultScopeTag),
                        tagTextInConfigFile.withSuperParent(5, websiteScopeTag),
                        tagTextInConfigFile.withSuperParent(5, storeScopeTag)
                ),
                new FieldNameCompletionProvider()
        );
    }
}
