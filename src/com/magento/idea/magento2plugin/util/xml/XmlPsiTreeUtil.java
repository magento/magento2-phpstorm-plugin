/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util.xml;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public class XmlPsiTreeUtil {

    @Nullable
    public static XmlTag getTypeTagOfArgument(XmlElement psiArgumentValueElement) {

        XmlTag argumentTag = PsiTreeUtil.getParentOfType(psiArgumentValueElement, XmlTag.class);
        XmlTag argumentsTag = PsiTreeUtil.getParentOfType(argumentTag, XmlTag.class);
        return PsiTreeUtil.getParentOfType(argumentsTag, XmlTag.class);
    }

    public static Collection<XmlAttributeValue> findAttributeValueElements(XmlFile xmlFile,
                                                                    String tagName,
                                                                    String attributeName) {
        Collection<XmlAttributeValue> psiElements = new THashSet<>();


        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) {
            return psiElements;
        }

        for (XmlTag tag: rootTag.findSubTags(tagName)) {
            if (tag != null) {
                XmlAttribute attribute = tag.getAttribute(attributeName);
                if (attribute != null && attribute.getValueElement() != null) {
                    psiElements.add(attribute.getValueElement());
                }
            }
        };

        return psiElements;
    }

    public static Collection<XmlAttributeValue> findTypeArgumentsItemValueElement(
            XmlFile xmlFile,
            String parentTagName,
            String parentTagAttributeName,
            String parentTagAttributeNameValue,
            String argumentTagAttributeName) {
        Collection<XmlAttributeValue> psiElements = new THashSet<>();

        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) {
            return psiElements;
        }

        for (XmlTag parentTag: rootTag.findSubTags(parentTagName)) {
            if (parentTag != null && parentTag.findSubTags(ModuleDiXml.ARGUMENTS_TAG).length > 0) {
                XmlAttribute parentAttribute = parentTag.getAttribute(parentTagAttributeName);
                if (parentAttribute != null
                        && parentAttribute.getValueElement() != null
                        && Objects.equals(parentAttribute.getValue(), parentTagAttributeNameValue)
                ) {
                    for (XmlTag argumentsTag: parentTag.findSubTags(ModuleDiXml.ARGUMENTS_TAG)) {
                        if (argumentsTag != null && argumentsTag.findSubTags(ModuleDiXml.ARGUMENT_TAG).length > 0) {
                            for (XmlTag argumentTag: argumentsTag.findSubTags(ModuleDiXml.ARGUMENT_TAG)) {
                                XmlAttribute argumentAttribute = argumentTag.getAttribute(ModuleDiXml.NAME_TAG);
                                if (argumentAttribute != null
                                        && argumentAttribute.getValueElement() != null
                                        && Objects.equals(argumentAttribute.getValue(), argumentTagAttributeName)
                                ) {
                                    psiElements.add(argumentAttribute.getValueElement());
                                }
                            }
                        }
                    }
                }
            }
        };

        return psiElements;
    }

    public static Collection<XmlAttributeValue> findAttributeValueElements(XmlFile xmlFile,
                                                                    String tagName,
                                                                    String attributeName,
                                                                    String value) {
        Collection<XmlAttributeValue> psiElements = findAttributeValueElements(xmlFile, tagName, attributeName);
        psiElements.removeIf(e -> e.getValue() == null || !e.getValue().equals(value));
        return psiElements;
    }
}
