package com.magento.idea.magento2plugin.xml.di.reference.provider.resolver;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 10/19/15.
 */
public interface ClassNameResolverI {
    @Nullable
    public XmlTag findTypeTag(PsiElement psiElement);

    @Nullable
    public String resolveTypeName(XmlTag xmlTag);
}
