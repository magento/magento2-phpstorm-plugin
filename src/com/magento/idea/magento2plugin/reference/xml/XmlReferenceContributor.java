/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.magento.files.MftfActionGroup;
import com.magento.idea.magento2plugin.magento.files.MftfTest;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.magento.files.UiComponentXml;
// CHECKSTYLE IGNORE check FOR NEXT 5 LINES
import com.magento.idea.magento2plugin.reference.provider.*;//NOPMD
import com.magento.idea.magento2plugin.reference.provider.mftf.*;//NOPMD
import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;
import static com.intellij.patterns.XmlPatterns.*;//NOPMD

/**
 * TODO: enable style checks after decomposition.
 */
@SuppressWarnings({"PMD", "checkstyle:all"})
public class XmlReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        // <someXmlTag someAttribute="Some\Php\ClassName[::CONST|$property|method()]" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withValue(string().matches(RegExUtil.XmlRegex.CLASS_ELEMENT)),
            new CompositeReferenceProvider(
                new PhpClassReferenceProvider(),
                new PhpClassMemberReferenceProvider()
            )
        );

        // <someXmlTag>Some\Php\ClassName[::CONST|$property|method()]</someXmlTag>
        registrar.registerReferenceProvider(
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .withText(string().matches(RegExUtil.XmlRegex.CLASS_ELEMENT)),
            new CompositeReferenceProvider(
                new PhpClassReferenceProvider(),
                new PhpClassMemberReferenceProvider()
            )
        );

        // virtual type references
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("type")).inFile(xmlFile().withName(string().endsWith("di.xml"))
            ),
            new VirtualTypeReferenceProvider()
        );
        registrar.registerReferenceProvider(
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withParent(
                XmlPatterns.xmlText().withParent(
                    XmlPatterns.xmlTag().withChild(
                        XmlPatterns.xmlAttribute().withName("xsi:type")
                    )
                )
            ).inFile(xmlFile().withName(string().endsWith("di.xml"))),
            new VirtualTypeReferenceProvider()
        );

        // arguments
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("name").withParent(
                    XmlPatterns.xmlTag().withName("argument").withParent(
                        XmlPatterns.xmlTag().withName("arguments")
                    )
                )
            ).inFile(xmlFile().withName(string().endsWith("di.xml"))),
            new PhpConstructorArgumentReferenceProvider()
        );

        // <service method="methodName"/>
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("method").withParent(
                    XmlPatterns.xmlTag().withName("service")
                )
            ).inFile(xmlFile().withName(string().endsWith("webapi.xml"))),
            new PhpServiceMethodReferenceProvider()
        );

        // <job method="methodName"/>
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("method").withParent(
                                XmlPatterns.xmlTag().withName("job")
                        )
                ).inFile(xmlFile().withName(string().matches("crontab.xml"))),
                new PhpJobMethodReferenceProvider()
        );

        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("name").withParent(
                    XmlPatterns.xmlTag().withName("referenceContainer")
                )
            ),
            new LayoutContainerReferenceProvider()
        );

        registrar.registerReferenceProvider(
            XmlPatterns.or(
                XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute().withName("name")
                    .withParent(XmlPatterns.xmlTag().withName("referenceBlock"))),
                XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute()
                        .withName(string().oneOf("before", "after"))
                    .withParent(XmlPatterns.xmlTag().withName("block"))),
                XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute()
                        .withName(string().oneOf("before", "after", "destination", "element"))
                    .withParent(XmlPatterns.xmlTag().withName("move"))),
                XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute().withName("name")
                    .withParent(XmlPatterns.xmlTag().withName("remove")))
            ),
            new LayoutBlockReferenceProvider()
        );

        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("handle").withParent(
                    XmlPatterns.xmlTag().withName("update")
                )
            ),
            new LayoutUpdateReferenceProvider()
        );

        // <uiComponent name="completion"/>
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("uiComponent")
                        )
                ),
                new UIComponentReferenceProvider()
        );

        // <event name="reference" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("name").withParent(
                    XmlPatterns.xmlTag().withName("event")
                )
            ),
            new EventNameReferenceProvider()
        );

        // <observer name="reference" />
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("name").withParent(
                                XmlPatterns.xmlTag().withName("observer")
                        )
                ),
                new ObserverNameReferenceProvider()
        );

        // <someXmlTag someAttribute="Module_Name[.*]" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withValue(string().matches(".*[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+.*")),
            new CompositeReferenceProvider(
                new ModuleNameReferenceProvider()
            )
        );

        // <someXmlTag>Module_Name[.*]</someXmlTag>
        registrar.registerReferenceProvider(
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                    .withText(string().matches(".*[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+.*")),
            new CompositeReferenceProvider(
                new ModuleNameReferenceProvider()
            )
        );

        // <someXmlTag someAttribute="path/to/some-of-file.some-ext" />
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withValue(string().matches(".*\\W([\\w-]+/)*[\\w\\.-]+.*")),
                new CompositeReferenceProvider(
                        new FilePathReferenceProvider()
                )
        );

        // <someXmlTag>path/to/some-of-file.some-ext</someXmlTag>
        registrar.registerReferenceProvider(
                XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withText(string().matches(".*\\W([\\w-]+/)*[\\w\\.-]+.*")),
                new CompositeReferenceProvider(
                        new FilePathReferenceProvider()
                )
        );

        // <someXmlTag userInput="{{someValue}}" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withValue(
                string().matches(RegExUtil.Magento.MFTF_CURLY_BRACES)
            ).withParent(XmlPatterns.xmlAttribute().withName(
                MftfActionGroup.USER_INPUT_TAG
            )),
            new CompositeReferenceProvider(
                new DataReferenceProvider()
            )
        );

        // <createData entity="SimpleProduct" />
        // <updateData entity="SimpleProduct" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute()
                .withName(MftfActionGroup.ENTITY_ATTRIBUTE)
                .withParent(XmlPatterns.xmlTag().withName(
                    string().oneOf(MftfActionGroup.CREATE_DATA_TAG, MftfActionGroup.UPDATE_DATA_TAG)
            ))),
            new CompositeReferenceProvider(
                new DataReferenceProvider()
            )
        );

        // <actionGroup ref="someActionGroup" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute().withName("ref")
                .withParent(XmlPatterns.xmlTag().withName("actionGroup"))),
            new CompositeReferenceProvider(
                new ActionGroupReferenceProvider()
            )
        );

        // <actionGroup extends="parentActionGroup" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute().withName("extends")
                .withParent(XmlPatterns.xmlTag().withName("actionGroup"))),
            new CompositeReferenceProvider(
                new ActionGroupReferenceProvider()
            )
        );

        // <entity name="B" extends="A" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute().withName("extends")
                .withParent(XmlPatterns.xmlTag().withName("entity"))),
            new CompositeReferenceProvider(
                new DataReferenceProvider()
            )
        );

        // <test name="B" extends="A" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute()
                .withName(MftfTest.EXTENDS_ATTRIBUTE)
                .withParent(XmlPatterns.xmlTag().withName(MftfTest.TEST_TAG)
                    .withParent(XmlPatterns.xmlTag().withName(MftfTest.ROOT_TAG)
                    )
                )
            ),
            new CompositeReferenceProvider(
                new TestNameReferenceProvider()
            )
        );

        // <test name="A" extends="B" />
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute()
                        .withName(MftfTest.NAME_ATTRIBUTE)
                        .withParent(XmlPatterns.xmlTag().withName(MftfTest.TEST_TAG)
                                .withParent(XmlPatterns.xmlTag().withName(MftfTest.ROOT_TAG))
                        )
                ),
                new CompositeReferenceProvider(
                        new TestExtendedByReferenceProvider()
                )
        );

        // <someXmlTag component="requireJsMappingKey" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName(UiComponentXml.XML_ATTRIBUTE_COMPONENT)
            ),
            new RequireJsPreferenceReferenceProvider()
        );

        // <item name="component">requireJsMappingKey</item>
        registrar.registerReferenceProvider(
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withParent(
                XmlPatterns.xmlText().withParent(
                    XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM).withChild(
                        XmlPatterns.xmlAttribute().withValue(string().matches(UiComponentXml.XML_ATTRIBUTE_COMPONENT))
                    ).withChild(
                        XmlPatterns.xmlAttribute().withName(UiComponentXml.XML_ATTRIBUTE_NAME)
                    )
                )
            ),
            new RequireJsPreferenceReferenceProvider()
        );

        // <item name="template">reference</item>
        registrar.registerReferenceProvider(
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withParent(
                XmlPatterns.xmlText().withParent(
                    XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM).withChild(
                        XmlPatterns.xmlAttribute().withValue(string().matches(UiComponentXml.XML_ATTRIBUTE_TEMPLATE))
                    ).withChild(
                        XmlPatterns.xmlAttribute().withName(UiComponentXml.XML_ATTRIBUTE_NAME)
                    )
                )
            ),
            new FilePathReferenceProvider()
        );

        // <add parent="reference" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName(ModuleMenuXml.parentTagAttribute).withParent(
                    XmlPatterns.xmlTag().withName(ModuleMenuXml.addTag)
                )
            ).inFile(
                xmlFile().withName(string().endsWith(ModuleMenuXml.fileName))
            ),
            new MenuReferenceProvider()
        );

        // <table name="reference" /> in db_schema.xml
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName(ModuleDbSchemaXml.XML_ATTR_TABLE_NAME)
                    .withParent(XmlPatterns.xmlTag().withName(ModuleDbSchemaXml.XML_TAG_TABLE)
                )
            ).inFile(
                xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))
            ),
            new TableColumnNamesReferenceProvider()
        );

        // <constraint table="reference" /> in db_schema.xml
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute()
                    .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_TABLE_NAME)
                    .withParent(XmlPatterns.xmlTag().withName(
                        ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                    )
            ).inFile(
                xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))
            ),
            new TableColumnNamesReferenceProvider()
        );

        // <constraint referenceTable="reference" /> in db_schema.xml
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute()
                    .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME)
                    .withParent(XmlPatterns.xmlTag().withName(
                            ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                    )
            ).inFile(
                xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))
            ),
            new TableColumnNamesReferenceProvider()
        );

        // <constraint column="reference" /> in db_schema.xml
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute()
                    .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_COLUMN_NAME)
                    .withParent(XmlPatterns.xmlTag().withName(
                            ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                    )
            ).inFile(
                xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))
            ),
            new TableColumnNamesReferenceProvider()
        );

        // <constraint referenceColumn="reference" /> in db_schema.xml
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute()
                    .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME)
                    .withParent(XmlPatterns.xmlTag().withName(
                            ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                    )
            ).inFile(
                xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))
            ),
            new TableColumnNamesReferenceProvider()
        );

        // <plugin name="pluginName" disabled="true" /> in di.xml
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("name").withParent(
                    XmlPatterns.xmlTag().withName("plugin").withChild(
                        XmlPatterns.xmlAttribute().withName("disabled")
                    )
                )
            ).inFile(xmlFile().withName(string().matches(ModuleDiXml.FILE_NAME))),
            new PluginReferenceProvider()
        );

        registerReferenceForDifferentNesting(registrar);
    }

    private void registerReferenceForDifferentNesting(@NotNull PsiReferenceRegistrar registrar) {
        int i = 1;
        int maxNesting = 10;
        while( i < maxNesting) {

            // <someXmlTag url="{{someValue}}" /> in MFTF Tests and ActionGroups
            registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withValue(
                    string().matches(RegExUtil.Magento.MFTF_CURLY_BRACES)
                ).withParent(XmlPatterns.xmlAttribute().withName(
                    MftfActionGroup.URL_ATTRIBUTE
                ).withParent(XmlPatterns.xmlTag().withSuperParent(i, XmlPatterns.xmlTag().withName(
                    string().oneOf(MftfActionGroup.ROOT_TAG, MftfTest.TEST_TAG)
                )))),
                new CompositeReferenceProvider(
                    new PageReferenceProvider()
                )
            );

            // <someXmlTag selector="{{someValue}}" /> in MFTF Tests and ActionGroups
            registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue().withValue(
                    string().matches(RegExUtil.Magento.MFTF_CURLY_BRACES)
                ).withParent(XmlPatterns.xmlAttribute().withName(
                    MftfActionGroup.SELECTOR_ATTRIBUTE
                ).withParent(XmlPatterns.xmlTag().withSuperParent(i, XmlPatterns.xmlTag().withName(
                    string().oneOf(MftfActionGroup.ROOT_TAG, MftfTest.TEST_TAG)
                )))),
                new CompositeReferenceProvider(
                    new SectionReferenceProvider()
                )
            );

            i++;
        }
    }
}
