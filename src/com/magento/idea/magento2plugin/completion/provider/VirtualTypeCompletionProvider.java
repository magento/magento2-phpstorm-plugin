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
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.magento.idea.magento2plugin.indexes.DiIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class VirtualTypeCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        String prefix = result.getPrefixMatcher().getPrefix();

        DiIndex index = DiIndex.getInstance(position.getProject());
        Collection<String> elements = index.getAllVirtualTypeElementNames(new PlainPrefixMatcher(prefix), position.getResolveScope());

        for (String elementName:elements) {
            result.addElement(
                    LookupElementBuilder
                            .create(elementName)
                            .withIcon(PhpIcons.CLASS_ICON)
            );
        }
    }
}
