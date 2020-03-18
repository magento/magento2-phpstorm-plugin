/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.RequireJsIndex;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public class RequireJsMappingCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }
        String prefix = result.getPrefixMatcher().getPrefix();

        Collection<String> requireJsKeys
                = FileBasedIndex.getInstance().getAllKeys(RequireJsIndex.KEY, position.getProject());

        requireJsKeys.removeIf(m -> !m.startsWith(prefix));
        JavaScriptFileType jsFileType = new JavaScriptFileType();
        for (String requireJsKey : requireJsKeys) {
            result.addElement(
                    LookupElementBuilder
                            .create(requireJsKey)
                            .withIcon(jsFileType.getIcon())
            );
        }
    }
}
