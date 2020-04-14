/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.provider.mftf;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.MftfPage;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.PageIndex;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public class PageCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters,
            ProcessingContext context,
            @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();

        if (position == null) {
            return;
        }

        Collection<String> allKeys
            = FileBasedIndex.getInstance().getAllKeys(PageIndex.KEY, position.getProject());

        for (String pageName: allKeys) {
            result.addElement(LookupElementBuilder.create(
                    pageName
                        + MftfPage.REFERENCE_SEPARATOR
                        + MftfPage.URL_ATTRIBUTE
            ));
        }
    }
}
