/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.DiIndex;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;


public class VirtualTypeCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        final String prefix = result.getPrefixMatcher().getPrefix();

        final DiIndex index = DiIndex.getInstance(position.getProject());
        final Collection<String> elements = index.getAllVirtualTypeElementNames(
                new PlainPrefixMatcher(prefix),
                position.getResolveScope()
        );

        for (final String elementName:elements) {
            result.addElement(
                    LookupElementBuilder
                            .create(elementName)
                            .withIcon(AllIcons.Nodes.Class)
            );
        }
    }
}
