package com.magento.idea.magento2plugin.xml.di.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.di.reference.ArgumentNameReference;
import com.magento.idea.magento2plugin.xml.di.reference.provider.resolver.TypeTagResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 10/19/15.
 */
public class ArgumentNameReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        if (!(psiElement instanceof XmlAttributeValue)) {
            return new PsiReference[0];
        }
        TypeTagResolver typeTagResolver = new TypeTagResolver();
        XmlTag typeTag = typeTagResolver.findTypeTag(psiElement);

        if (typeTag == null) {
            return new PsiReference[0];
        }

        String typeName = typeTagResolver.resolveTypeName(typeTag);
        if (typeName == null) {
            return new PsiReference[0];
        }

        return new PsiReference[]{new ArgumentNameReference(psiElement, typeName)};
    }
}
