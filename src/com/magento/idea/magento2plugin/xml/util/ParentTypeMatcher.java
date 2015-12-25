package com.magento.idea.magento2plugin.xml.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import org.jetbrains.annotations.Nullable;

/**
* Created by dkvashnin on 12/25/15.
*/
public class ParentTypeMatcher implements PsiContextMatcherI<PsiElement> {
    private PhpClass parentType;

    public ParentTypeMatcher(PhpClass parentType) {
        this.parentType = parentType;
    }

    @Override
    public boolean match(PsiElement psiElement) {
        if (!(psiElement instanceof PhpClass)) {
            return false;
        }

        return parentType.equals(psiElement)
            || match(((PhpClass)psiElement).getImplementedInterfaces())
            || match(((PhpClass) psiElement).getSuperClass());
    }

    public boolean match(@Nullable PhpClass[] probableMistakes) {
        if (probableMistakes == null) {
            return false;
        }

        boolean result = false;

        for (PhpClass probableMistake : probableMistakes) {
            result = parentType.equals(probableMistake) || match(probableMistake.getImplementedInterfaces());
        }

        return result;
    }
}
