/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTokenType;
// CHECKSTYLE IGNORE check FOR NEXT 6 LINES
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.completion.provider.*;//NOPMD
import com.magento.idea.magento2plugin.completion.provider.mftf.*;//NOPMD
import com.magento.idea.magento2plugin.magento.files.*;//NOPMD
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlFile;

/**
 * TODO: enable style checks after decomposition.
 */
@SuppressWarnings({"PMD", "checkstyle:all"})
public class XmlCompletionContributor extends CompletionContributor {

    public XmlCompletionContributor() {

        /* PHP class member completion provider */
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withChild(
                                XmlPatterns.xmlAttribute().withName(CommonXml.SCHEMA_VALIDATE_ATTRIBUTE)
                                        .withValue(string().oneOf(CommonXml.INIT_PARAMETER))))
                        ),
                new PhpClassMemberCompletionProvider()
        );

        /* Module Completion provider */
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleAclXml.XML_ATTR_ID))
                        .inFile(xmlFile().withName(string().endsWith(ModuleAclXml.FILE_NAME))),
                new ModuleNameCompletionProvider()
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleXml.MODULE_ATTR_NAME))
                        .inFile(xmlFile().withName(string().endsWith(ModuleXml.FILE_NAME))),
                new ModuleNameCompletionProvider()
        );

        /* PHP Class completion provider */

        // <randomTag xsi:type="completion">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .withParent(XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withChild(
                                XmlPatterns.xmlAttribute().withName(CommonXml.SCHEMA_VALIDATE_ATTRIBUTE).withValue(string().oneOf(CommonXml.OBJECT))))
                        ),
                new PhpClassCompletionProvider()
        );

        // <randomTag class="completion">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(CommonXml.ATTR_CLASS)),
                new PhpClassCompletionProvider()
        );
        // <preference for="completion">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleDiXml.PREFERENCE_ATTR_FOR)),
                new PhpClassCompletionProvider()
        );

        // <type name="completion">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleDiXml.NAME_ATTR)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleDiXml.TYPE_TAG))),
                new PhpClassCompletionProvider()
        );

        // <plugin type="completion">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName(ModuleDiXml.TYPE_ATTR)
                    .withParent(XmlPatterns.xmlTag().withName(ModuleDiXml.PLUGIN_TAG_NAME))),
            new PhpClassCompletionProvider()
        );

        /* File Path Completion provider */
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(LayoutXml.XML_ATTRIBUTE_TEMPLATE)),
                new FilePathCompletionProvider()
        );

        // <uiComponent name="completion"/>
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(LayoutXml.NAME_ATTRIBUTE)
                                .withParent(XmlPatterns.xmlTag().withName(LayoutXml.UI_COMPONENT_TAG_NAME))),
                new UiComponentCompletionProvider()
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .withParent(XmlPatterns.xmlText().withParent(
                    XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM).withChild(
                        XmlPatterns.xmlAttribute().withValue(string()
                            .matches(UiComponentXml.XML_ATTRIBUTE_TEMPLATE))
                    ))
                ),
            new FilePathCompletionProvider()
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName("type")),
            new VirtualTypeCompletionProvider()
        );
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName("name")
                    .withParent(XmlPatterns.xmlTag().withName("virtualType"))),
            new VirtualTypeCompletionProvider()
        );
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .withParent(XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withChild(
                    XmlPatterns.xmlAttribute().withName("xsi:type").withValue(string().oneOf("object"))))
                ),
            new VirtualTypeCompletionProvider()
        );

        // <argument name="parameterName">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute().withName("name")
                .withParent(XmlPatterns.xmlTag().withName("argument")
                    .withParent(XmlPatterns.xmlTag().withName("arguments"))
                )
            ).inFile(xmlFile().withName(string().endsWith("di.xml"))),
            new PhpConstructorArgumentCompletionProvider()
        );

        // <observer instance="Class">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleEventsXml.INSTANCE_ATTRIBUTE)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleEventsXml.OBSERVER_TAG)
                                )
                        ).inFile(xmlFile().withName(string().matches(ModuleEventsXml.FILE_NAME))),
                new PhpClassCompletionProvider()
        );

        // <job instance="class">
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(CommonXml.ATTR_INSTANCE)
                                .withParent(XmlPatterns.xmlTag().withName(CrontabXmlTemplate.CRON_JOB_TAG)
                                )
                        ).inFile(xmlFile().withName(string().matches(CrontabXmlTemplate.FILE_NAME))),
                new PhpClassCompletionProvider()
        );

        // <source_model>php class completion</source_model> in system.xml files.
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
            .inside(XmlPatterns.xmlTag().withName(ModuleSystemXml.XML_TAG_SOURCE_MODEL)
                .withParent(XmlPatterns.xmlTag().withName(ModuleSystemXml.FIELD_ELEMENT_NAME))
            ).inFile(xmlFile().withName(string().matches(ModuleSystemXml.FILE_NAME))),
            new PhpClassCompletionProvider()
        );

        // <frontend_model>completion</frontend_model>
        extend(CompletionType.BASIC,
                psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .inside(XmlPatterns.xmlTag().withName(ModuleSystemXml.XML_TAG_FRONTEND_MODEL)),
                new PhpClassCompletionProvider()
        );

        // <backend_model>completion</backend_model> in system.xml
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
            .inside(XmlPatterns.xmlTag().withName(ModuleSystemXml.XML_TAG_BACKEND_MODEL)
                    .withParent(XmlPatterns.xmlTag().withName(ModuleSystemXml.FIELD_ELEMENT_NAME))
            ).inFile(xmlFile().withName(string().matches(ModuleSystemXml.FILE_NAME))),
            new PhpClassCompletionProvider()
        );

        // <randomTag backend_model="completion"> in config.xml
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute().withName(ModuleConfigXml.XML_ATTRIBUTE_BACKEND_MODEL))
            .inFile(xmlFile().withName(string().matches(ModuleConfigXml.FILE_NAME))),
            new PhpClassCompletionProvider()
        );

        // <parameter source_model="completion">...</parameter> in widget.xml files.
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute().withName(ModuleWidgetXml.ATTRIBUTE_SOURCE_MODEL_NAME)
                .withParent(XmlPatterns.xmlTag().withName(ModuleWidgetXml.TAG_PARAMETER_NAME).
                    withParent(XmlPatterns.xmlTag().withName(ModuleWidgetXml.TAG_PARAMETERS_NAME)))
            ).inFile(xmlFile().withName(string().matches(ModuleWidgetXml.FILE_NAME))),
                new PhpClassCompletionProvider()
        );

        // <service method="methodName"/>
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName("method")
                    .withParent(XmlPatterns.xmlTag().withName("service"))
                ).inFile(xmlFile().withName(string().endsWith("webapi.xml"))),
            new PhpServiceMethodCompletionContributor()
        );

        // <job method="methodName"/>
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName("method")
                    .withParent(XmlPatterns.xmlTag().withName("job"))
                ).inFile(xmlFile().withName(string().matches("crontab.xml"))),
            new PhpJobMethodCompletionContributor()
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute().withName("name")
                .withParent(XmlPatterns.xmlTag().withName("referenceContainer"))
            ),
            new LayoutContainerCompletionContributor()
        );

        extend(CompletionType.BASIC, XmlPatterns.or(
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN).inside(XmlPatterns.xmlAttribute().withName("name")
                .withParent(XmlPatterns.xmlTag().withName("referenceBlock"))),
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN).inside(XmlPatterns.xmlAttribute()
                .withName(string().oneOf("before", "after"))
                .withParent(XmlPatterns.xmlTag().withName("block"))),
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN).inside(XmlPatterns.xmlAttribute()
                .withName(string().oneOf("before", "after", "destination", "element"))
                .withParent(XmlPatterns.xmlTag().withName("move"))),
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN).inside(XmlPatterns.xmlAttribute().withName("name")
                .withParent(XmlPatterns.xmlTag().withName("remove")))
            ),
            new LayoutBlockCompletionContributor()
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute().withName("handle")
                .withParent(XmlPatterns.xmlTag().withName("update"))
            ),
            new LayoutUpdateCompletionContributor()
        );

        // event name completion contributor
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute().withName("name")
                .withParent(XmlPatterns.xmlTag().withName("event"))
            ).inFile(xmlFile().withName(string().endsWith("events.xml"))),
            new EventNameCompletionContributor()
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName(UiComponentXml.XML_ATTRIBUTE_COMPONENT)
            )),
            new CompositeCompletionProvider(
                new RequireJsMappingCompletionProvider(),
                new FilePathCompletionProvider()
            )
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
             .withParent(XmlPatterns.xmlText().withParent(
                XmlPatterns.xmlTag().withName(UiComponentXml.XML_TAG_ITEM).withChild(
                        XmlPatterns.xmlAttribute().withValue(string()
                            .matches(UiComponentXml.XML_ATTRIBUTE_COMPONENT))
                    ).withChild(
                        XmlPatterns.xmlAttribute().withName(UiComponentXml.XML_ATTRIBUTE_NAME)
                    )
                )
            ),
            new CompositeCompletionProvider(
                new RequireJsMappingCompletionProvider(),
                new FilePathCompletionProvider()
            )
        );

        // mftf action group completion contributor
        extend(
            CompletionType.BASIC,
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(
                    XmlPatterns.xmlAttribute().withName(string().oneOf("ref", "extends"))
                        .withParent(XmlPatterns.xmlTag().withName("actionGroup")
                    )
                ),
            new ActionGroupCompletionProvider()
        );

        // mftf data entity completion contributor
        extend(
            CompletionType.BASIC,
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN).inside(XmlPatterns.xmlAttribute()
                .withName(string().oneOf("entity", "value", "userInput"))
                .without(new PatternCondition<XmlAttribute>("value attribute of general text tag") {
                    @Override
                    public boolean accepts(@NotNull XmlAttribute attribute, ProcessingContext context) {
                        final String tagName = attribute.getParent().getName();
                        return tagName.matches("stories|title|description");
                    }
                })
            ),
            new DataCompletionProvider()
        );

        // Data entity/extends completion contributor
        extend(
            CompletionType.BASIC,
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(
                    XmlPatterns.xmlAttribute().withName("extends")
                        .withParent(XmlPatterns.xmlTag().withName("entity")
                )
            ),
            new DataCompletionProvider()
        );

        // MFTF Test extends completion contributor
        extend(
            CompletionType.BASIC,
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(
                    XmlPatterns.xmlAttribute().withName(MftfTest.EXTENDS_ATTRIBUTE)
                        .withParent(XmlPatterns.xmlTag().withName(MftfTest.TEST_TAG)
                        .withParent(XmlPatterns.xmlTag().withName(MftfTest.ROOT_TAG)
                        )
                    )
                ),
            new TestNameCompletionProvider()
        );

        // <add parent="completion" />
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName(ModuleMenuXml.parentTagAttribute)
                    .withParent(XmlPatterns.xmlTag().withName(ModuleMenuXml.addTag)
                        .withParent(XmlPatterns.xmlTag().withName(ModuleMenuXml.menuTag))
                    )
                ).inFile(xmlFile().withName(string().matches(ModuleMenuXml.fileName))),
            new MenuCompletionProvider()
        );

        // <table name="completion" /> in db_schema.xml
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName(ModuleDbSchemaXml.XML_ATTR_TABLE_NAME)
                        .withParent(XmlPatterns.xmlTag().withName(ModuleDbSchemaXml.XML_TAG_TABLE)
                                .withParent(XmlPatterns.xmlTag()
                                        .withName(ModuleDbSchemaXml.XML_TAG_SCHEMA))))
                        .inFile(xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))),
            new TableNameCompletionProvider()
        );

        // <constraint table="completion" /> in db_schema.xml
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute()
                        .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_TABLE_NAME)
                        .withParent(XmlPatterns.xmlTag()
                                .withName(ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                        .withParent(XmlPatterns.xmlTag()
                                .withName(ModuleDbSchemaXml.XML_TAG_TABLE))))
                .inFile(xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))),
                new TableNameCompletionProvider()
        );

        // <constraint referenceTable="completion" /> in db_schema.xml
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute()
                        .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME)
                        .withParent(XmlPatterns.xmlTag()
                                .withName(ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                                .withParent(XmlPatterns.xmlTag()
                                        .withName(ModuleDbSchemaXml.XML_TAG_TABLE))))
                .inFile(xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))),
                new TableNameCompletionProvider()
        );

        // <constraint column="completion" /> in db_schema.xml
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute()
                        .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_COLUMN_NAME)
                        .withParent(XmlPatterns.xmlTag()
                                .withName(ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                                .withParent(XmlPatterns.xmlTag()
                                        .withName(ModuleDbSchemaXml.XML_TAG_TABLE))))
                .inFile(xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))),
                new ColumnNameCompletionProvider()
        );

        // <constraint referenceColumn="completion" /> in db_schema.xml
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute()
                        .withName(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME)
                        .withParent(XmlPatterns.xmlTag()
                                .withName(ModuleDbSchemaXml.XML_TAG_CONSTRAINT)
                                .withParent(XmlPatterns.xmlTag()
                                        .withName(ModuleDbSchemaXml.XML_TAG_TABLE))))
                .inFile(xmlFile().withName(string().matches(ModuleDbSchemaXml.FILE_NAME))),
                new ColumnNameCompletionProvider()
        );

        registerCompletionsForDifferentNesting();
    }

    private void registerCompletionsForDifferentNesting() {
        int i = 1;
        int maxNesting = 10;
        while( i < maxNesting) {

            // mftf selector completion contributor
            extend(CompletionType.BASIC,
                psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                    .inside(XmlPatterns.xmlAttribute()
                        .withName(MftfActionGroup.SELECTOR_ATTRIBUTE).withSuperParent(
                            i,
                            XmlPatterns.xmlTag().withParent(
                                XmlPatterns.xmlTag().withName(
                                    string().oneOf(MftfActionGroup.ROOT_TAG, MftfTest.TEST_TAG)
                                )))
                    ),
                new SelectorCompletionProvider()
            );

            // mftf page url completion contributor
            extend(
                CompletionType.BASIC,
                psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                    .inside(
                        XmlPatterns.xmlAttribute().withName(MftfActionGroup.URL_ATTRIBUTE)
                            .withSuperParent(i ,XmlPatterns.xmlTag().withParent(
                                XmlPatterns.xmlTag().withName(
                                    string().oneOf(MftfActionGroup.ROOT_TAG, MftfTest.TEST_TAG)
                                )))
                    ),
                new PageCompletionProvider()
            );

            i++;
        }
    }
}
