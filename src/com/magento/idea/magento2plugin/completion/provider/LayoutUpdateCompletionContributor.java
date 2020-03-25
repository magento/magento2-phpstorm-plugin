/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
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
import com.magento.idea.magento2plugin.indexes.LayoutIndex;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LayoutUpdateCompletionContributor extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        List<XmlFile> targets = LayoutIndex.getLayoutFiles(position.getProject());
        if (targets.size() > 0) {
            for (XmlFile file : targets) {
                result.addElement(
                    LookupElementBuilder
                            .create(file.getVirtualFile().getNameWithoutExtension())
                            .withIcon(PhpIcons.XML_TAG_ICON)
                );
            }
        }
    }
}
