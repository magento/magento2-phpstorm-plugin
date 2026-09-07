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
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.LayoutIndex;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public class LayoutUpdateCompletionContributor extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        final List<XmlFile> targets = LayoutIndex.getLayoutFiles(position.getProject());
        if (!targets.isEmpty()) {
            for (final XmlFile file : targets) {
                result.addElement(
                        LookupElementBuilder
                            .create(file.getVirtualFile().getNameWithoutExtension())
                            .withIcon(AllIcons.Nodes.Tag)
                );
            }
        }
    }
}
