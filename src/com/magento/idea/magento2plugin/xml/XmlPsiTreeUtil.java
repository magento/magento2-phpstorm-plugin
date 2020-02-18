/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.xml;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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

    public static Collection<XmlAttributeValue> findAttributeValueElements(XmlFile xmlFile,
                                                                    String tagName,
                                                                    String attributeName,
                                                                    String value) {
        Collection<XmlAttributeValue> psiElements = findAttributeValueElements(xmlFile, tagName, attributeName);
        psiElements.removeIf(e -> e.getValue() == null || !e.getValue().equals(value));
        return psiElements;
    }
}
