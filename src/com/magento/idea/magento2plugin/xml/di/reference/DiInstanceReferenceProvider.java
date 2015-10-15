package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Warider on 17.08.2015.
 */
public class DiInstanceReferenceProvider extends PsiReferenceProvider {
    private TypeReference.ReferenceType[] referenceTypes;

    public DiInstanceReferenceProvider(TypeReference.ReferenceType[] referenceTypes) {
        super();
        this.referenceTypes = referenceTypes;
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        return new PsiReference[]{new TypeReference(psiElement, referenceTypes)};
    }
}
