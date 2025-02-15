/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class CompositeCompletionProvider extends CompletionProvider<CompletionParameters> {

    private CompletionProvider<CompletionParameters>[] providers = null;

    @SafeVarargs
    public CompositeCompletionProvider(CompletionProvider<CompletionParameters> ...providers) {
        this.providers = providers;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        for (CompletionProvider<CompletionParameters> provider : providers) {
            provider.addCompletionVariants(parameters, context, result);
        }
    }
}
