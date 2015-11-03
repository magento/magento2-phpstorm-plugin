package com.magento.idea.magento2plugin.xml.observer.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.magento.idea.magento2plugin.xml.reference.TypeReference;
import com.magento.idea.magento2plugin.xml.reference.util.ResolveResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
* Created by dkvashnin on 11/3/15.
*/
class EventReferenceProvider extends PsiReferenceProvider {
    private final ResolveResultsFiller[] resultsFillers;

    public EventReferenceProvider(ResolveResultsFiller[] resultsFillers) {
        this.resultsFillers = resultsFillers;
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        ParameterList parameterList = PsiTreeUtil.getParentOfType(psiElement, ParameterList.class);

        if (parameterList == null) {
            return new PsiReference[0];
        }

        if(!(parameterList.getContext() instanceof MethodReference)) {
            return new PsiReference[0];
        }

        MethodReference methodReference = (MethodReference)parameterList.getContext();
        if (!methodReference.getName().equals("dispatch")) {
            return new PsiReference[0];
        }


        return new PsiReference[]{new TypeReference(psiElement, this.resultsFillers, true)};
    }
}
