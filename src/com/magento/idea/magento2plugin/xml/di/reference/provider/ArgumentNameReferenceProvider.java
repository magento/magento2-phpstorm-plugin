package com.magento.idea.magento2plugin.xml.di.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.di.reference.ArgumentNameReference;
import com.magento.idea.magento2plugin.xml.di.reference.provider.resolver.ClassNameResolver;
import com.magento.idea.magento2plugin.xml.di.reference.provider.resolver.TypeTagResolver;
import com.magento.idea.magento2plugin.xml.di.reference.provider.resolver.VirtualTypeTagResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 10/19/15.
 */
public class ArgumentNameReferenceProvider extends PsiReferenceProvider {
    private static ClassNameResolver[] classNameResolvers = new ClassNameResolver[] {
        TypeTagResolver.INSTANCE,
        VirtualTypeTagResolver.INSTANCE
    };

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        if (!(psiElement instanceof XmlAttributeValue)) {
            return new PsiReference[0];
        }

        for (ClassNameResolver classNameResolver: classNameResolvers) {
            XmlTag typeTag = classNameResolver.findTypeTag(psiElement);

            if (typeTag == null) {
                continue;
            }

            String typeName = classNameResolver.resolveTypeName(typeTag);
            if (typeName == null) {
                continue;
            }

            return new PsiReference[]{new ArgumentNameReference(psiElement, typeTag, typeName)};
        }

        return new PsiReference[0];
    }
}
