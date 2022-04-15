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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.xml.SystemXmlGroupIndex;
import com.magento.idea.magento2plugin.util.magento.xml.SystemConfigurationParserUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public class GroupNameCompletionProvider extends CompletionProvider<CompletionParameters> {

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
        final XmlTag startingTag = PsiTreeUtil.getParentOfType(
                position,
                XmlTag.class
        );
        if (startingTag == null) {
            return;
        }
        final String currentPath = SystemConfigurationParserUtil
                .parseOuterConfigPath(
                        startingTag,
                        SystemConfigurationParserUtil.ParsingDepth.SECTION_ID
                );

        if (currentPath == null) {
            return;
        }
        final String prefix = result.getPrefixMatcher().getPrefix().trim();
        final String capture = currentPath + "." + prefix;

        for (final LookupElement element : makeCompletion(capture, position.getProject())) {
            result.addElement(element);
        }
    }

    /**
     * Make completion for groups.
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
                SystemXmlGroupIndex.KEY,
                project
        );
        final AtomicInteger resultsCounter = new AtomicInteger(0);

        allKeys.stream()
                .filter(input -> input.startsWith(phrase) && !input.equals(phrase))
                .takeWhile(input -> resultsCounter.get() < 5)
                .forEach(
                        input -> {
                            if (FileBasedIndex.getInstance().getValues(
                                    SystemXmlGroupIndex.KEY,
                                    input,
                                    GlobalSearchScope.allScope(project)).isEmpty()
                            ) {
                                return;
                            }
                            final String[] groupNameParts = input.split("\\.");

                            if (groupNameParts.length != 2) { //NOPMD
                                return;
                            }
                            final String groupId = groupNameParts[1];
                            result.add(
                                    LookupElementBuilder.create(groupId)
                                            .withIcon(MagentoIcons.MODULE)
                            );
                            resultsCounter.incrementAndGet();
                        }
                );

        return result;
    }
}
