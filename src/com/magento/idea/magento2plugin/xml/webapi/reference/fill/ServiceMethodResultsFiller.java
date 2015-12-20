package com.magento.idea.magento2plugin.xml.webapi.reference.fill;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import com.magento.idea.magento2plugin.xml.webapi.XmlHelper;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * Created by isentiabov on 20.12.2015.
 */
public class ServiceMethodResultsFiller implements ReferenceResultsFiller {
    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        // get service xml tag `method` attribute
        XmlAttribute methodAttribute = getMethodAttribute(psiElement);
        if (methodAttribute == null) {
            return;
        }

        // get service xml tag
        XmlTag serviceTag = getServiceTag(methodAttribute);
        if (serviceTag == null) {
            return;
        }

        // get service xml tag `class` attribute
        XmlAttribute classAttribute = getClassAttribute(serviceTag);
        if (classAttribute == null) {
            return;
        }

        PhpClass serviceInterface = getServiceInterface(classAttribute);
        if (serviceInterface == null) {
            return;
        }

        fillResults(serviceInterface, results, typeName);
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

    @Nullable
    protected PhpClass getServiceInterface(XmlAttribute xmlAttribute) {
        XmlAttributeValue value = xmlAttribute.getValueElement();
        if (value == null) {
            return null;
        }

        PsiReference reference = value.getReference();
        if (reference == null) {
            return null;
        }

        return (PhpClass)reference.resolve();
    }

    protected void fillResults(PhpClass serviceInterface, List<ResolveResult> results, String typeName) {
        for (Method method : serviceInterface.getMethods()) {
            if (method.getName().equals(typeName)) {
                results.add(
                    new PhpResolveResult(method)
                );

                break;
            }
        }
    }
}
