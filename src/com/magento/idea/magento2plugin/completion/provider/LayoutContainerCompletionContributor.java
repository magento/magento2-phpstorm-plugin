/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.LayoutIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class LayoutContainerCompletionContributor extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        final Collection<String> keys
                = LayoutIndex.getAllKeys(ContainerNameIndex.KEY, position.getProject());
        for (final String key: keys) {
            result.addElement(
                    LookupElementBuilder.create(key).withIcon(AllIcons.Nodes.Tag)
            );
        }
    }
}
