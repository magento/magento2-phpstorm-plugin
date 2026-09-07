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
import com.magento.idea.magento2plugin.stubs.indexes.xml.DeclarativeSchemaElementsIndex;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Provides table names for completion.
 */
public class TableNameCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(
            final @NotNull CompletionParameters parameters,
            final @NotNull ProcessingContext context,
            final @NotNull CompletionResultSet result
    ) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        final Collection<String> tableNames = FileBasedIndex.getInstance().getAllKeys(
                DeclarativeSchemaElementsIndex.KEY, position.getProject()
        );
        final List<String> filteredTableNames = tableNames.stream()
                .filter(name -> !name.contains(".")).collect(Collectors.toList());

        for (final String tableName: filteredTableNames) {
            result.addElement(LookupElementBuilder.create(tableName));
        }
    }
}
