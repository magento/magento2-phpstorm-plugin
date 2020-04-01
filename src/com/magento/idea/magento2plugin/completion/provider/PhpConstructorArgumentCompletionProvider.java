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
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.indexes.DiIndex;
import org.jetbrains.annotations.NotNull;

public class PhpConstructorArgumentCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null || !(position instanceof XmlElement)) {
            return;
        }


        DiIndex diIndex = DiIndex.getInstance(position.getProject());
        PhpClass phpClass = diIndex.getPhpClassOfArgument((XmlElement) position);
        if (phpClass != null) {
            Method constructor = phpClass.getConstructor();
            if (constructor != null) {
                for (Parameter parameter : constructor.getParameters()) {
                    result.addElement(
                            LookupElementBuilder
                                    .create(parameter.getName())
                                    .withIcon(parameter.getIcon())
                    );
                }
            }
        }
    }
}
