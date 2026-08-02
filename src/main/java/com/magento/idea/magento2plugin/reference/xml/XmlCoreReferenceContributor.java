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
import com.magento.idea.magento2plugin.magento.files.MftfActionGroup;
import com.magento.idea.magento2plugin.magento.files.MftfTest;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
// CHECKSTYLE IGNORE check FOR NEXT 3 LINES
import com.magento.idea.magento2plugin.reference.provider.*;//NOPMD
import com.magento.idea.magento2plugin.reference.provider.mftf.*;//NOPMD
import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;

/**
 * XML references that are available on every supported IntelliJ Platform IDE.
 */
@SuppressWarnings({"PMD", "checkstyle:all"})
public class XmlCoreReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
        registerLayoutReferences(registrar);
        registerModuleAndFileReferences(registrar);
        registerMftfReferences(registrar);
        registerMenuAndSchemaReferences(registrar);
    }

    private void registerLayoutReferences(final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("referenceContainer"))),
                new LayoutContainerReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.or(
                        XmlPatterns.xmlAttributeValue().withParent(
                                XmlPatterns.xmlAttribute().withName("name").withParent(
                                        XmlPatterns.xmlTag().withName("referenceBlock"))),
                        XmlPatterns.xmlAttributeValue().withParent(
                                XmlPatterns.xmlAttribute().withName(
                                                string().oneOf("before", "after"))
                                        .withParent(XmlPatterns.xmlTag().withName("block"))),
                        XmlPatterns.xmlAttributeValue().withParent(
                                XmlPatterns.xmlAttribute().withName(string()
                                                .oneOf("before", "after", "destination", "element"))
                                        .withParent(XmlPatterns.xmlTag().withName("move"))),
                        XmlPatterns.xmlAttributeValue().withParent(
                                XmlPatterns.xmlAttribute().withName("name").withParent(
                                        XmlPatterns.xmlTag().withName("remove")))),
                new LayoutBlockReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("handle").withParent(
                                XmlPatterns.xmlTag().withName("update"))),
                new LayoutUpdateReferenceProvider()
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("uiComponent"))),
                new UIComponentReferenceProvider()
        );
    }

    private void registerModuleAndFileReferences(final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withValue(string()
                        .matches(".*[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+.*")),
                new CompositeReferenceProvider(new ModuleNameReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withText(string()
                        .matches(".*[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+.*")),
                new CompositeReferenceProvider(new ModuleNameReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withValue(string()
                        .matches(".*\\W([\\w-]+/)*[\\w\\.-]+.*")),
                new CompositeReferenceProvider(new FilePathReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withText(string()
                        .matches(".*\\W([\\w-]+/)*[\\w\\.-]+.*")),
                new CompositeReferenceProvider(new FilePathReferenceProvider())
        );
    }

    private void registerMftfReferences(final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withValue(string()
                                .matches(RegExUtil.Magento.MFTF_CURLY_BRACES))
                        .withParent(XmlPatterns.xmlAttribute().withName(
                                MftfActionGroup.USER_INPUT_TAG)),
                new CompositeReferenceProvider(new DataReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute()
                        .withName(MftfActionGroup.ENTITY_ATTRIBUTE)
                        .withParent(XmlPatterns.xmlTag().withName(string().oneOf(
                                MftfActionGroup.CREATE_DATA_TAG,
                                MftfActionGroup.UPDATE_DATA_TAG)))),
                new CompositeReferenceProvider(new DataReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("ref").withParent(
                                XmlPatterns.xmlTag().withName("actionGroup"))),
                new CompositeReferenceProvider(new ActionGroupReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("extends").withParent(
                                XmlPatterns.xmlTag().withName("actionGroup"))),
                new CompositeReferenceProvider(new ActionGroupReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("extends").withParent(
                                XmlPatterns.xmlTag().withName("entity"))),
                new CompositeReferenceProvider(new DataReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName(MftfTest.EXTENDS_ATTRIBUTE)
                                .withParent(XmlPatterns.xmlTag().withName(MftfTest.TEST_TAG)
                                        .withParent(XmlPatterns.xmlTag().withName(
                                                MftfTest.ROOT_TAG)))),
                new CompositeReferenceProvider(new TestNameReferenceProvider())
        );
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName(MftfTest.NAME_ATTRIBUTE)
                                .withParent(XmlPatterns.xmlTag().withName(MftfTest.TEST_TAG)
                                        .withParent(XmlPatterns.xmlTag().withName(
                                                MftfTest.ROOT_TAG)))),
                new CompositeReferenceProvider(new TestExtendedByReferenceProvider())
        );

        for (int nesting = 1; nesting < 10; nesting++) {
            registrar.registerReferenceProvider(
                    XmlPatterns.xmlAttributeValue().withValue(string()
                                    .matches(RegExUtil.Magento.MFTF_CURLY_BRACES))
                            .withParent(XmlPatterns.xmlAttribute().withName(
                                            MftfActionGroup.URL_ATTRIBUTE)
                                    .withParent(XmlPatterns.xmlTag().withSuperParent(
                                            nesting,
                                            XmlPatterns.xmlTag().withName(string().oneOf(
                                                    MftfActionGroup.ROOT_TAG,
                                                    MftfTest.TEST_TAG))))),
                    new CompositeReferenceProvider(new PageReferenceProvider())
            );
            registrar.registerReferenceProvider(
                    XmlPatterns.xmlAttributeValue().withValue(string()
                                    .matches(RegExUtil.Magento.MFTF_CURLY_BRACES))
                            .withParent(XmlPatterns.xmlAttribute().withName(
                                            MftfActionGroup.SELECTOR_ATTRIBUTE)
                                    .withParent(XmlPatterns.xmlTag().withSuperParent(
                                            nesting,
                                            XmlPatterns.xmlTag().withName(string().oneOf(
                                                    MftfActionGroup.ROOT_TAG,
                                                    MftfTest.TEST_TAG))))),
                    new CompositeReferenceProvider(new SectionReferenceProvider())
            );
        }
    }

    private void registerMenuAndSchemaReferences(final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName(ModuleMenuXml.parentTagAttribute)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleMenuXml.addTag)))
                        .inFile(xmlFile().withName(string().endsWith(ModuleMenuXml.fileName))),
                new MenuReferenceProvider()
        );
        registerSchemaReference(
                registrar,
                ModuleDbSchemaXml.XML_ATTR_TABLE_NAME,
                ModuleDbSchemaXml.XML_TAG_TABLE
        );
        registerSchemaReference(
                registrar,
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_TABLE_NAME,
                ModuleDbSchemaXml.XML_TAG_CONSTRAINT
        );
        registerSchemaReference(
                registrar,
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME,
                ModuleDbSchemaXml.XML_TAG_CONSTRAINT
        );
        registerSchemaReference(
                registrar,
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_COLUMN_NAME,
                ModuleDbSchemaXml.XML_TAG_CONSTRAINT
        );
        registerSchemaReference(
                registrar,
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME,
                ModuleDbSchemaXml.XML_TAG_CONSTRAINT
        );
    }

    private void registerSchemaReference(
            final PsiReferenceRegistrar registrar,
            final String attributeName,
            final String tagName
    ) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName(attributeName)
                                .withParent(XmlPatterns.xmlTag().withName(tagName)))
                        .inFile(xmlFile().withName(string().matches(
                                ModuleDbSchemaXml.FILE_NAME))),
                new TableColumnNamesReferenceProvider()
        );
    }
}
