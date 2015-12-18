package com.magento.idea.magento2plugin.util;

import com.intellij.psi.PsiElement;

/**
 * Created by dkvashnin on 12/18/15.
 */
public interface PsiContextMatcherI {
    boolean match(PsiElement psiElement);
}
