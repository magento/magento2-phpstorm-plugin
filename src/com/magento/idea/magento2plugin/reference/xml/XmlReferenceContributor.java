package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.php.util.PhpRegex;
import com.magento.idea.magento2plugin.reference.provider.*;
import com.magento.idea.magento2plugin.reference.provider.mftf.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.XmlPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlFile;

public class XmlReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        // <someXmlTag someAttribute="Some\Php\ClassName[::CONST|$property|method()]" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withValue(string().matches(PhpRegex.Xml.CLASS_ELEMENT)),
            new CompositeReferenceProvider(
                new PhpClassReferenceProvider(),
                new PhpClassMemberReferenceProvider()
            )
        );

        // <someXmlTag>Some\Php\ClassName[::CONST|$property|method()]</someXmlTag>
        registrar.registerReferenceProvider(
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .withText(string().matches(PhpRegex.Xml.CLASS_ELEMENT)),
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

        // <event name="reference" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName("name").withParent(
                    XmlPatterns.xmlTag().withName("event")
                )
            ),
            new EventNameReferenceProvider()
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

        // <someXmlTag someAttribute="{{someValue}}" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withValue(string().matches(".*\\{\\{[^\\}]+\\}\\}.*")),
            new CompositeReferenceProvider(
                new DataReferenceProvider(),
                new PageReferenceProvider(),
                new SectionReferenceProvider()
            )
        );

        // <createData entity="SimpleProduct" />
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue().withParent(XmlPatterns.xmlAttribute().withName(string().oneOf("entity", "value", "userInput", "url"))),
            new CompositeReferenceProvider(
                new DataReferenceProvider(),
                new SectionReferenceProvider()
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

        // <assert*><*Result type="variable">$someVariableReferenceToStepKey</*Result></assert*>
        registrar.registerReferenceProvider(
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withParent(
                XmlPatterns.xmlText().withParent(
                    XmlPatterns.xmlTag().withChild(
                        XmlPatterns.xmlAttribute().withName("type")
                    )
                )
            ),
            new CompositeReferenceProvider(
                new VariableToStepKeyProvider()
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
    }
}
