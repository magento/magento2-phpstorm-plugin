package com.magento.idea.magento2plugin.xml.layout.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.PhpIcons;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import com.magento.idea.magento2plugin.xml.completion.CompletionProviderI;
import com.magento.idea.magento2plugin.xml.layout.index.util.LayoutIndexUtility;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class ReferenceComponentCompletionProvider implements CompletionProviderI<PsiElement> {
    private ID<String, Void> indexId;

    public ReferenceComponentCompletionProvider(ID<String, Void> indexId) {
        this.indexId = indexId;
    }

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement, @Nullable PsiContextMatcherI<PsiElement> context) {
        Collection<String> keys = LayoutIndexUtility.getAllKeys(indexId, psiElement.getProject());

        List<LookupElement> results = new ArrayList<>();
        for (String key: keys) {
            results.add(
                LookupElementBuilder.create(key).withIcon(PhpIcons.XML_TAG_ICON)
            );
        }

        return results;
    }

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement) {
        return collectCompletionResult(psiElement, null);
    }
}
