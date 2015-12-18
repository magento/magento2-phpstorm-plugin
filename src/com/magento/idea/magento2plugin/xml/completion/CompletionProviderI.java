package com.magento.idea.magento2plugin.xml.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by dkvashnin on 11/17/15.
 */
public interface CompletionProviderI {
    public List<LookupElement> collectCompletionResult(PsiElement psiElement, @Nullable PsiContextMatcherI context);

    public List<LookupElement> collectCompletionResult(PsiElement psiElement);
}
