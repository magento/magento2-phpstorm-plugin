package com.magento.idea.magento2plugin.xml.di.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.completion.ClassCompletionProvider;
import com.magento.idea.magento2plugin.xml.completion.CompletionProviderI;
import com.magento.idea.magento2plugin.xml.completion.VirtualTypeCompletionProvider;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 10/15/15.
 */
public class DiCompletionContributor extends CompletionContributor {
    private CompletionProviderI[] completionProviders = new CompletionProviderI[] {
        ClassCompletionProvider.INSTANCE,
        VirtualTypeCompletionProvider.INSTANCE
    };

    public DiCompletionContributor() {
        extend(CompletionType.BASIC,
            XmlPatterns.or(
                XmlHelper.getArgumentValuePatternForType("object"),
                XmlHelper.getItemValuePatternForType("object"),
                XmlHelper.getTagAttributePattern(XmlHelper.TYPE_TAG, XmlHelper.NAME_ATTRIBUTE),
                XmlHelper.getTagAttributePattern(XmlHelper.PREFERENCE_TAG, XmlHelper.TYPE_ATTRIBUTE),
                XmlHelper.getTagAttributePattern(XmlHelper.VIRTUAL_TYPE_TAG, XmlHelper.TYPE_ATTRIBUTE),
                XmlHelper.getTagAttributePattern(XmlHelper.PLUGIN_TAG, XmlHelper.TYPE_ATTRIBUTE)
            ),
            new CompletionProvider<CompletionParameters>() {
                public void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
                    PsiElement psiElement = parameters.getOriginalPosition();
                    for (CompletionProviderI completionProvider: completionProviders) {
                        resultSet.addAllElements(completionProvider.collectCompletionResult(psiElement));
                    }
                }
            }
        );
    }
}
