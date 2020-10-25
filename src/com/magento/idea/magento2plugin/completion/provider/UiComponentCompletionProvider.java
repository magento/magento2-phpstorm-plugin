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
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.magento.idea.magento2plugin.indexes.UIComponentIndex;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public class UiComponentCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull final CompletionParameters parameters,
                                  final ProcessingContext context,
                                  @NotNull final CompletionResultSet result) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        final List<XmlFile> targets = UIComponentIndex.getUiComponentFiles(position.getProject());
        if (!targets.isEmpty()) {
            for (final XmlFile file : targets) {
                result.addElement(LookupElementBuilder
                            .create(file.getVirtualFile().getNameWithoutExtension())
                            .withIcon(PhpIcons.XML_TAG_ICON)
                );
            }
        }
    }
}
