package com.magento.idea.magento2plugin.xml.observer.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.php.util.MagentoTypes;
import com.magento.idea.magento2plugin.xml.observer.PhpPatternsHelper;
import com.magento.idea.magento2plugin.xml.reference.TypeReference;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
* Created by dkvashnin on 11/3/15.
*/
class EventReferenceProvider extends PsiReferenceProvider {
    private final ReferenceResultsFiller[] resultsFillers;
    private static final String DISPATCH_METHOD = "dispatch";

    public EventReferenceProvider(ReferenceResultsFiller[] resultsFillers) {
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
        if (!DISPATCH_METHOD.equals(methodReference.getName())) {
            return new PsiReference[0];
        }

        PsiElement resolvedElement = methodReference.resolve();
        if (resolvedElement != null && resolvedElement instanceof Method) {
            PhpClass containingClass = ((Method) resolvedElement).getContainingClass();

            if (containingClass != null && MagentoTypes.EVENT_MANAGER_TYPE.equals(containingClass.getPresentableFQN())) {
                return new PsiReference[]{new TypeReference(psiElement, this.resultsFillers, true)};
            }
        }


        return new PsiReference[0];
    }
}
