/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.completion.provider.*;
import com.magento.idea.magento2plugin.completion.provider.mftf.*;
import com.magento.idea.magento2plugin.magento.files.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlFile;

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
                        .inside(XmlPatterns.xmlAttribute().withName(ModuleDiXml.PLUGIN_TYPE_ATTR_NAME)
                                .withParent(XmlPatterns.xmlTag().withName(ModuleDiXml.PLUGIN_TYPE_TAG))),
                new PhpClassCompletionProvider()
        );

        // <frontend_model>completion</frontend_model>
        extend(CompletionType.BASIC,
                psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                        .inside(XmlPatterns.xmlTag().withName(SystemXml.XML_TAG_FRONTEND_MODEL)),
                new PhpClassCompletionProvider()
        );

        /* File Path Completion provider */
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                        .inside(XmlPatterns.xmlAttribute().withName(LayoutXml.XML_ATTRIBUTE_TEMPLATE)),
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

        // <source_model>php class completion</source_model> in system.xml files.
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
            .inside(XmlPatterns.xmlTag().withName(ModuleSystemXml.SOURCE_MODEL_ELEMENT_NAME)
                .withParent(XmlPatterns.xmlTag().withName(ModuleSystemXml.FIELD_ELEMENT_NAME))
            ).inFile(xmlFile().withName(string().endsWith(ModuleSystemXml.FILE_NAME))),
            new PhpClassCompletionProvider()
        );

        // <parameter source_model="completion">...</parameter> in widget.xml files.
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute().withName(ModuleWidgetXml.ATTRIBUTE_SOURCE_MODEL_NAME)
                .withParent(XmlPatterns.xmlTag().withName(ModuleWidgetXml.TAG_PARAMETER_NAME).
                    withParent(XmlPatterns.xmlTag().withName(ModuleWidgetXml.TAG_PARAMETERS_NAME)))
            ).inFile(xmlFile().withName(string().endsWith(ModuleWidgetXml.FILE_NAME))),
                new PhpClassCompletionProvider()
        );

        // <service method="methodName"/>
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName("method")
                    .withParent(XmlPatterns.xmlTag().withName("service"))
                ).inFile(xmlFile().withName(string().endsWith("webapi.xml"))),
            new PhpServiceMethodCompletionContributor()
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
                XmlPatterns.xmlAttribute().withName("component")
            )),
            new RequireJsMappingCompletionProvider()
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS)
             .withParent(XmlPatterns.xmlText().withParent(
                XmlPatterns.xmlTag().withName("item").withChild(
                        XmlPatterns.xmlAttribute().withValue(string().matches("component"))
                    ).withChild(
                        XmlPatterns.xmlAttribute().withName("name")
                    )
                )
            ),
            new RequireJsMappingCompletionProvider()
        );

        // mftf selector completion contributor
        extend(CompletionType.BASIC,
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .inside(XmlPatterns.xmlAttribute())
            .inFile(xmlFile().withName(string().endsWith("Test.xml"))),
            new SelectorCompletionProvider()
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
            psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .inside(XmlPatterns.xmlAttribute().withName(string().oneOf("entity", "value", "userInput", "url"))
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
    }
}
