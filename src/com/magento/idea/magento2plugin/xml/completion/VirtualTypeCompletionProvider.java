package com.magento.idea.magento2plugin.xml.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkvashnin on 11/17/15.
 */
public class VirtualTypeCompletionProvider implements CompletionProviderI {
    public final static CompletionProviderI INSTANCE = new VirtualTypeCompletionProvider();

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement, @Nullable PsiContextMatcherI context) {
        List<LookupElement> result = new ArrayList<>();
        String[] allVirtualTypesNames = VirtualTypesNamesFileBasedIndex.getAllVirtualTypesNames(
            psiElement.getProject()
        );

        for (String name: allVirtualTypesNames) {
            result.add(LookupElementBuilder.create(name));
        }

        return result;
    }

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement) {
        return collectCompletionResult(psiElement, null);
    }
}
