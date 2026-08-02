/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ProcessingContext;
// CHECKSTYLE IGNORE check FOR NEXT 4 LINES
import com.magento.idea.magento2plugin.completion.provider.*;//NOPMD
import com.magento.idea.magento2plugin.completion.provider.mftf.*;//NOPMD
import com.magento.idea.magento2plugin.magento.files.*;//NOPMD
import org.jetbrains.annotations.NotNull;

/**
 * XML completions that are available on every supported IntelliJ Platform IDE.
 */
@SuppressWarnings({"PMD", "checkstyle:all"})
public class XmlCoreCompletionContributor extends CompletionContributor {

    public XmlCoreCompletionContributor() {
        registerModuleCompletions();
        registerLayoutAndUiCompletions();
        registerMftfCompletions();
        registerMenuAndSchemaCompletions();
    }

    private void registerModuleCompletions() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleAclXml.XML_ATTR_ID))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .endsWith(ModuleAclXml.FILE_NAME))),
                new ModuleNameCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleXml.MODULE_ATTR_NAME))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .endsWith(ModuleXml.FILE_NAME))),
                new ModuleNameCompletionProvider()
        );
    }

    private void registerLayoutAndUiCompletions() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(LayoutXml.XML_ATTRIBUTE_TEMPLATE)),
                new FilePathCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(LayoutXml.NAME_ATTRIBUTE)
                                .withParent(XmlPatterns.xmlTag().withName(
                                        LayoutXml.UI_COMPONENT_TAG_NAME))),
                new UiComponentCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(
                                XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM).withChild(
                                        XmlPatterns.xmlAttribute().withValue(StandardPatterns.string()
                                                .matches(UiComponentXml.XML_ATTRIBUTE_TEMPLATE))))),
                new FilePathCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("name")
                                .withParent(XmlPatterns.xmlTag().withName("referenceContainer"))),
                new LayoutContainerCompletionContributor()
        );
        extend(CompletionType.BASIC, XmlPatterns.or(
                        PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                                .inside(XmlPatterns.xmlAttribute().withName("name")
                                        .withParent(XmlPatterns.xmlTag().withName("referenceBlock"))),
                        PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                                .inside(XmlPatterns.xmlAttribute()
                                        .withName(StandardPatterns.string().oneOf("before", "after"))
                                        .withParent(XmlPatterns.xmlTag().withName("block"))),
                        PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                                .inside(XmlPatterns.xmlAttribute().withName(StandardPatterns.string()
                                                .oneOf("before", "after", "destination", "element"))
                                        .withParent(XmlPatterns.xmlTag().withName("move"))),
                        PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                                .inside(XmlPatterns.xmlAttribute().withName("name")
                                        .withParent(XmlPatterns.xmlTag().withName("remove")))),
                new LayoutBlockCompletionContributor()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("handle")
                                .withParent(XmlPatterns.xmlTag().withName("update"))),
                new LayoutUpdateCompletionContributor()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttributeValue().withParent(
                                XmlPatterns.xmlAttribute().withName(
                                        UiComponentXml.XML_ATTRIBUTE_COMPONENT))),
                new CompositeCompletionProvider(
                        new RequireJsMappingCompletionProvider(),
                        new FilePathCompletionProvider()
                )
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(
                                XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM)
                                        .withChild(XmlPatterns.xmlAttribute().withValue(
                                                StandardPatterns.string().matches(
                                                        UiComponentXml.XML_ATTRIBUTE_COMPONENT)))
                                        .withChild(XmlPatterns.xmlAttribute().withName(
                                                UiComponentXml.XML_ATTRIBUTE_NAME)))),
                new CompositeCompletionProvider(
                        new RequireJsMappingCompletionProvider(),
                        new FilePathCompletionProvider()
                )
        );
    }

    private void registerMftfCompletions() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(StandardPatterns.string()
                                        .oneOf("ref", "extends"))
                                .withParent(XmlPatterns.xmlTag().withName("actionGroup"))),
                new ActionGroupCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(StandardPatterns.string()
                                        .oneOf("entity", "value", "userInput"))
                                .without(new PatternCondition<XmlAttribute>(
                                        "value attribute of general text tag") {
                                    @Override
                                    public boolean accepts(
                                            final @NotNull XmlAttribute attribute,
                                            final ProcessingContext context
                                    ) {
                                        return attribute.getParent().getName()
                                                .matches("stories|title|description");
                                    }
                                })),
                new DataCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName("extends")
                                .withParent(XmlPatterns.xmlTag().withName("entity"))),
                new DataCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(MftfTest.EXTENDS_ATTRIBUTE)
                                .withParent(XmlPatterns.xmlTag().withName(MftfTest.TEST_TAG)
                                        .withParent(XmlPatterns.xmlTag().withName(
                                                MftfTest.ROOT_TAG)))),
                new TestNameCompletionProvider()
        );

        for (int nesting = 1; nesting < 10; nesting++) {
            extend(CompletionType.BASIC,
                    PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                            .inside(XmlPatterns.xmlAttribute()
                                    .withName(MftfActionGroup.SELECTOR_ATTRIBUTE)
                                    .withSuperParent(nesting, XmlPatterns.xmlTag().withParent(
                                            XmlPatterns.xmlTag().withName(StandardPatterns.string()
                                                    .oneOf(MftfActionGroup.ROOT_TAG,
                                                            MftfTest.TEST_TAG))))),
                    new SelectorCompletionProvider()
            );
            extend(CompletionType.BASIC,
                    PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                            .inside(XmlPatterns.xmlAttribute().withName(MftfActionGroup.URL_ATTRIBUTE)
                                    .withSuperParent(nesting, XmlPatterns.xmlTag().withParent(
                                            XmlPatterns.xmlTag().withName(StandardPatterns.string()
                                                    .oneOf(MftfActionGroup.ROOT_TAG,
                                                            MftfTest.TEST_TAG))))),
                    new PageCompletionProvider()
            );
        }
    }

    private void registerMenuAndSchemaCompletions() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleMenuXml.parentTagAttribute)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleMenuXml.addTag)
                                        .withParent(XmlPatterns.xmlTag().withName(
                                                ModuleMenuXml.menuTag))))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleMenuXml.fileName))),
                new MenuCompletionProvider()
        );
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleDbSchemaXml.XML_ATTR_TABLE_NAME)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleDbSchemaXml.XML_TAG_TABLE)
                                        .withParent(XmlPatterns.xmlTag().withName(
                                                ModuleDbSchemaXml.XML_TAG_SCHEMA))))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleDbSchemaXml.FILE_NAME))),
                new TableNameCompletionProvider()
        );
        registerConstraintCompletion(
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_TABLE_NAME,
                new TableNameCompletionProvider()
        );
        registerConstraintCompletion(
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME,
                new TableNameCompletionProvider()
        );
        registerConstraintCompletion(
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_COLUMN_NAME,
                new ColumnNameCompletionProvider()
        );
        registerConstraintCompletion(
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME,
                new ColumnNameCompletionProvider()
        );
    }

    private void registerConstraintCompletion(
            final String attributeName,
            final CompletionProvider<CompletionParameters> provider
    ) {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(attributeName)
                                .withParent(XmlPatterns.xmlTag().withName(
                                                ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                                        .withParent(XmlPatterns.xmlTag().withName(
                                                ModuleDbSchemaXml.XML_TAG_TABLE))))
                        .inFile(XmlPatterns.xmlFile().withName(StandardPatterns.string()
                                .matches(ModuleDbSchemaXml.FILE_NAME))),
                provider
        );
    }
}
