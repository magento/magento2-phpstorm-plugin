package com.magento.idea.magento2plugin.xml.layout.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIcons;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;
import com.magento.idea.magento2plugin.xml.completion.ClassCompletionProvider;
import com.magento.idea.magento2plugin.xml.completion.CompletionProviderI;
import com.magento.idea.magento2plugin.xml.completion.VirtualTypeCompletionProvider;
import com.magento.idea.magento2plugin.xml.layout.index.AbstractComponentNameFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.BlockFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.ContainerFileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class LayoutCompletionContributor extends CompletionContributor {
    private CompletionProviderI[] typeCompletionProviders = new CompletionProviderI[] {
        ClassCompletionProvider.INSTANCE,
        VirtualTypeCompletionProvider.INSTANCE
    };

    private ReferenceComponentCompletionProvider containerCompletionProvider = new ReferenceComponentCompletionProvider(ContainerFileBasedIndex.NAME);
    private ReferenceComponentCompletionProvider blockCompletionProvider = new ReferenceComponentCompletionProvider(BlockFileBasedIndex.NAME);


    public LayoutCompletionContributor() {
        extend(
            CompletionType.BASIC,
            XmlHelperUtility.getTagAttributePattern("block", "class"),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getOriginalPosition();
                    for (CompletionProviderI completionProvider: typeCompletionProviders) {
                        completionResultSet.addAllElements(completionProvider.collectCompletionResult(psiElement));
                    }
                }
            }
        );

        extend(
            CompletionType.BASIC,
            XmlHelperUtility.getTagAttributePattern("referenceContainer", "name"),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getOriginalPosition();
                    completionResultSet.addAllElements(containerCompletionProvider.collectCompletionResult(psiElement));
                }
            }
        );

        extend(
            CompletionType.BASIC,
            XmlHelperUtility.getTagAttributePattern("referenceBlock", "name"),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getOriginalPosition();
                    completionResultSet.addAllElements(blockCompletionProvider.collectCompletionResult(psiElement));
                }
            }
        );
    }
}
