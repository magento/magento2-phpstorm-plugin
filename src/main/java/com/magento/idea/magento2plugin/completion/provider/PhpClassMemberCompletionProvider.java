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
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PhpClassMemberCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        final Collection<PhpClassMember> members = new THashSet<>();
        String prefix = result.getPrefixMatcher().getPrefix();

        if (!(prefix.lastIndexOf("::") > 0 && prefix.lastIndexOf("\\") > 0
                && prefix.lastIndexOf("::") > prefix.lastIndexOf("\\"))) {
            return;
        }

        String className = prefix.substring(0, prefix.lastIndexOf("::"));

        PhpIndex phpIndex = PhpIndex.getInstance(parameters.getPosition().getProject());
        for (PhpClass phpClass : phpIndex.getAnyByFQN(className)) {
            members.addAll(phpClass.getFields());
            members.addAll(phpClass.getMethods());
        }

        for (PhpClassMember member : members) {
            if (Field.class.isInstance(member)) {
                result.addElement(
                        LookupElementBuilder
                                .create(className + (((Field) member).isConstant() ? "::" : "::$") + member.getName())
                                .withIcon(member.getIcon())
                );
            } else {
                result.addElement(
                        LookupElementBuilder
                                .create(className + "::" + member.getName() + "()")
                                .withIcon(member.getIcon())
                );
            }
        }
    }
}
