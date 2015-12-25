package com.magento.idea.magento2plugin.xml.observer.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.php.util.MagentoTypes;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;
import com.magento.idea.magento2plugin.xml.completion.ClassCompletionProvider;
import com.magento.idea.magento2plugin.xml.completion.VirtualTypeCompletionProvider;
import com.magento.idea.magento2plugin.php.util.ImplementationMatcher;
import com.magento.idea.magento2plugin.xml.observer.XmlHelper;
import com.magento.idea.magento2plugin.xml.observer.index.EventsDeclarationsFileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by dkvashnin on 11/17/15.
 */
public class EventCompletionContributor extends CompletionContributor {
    public EventCompletionContributor() {
        extend(
            CompletionType.BASIC,
            XmlHelperUtility.getTagAttributePattern(XmlHelper.OBSERVER_TAG, XmlHelper.INSTANCE_ATTRIBUTE, XmlHelper.FILE_TYPE),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    ImplementationMatcher completionContext = new ImplementationMatcher(MagentoTypes.OBSERVER_TYPE);
                    PsiElement psiElement = completionParameters.getOriginalPosition();

                    completionResultSet.addAllElements(
                        ClassCompletionProvider.INSTANCE.collectCompletionResult(psiElement, completionContext)
                    );
                    completionResultSet.addAllElements(VirtualTypeCompletionProvider.INSTANCE.collectCompletionResult(psiElement));
                }
            }
        );

        extend(
            CompletionType.BASIC,
            XmlHelperUtility.getTagAttributePattern(XmlHelper.EVENT_TAG, XmlHelper.NAME_ATTRIBUTE, XmlHelper.FILE_TYPE),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getOriginalPosition();
                    Collection<String> eventNames = FileBasedIndex.getInstance().getAllKeys(EventsDeclarationsFileBasedIndex.NAME, psiElement.getProject());

                    for (String eventName: eventNames) {
                        completionResultSet.addElement(LookupElementBuilder.create(eventName));
                    }
                }
            }
        );
    }

}

