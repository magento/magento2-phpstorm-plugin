/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.provider.mftf;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.MftfActionGroup;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.ActionGroupIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ActionGroupCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result)
    {
        PsiElement position = parameters.getPosition().getOriginalElement();

        if (position == null) {
            return;
        }

        Collection<String> selectorNames
            = FileBasedIndex.getInstance().getAllKeys(ActionGroupIndex.KEY, position.getProject());
        String currentActionGroup = getCurrentActionGroupName((XmlAttributeValue) parameters.getPosition().getParent());

        for (String selectorName: selectorNames) {
            if (selectorName.equals(currentActionGroup)) {
                continue;
            }

            result.addElement(LookupElementBuilder.create(selectorName));
        }
    }

    private String getCurrentActionGroupName(XmlAttributeValue xmlAttributeValue) {
        PsiElement xmlAttribute = xmlAttributeValue.getParent();
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlAttribute, XmlTag.class);

        if (xmlTag == null) {
            return null;
        }

        XmlAttribute nameAttribute = xmlTag.getAttribute(MftfActionGroup.NAME_ATTRIBUTE);

        if (nameAttribute == null) {
            return null;
        }

        String value = nameAttribute.getValue();

        if (value == null || value.isEmpty()) {
            return null;
        }

        return value;
    }
}
