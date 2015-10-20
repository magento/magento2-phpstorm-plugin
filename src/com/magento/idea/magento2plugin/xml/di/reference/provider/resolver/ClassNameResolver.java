package com.magento.idea.magento2plugin.xml.di.reference.provider.resolver;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 10/19/15.
 */
public abstract class ClassNameResolver {

    public XmlTag findTypeTag(PsiElement psiElement) {
        if (psiElement instanceof XmlTag && ((XmlTag) psiElement).getName().equals(getTagName())) {
            return (XmlTag) psiElement;
        }

        if (psiElement.getParent() == null || !(psiElement.getParent() instanceof XmlElement)) {
            return null;
        }

        return findTypeTag(psiElement.getParent());
    }

    @Nullable
    abstract public String resolveTypeName(XmlTag xmlTag);

    abstract protected String getTagName();
}
