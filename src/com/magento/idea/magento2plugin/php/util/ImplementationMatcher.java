package com.magento.idea.magento2plugin.php.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;

/**
* Created by dkvashnin on 12/18/15.
*/
public class ImplementationMatcher implements PsiContextMatcherI<PsiElement> {
    private String type;

    public ImplementationMatcher(String type) {
        this.type = type;
    }

    @Override
    public boolean match(PsiElement psiElement) {
        if (psiElement instanceof PhpClass) {
            for (PhpClass parent : ((PhpClass)psiElement).getImplementedInterfaces()) {
                if (parent.getPresentableFQN().equals(type)) {
                    return true;
                }
            }

            PhpClass parent = ((PhpClass) psiElement).getSuperClass();
            if (parent != null) {
                return match(parent);
            }
        }
        return false;
    }
}
