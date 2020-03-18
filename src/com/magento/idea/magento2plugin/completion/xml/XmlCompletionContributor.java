/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.completion.provider.*;
import com.magento.idea.magento2plugin.completion.provider.mftf.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlFile;

public class XmlCompletionContributor extends CompletionContributor {

    public XmlCompletionContributor() {
        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN),
            new CompositeCompletionProvider(
                new PhpClassCompletionProvider(),
                new PhpClassMemberCompletionProvider(),
                new ModuleNameCompletionProvider(),
                new FilePathCompletionProvider()
            )
        );

        extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS),
            new CompositeCompletionProvider(
                new PhpClassCompletionProvider(),
                new PhpClassMemberCompletionProvider(),
                new ModuleNameCompletionProvider(),
                new FilePathCompletionProvider()
            )
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
