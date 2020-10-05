/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class MenuCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(
            final @NotNull CompletionParameters parameters,
            final ProcessingContext context,
            final @NotNull CompletionResultSet result
    ) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }
        final String prefix = result.getPrefixMatcher().getPrefix();

        final Collection<String> menuReferences
                = FileBasedIndex.getInstance().getAllKeys(MenuIndex.KEY, position.getProject());

        menuReferences.removeIf(m -> !m.startsWith(prefix));
        for (final String menuReference : menuReferences) {
            result.addElement(
                    LookupElementBuilder
                            .create(menuReference)
                            .withIcon(MagentoIcons.MODULE)
            );
        }
    }
}
