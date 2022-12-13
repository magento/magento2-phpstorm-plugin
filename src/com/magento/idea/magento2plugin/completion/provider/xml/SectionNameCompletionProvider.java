/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.provider.xml;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.xml.SystemXmlSectionIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public class SectionNameCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(
            final @NotNull CompletionParameters parameters,
            final @NotNull ProcessingContext context,
            final @NotNull CompletionResultSet result
    ) {
        final PsiElement position = parameters.getPosition().getOriginalElement();

        if (position == null || !Settings.isEnabled(position.getProject())) {
            return;
        }
        final String prefix = result.getPrefixMatcher().getPrefix().trim();

        for (final LookupElement element : makeCompletion(prefix, position.getProject())) {
            result.addElement(element);
        }
    }

    /**
     * Make completion for sections.
     *
     * @param phrase String
     * @param project Project
     *
     * @return List[LookupElement]
     */
    public static List<LookupElement> makeCompletion(
            final @NotNull String phrase,
            final @NotNull Project project
    ) {
        final List<LookupElement> result = new ArrayList<>();
        final Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(
                SystemXmlSectionIndex.KEY,
                project
        );
        final AtomicInteger resultsCounter = new AtomicInteger(0);

        allKeys.stream()
                .filter(input -> input.startsWith(phrase) && !input.equals(phrase))
                .takeWhile(input -> resultsCounter.get() < 5)
                .forEach(
                        input -> {
                            if (FileBasedIndex.getInstance().getValues(
                                    SystemXmlSectionIndex.KEY,
                                    input,
                                    GlobalSearchScope.allScope(project)).isEmpty()
                            ) {
                                return;
                            }
                            result.add(LookupElementBuilder
                                    .create(input).withIcon(MagentoIcons.MODULE));
                            resultsCounter.incrementAndGet();
                        }
                );

        return result;
    }
}
