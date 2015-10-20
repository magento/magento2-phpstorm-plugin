package com.magento.idea.magento2plugin.xml.di.reference.provider.resolver;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 10/19/15.
 */
public class TypeTagResolver implements ClassNameResolverI {
    private static final String TAG_NAME = "type";
    private static final String ATTRIBUTE_NAME = "name";


    @Nullable
    @Override
    public XmlTag findTypeTag(PsiElement psiElement) {
        if (psiElement instanceof XmlTag && ((XmlTag) psiElement).getName().equals(TAG_NAME)) {
            return (XmlTag) psiElement;
        }

        if (psiElement.getParent() == null || !(psiElement.getParent() instanceof XmlElement)) {
            return null;
        }

        return findTypeTag(psiElement.getParent());
    }

    @Nullable
    @Override
    public String resolveTypeName(XmlTag xmlTag) {
        return xmlTag.getAttributeValue(ATTRIBUTE_NAME);
    }
}
