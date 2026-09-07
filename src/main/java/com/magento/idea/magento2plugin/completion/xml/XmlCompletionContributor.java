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
import com.magento.idea.magento2plugin.completion.provider.EventNameCompletionContributor;
import com.magento.idea.magento2plugin.completion.provider.PhpClassCompletionProvider;
import com.magento.idea.magento2plugin.completion.provider.PhpClassMemberCompletionProvider;
import com.magento.idea.magento2plugin.completion.provider.PhpConstructorArgumentCompletionProvider;
import com.magento.idea.magento2plugin.completion.provider.PhpJobMethodCompletionContributor;
import com.magento.idea.magento2plugin.completion.provider.PhpServiceMethodCompletionContributor;
import com.magento.idea.magento2plugin.completion.provider.VirtualTypeCompletionProvider;
import com.magento.idea.magento2plugin.magento.files.CommonXml;
import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;
import com.magento.idea.magento2plugin.magento.files.ModuleConfigXml;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;
import com.magento.idea.magento2plugin.magento.files.ModuleWidgetXml;

/**
 * XML completions that require PHP PSI support.
 */
@SuppressWarnings({"PMD", "checkstyle:all"})
public class XmlCompletionContributor extends CompletionContributor {

    public XmlCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withChild(
                                XmlPatterns.xmlAttribute().withName(CommonXml.SCHEMA_VALIDATE_ATTRIBUTE)
                                        .withValue(StandardPatterns.string().oneOf(CommonXml.INIT_PARAMETER))))
                        ),
                new PhpClassMemberCompletionProvider()
        );

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withChild(
                                XmlPatterns.xmlAttribute().withName(CommonXml.SCHEMA_VALIDATE_ATTRIBUTE)
                                        .withValue(StandardPatterns.string().oneOf(CommonXml.OBJECT))))
                        ),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(CommonXml.ATTR_CLASS)),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleDiXml.PREFERENCE_ATTR_FOR)),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleDiXml.NAME_ATTR)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleDiXml.TYPE_TAG))),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleDiXml.TYPE_ATTR)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleDiXml.PLUGIN_TAG_NAME))),
                new PhpClassCompletionProvider()
        );

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("type")),
                new VirtualTypeCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("name")
                                .withParent(XmlPatterns.xmlTag().withName("virtualType"))),
                new VirtualTypeCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withChild(
                                XmlPatterns.xmlAttribute().withName("xsi:type")
                                        .withValue(StandardPatterns.string().oneOf("object"))))
                        ),
                new VirtualTypeCompletionProvider()
        );

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("name")
                                .withParent(XmlPatterns.xmlTag().withName("argument")
                                        .withParent(XmlPatterns.xmlTag().withName("arguments"))))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string().endsWith("di.xml"))),
                new PhpConstructorArgumentCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleEventsXml.INSTANCE_ATTRIBUTE)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleEventsXml.OBSERVER_TAG)))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleEventsXml.FILE_NAME))),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(CommonXml.ATTR_INSTANCE)
                                .withParent(XmlPatterns.xmlTag().withName(CrontabXmlTemplate.CRON_JOB_TAG)))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(CrontabXmlTemplate.FILE_NAME))),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .inside(XmlPatterns.xmlTag().withName(ModuleSystemXmlFile.XML_TAG_SOURCE_MODEL)
                                .withParent(XmlPatterns.xmlTag().withName(
                                        ModuleSystemXmlFile.FIELD_ELEMENT_NAME)))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleSystemXmlFile.FILE_NAME))),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .inside(XmlPatterns.xmlTag().withName(ModuleSystemXmlFile.XML_TAG_FRONTEND_MODEL)),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .inside(XmlPatterns.xmlTag().withName(ModuleSystemXmlFile.XML_TAG_BACKEND_MODEL)
                                .withParent(XmlPatterns.xmlTag().withName(
                                        ModuleSystemXmlFile.FIELD_ELEMENT_NAME)))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleSystemXmlFile.FILE_NAME))),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleConfigXml.XML_ATTRIBUTE_BACKEND_MODEL))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleConfigXml.FILE_NAME))),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleWidgetXml.ATTRIBUTE_SOURCE_MODEL_NAME)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleWidgetXml.TAG_PARAMETER_NAME)
                                        .withParent(XmlPatterns.xmlTag().withName(
                                                ModuleWidgetXml.TAG_PARAMETERS_NAME))))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleWidgetXml.FILE_NAME))),
                new PhpClassCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("method")
                                .withParent(XmlPatterns.xmlTag().withName("service")))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string().endsWith("webapi.xml"))),
                new PhpServiceMethodCompletionContributor()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("method")
                                .withParent(XmlPatterns.xmlTag().withName("job")))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string().matches("crontab.xml"))),
                new PhpJobMethodCompletionContributor()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("name")
                                .withParent(XmlPatterns.xmlTag().withName("event")))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string().endsWith("events.xml"))),
                new EventNameCompletionContributor()
        );
    }
}
