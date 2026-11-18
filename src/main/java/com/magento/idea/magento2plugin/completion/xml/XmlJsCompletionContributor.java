/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.completion.provider.RequireJsMappingCompletionProvider;
import com.magento.idea.magento2plugin.magento.files.UiComponentXml;

/** RequireJS alias completions supplement the shared filesystem completions when JavaScript is installed. */
public class XmlJsCompletionContributor extends CompletionContributor {
    public XmlJsCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttributeValue().withParent(
                                XmlPatterns.xmlAttribute().withName(
                                        UiComponentXml.XML_ATTRIBUTE_COMPONENT))),
                new RequireJsMappingCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(
                                XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM)
                                        .withChild(XmlPatterns.xmlAttribute().withValue(
                                                StandardPatterns.string().matches(
                                                        UiComponentXml.XML_ATTRIBUTE_COMPONENT)))
                                        .withChild(XmlPatterns.xmlAttribute().withName(
                                                UiComponentXml.XML_ATTRIBUTE_NAME)))),
                new RequireJsMappingCompletionProvider()
        );
    }
}
