package com.magento.idea.magento2plugin.xml.di.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.reference.TypeReference;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Warider on 17.08.2015.
 */
public class XmlReferenceProvider extends PsiReferenceProvider {
    private final ReferenceResultsFiller[] resultsFillers;

    public XmlReferenceProvider(ReferenceResultsFiller[] resultsFillers) {
        super();
        this.resultsFillers = resultsFillers;
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        return new PsiReference[]{new TypeReference(psiElement, resultsFillers)};
    }
}
