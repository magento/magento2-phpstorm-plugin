/**
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
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public class ModuleNameCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }
        String prefix = result.getPrefixMatcher().getPrefix();

        Collection<String> moduleNames
                = FileBasedIndex.getInstance().getAllKeys(ModuleNameIndex.KEY, position.getProject());

        moduleNames.removeIf(m -> !m.startsWith(prefix));
        for (String moduleName : moduleNames) {
            result.addElement(
                    LookupElementBuilder
                            .create(moduleName)
                            .withIcon(MagentoIcons.MODULE)
            );
        }
    }
}
