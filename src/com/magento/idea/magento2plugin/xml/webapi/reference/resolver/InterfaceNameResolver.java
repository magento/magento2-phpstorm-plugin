package com.magento.idea.magento2plugin.xml.webapi.reference.resolver;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.xml.webapi.XmlHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Created by isentiabov on 22.12.2015.
 */
public class InterfaceNameResolver {

    @Nullable
    public XmlAttribute getInterfaceAttributeByMethod(PsiElement psiElement)
    {
        // get service xml tag `method` attribute
        XmlAttribute methodAttribute = getMethodAttribute(psiElement);
        if (methodAttribute == null) {
            return null;
        }

        // get service xml tag
        XmlTag serviceTag = getServiceTag(methodAttribute);
        if (serviceTag == null) {
            return null;
        }

        // get service xml tag `class` attribute
        return getClassAttribute(serviceTag);
    }

    protected XmlAttribute getMethodAttribute(PsiElement xmlAttribute) {
        return PsiTreeUtil.getParentOfType(xmlAttribute, XmlAttribute.class);
    }

    protected XmlTag getServiceTag(XmlAttribute xmlAttribute) {
        return PsiTreeUtil.getParentOfType(xmlAttribute, XmlTag.class);
    }

    protected XmlAttribute getClassAttribute(XmlTag xmlTag) {
        return xmlTag.getAttribute(XmlHelper.CLASS_ATTRIBUTE);
    }
}
