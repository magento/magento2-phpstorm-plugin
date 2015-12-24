package com.magento.idea.magento2plugin.util;

/**
 * Created by dkvashnin on 12/18/15.
 */
public interface PsiContextMatcherI<T> {
    boolean match(T psiElement);
}
