package com.magento.idea.magento2plugin.completion.xml.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.magento.idea.magento2plugin.indexes.LayoutIndex;
import com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LayoutBlockCompletionContributor extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        Collection<String> keys = LayoutIndex.getAllKeys(BlockNameIndex.KEY, position.getProject());
        for (String key: keys) {
            result.addElement(
                LookupElementBuilder.create(key).withIcon(PhpIcons.XML_TAG_ICON)
            );
        }
    }
}
