package com.magento.idea.magento2plugin.xml.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mslabko on 18.12.2015.
 */
public class InterfaceCompletionProvider implements CompletionProviderI<PsiElement> {
    public final static InterfaceCompletionProvider INSTANCE = new InterfaceCompletionProvider();

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement, @Nullable PsiContextMatcherI<PsiElement> context) {
        List<LookupElement> result = new ArrayList<>();
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());
        String input = StringUtils.strip(psiElement.getText(), "\"");

        Collection<String> interfaceNames = phpIndex.getAllInterfaceNames();

        for (String interfaceName: interfaceNames) {
            if (!interfaceName.toLowerCase().contains(input.toLowerCase())) {
                continue;
            }

            Collection<PhpClass> classesByName = phpIndex.getInterfacesByName(interfaceName);
            for (PhpClass phpClass: classesByName) {
                if (context != null && !context.match(phpClass)) {
                    continue;
                }

                String classFqn = phpClass.getPresentableFQN();

                result.add(
                    LookupElementBuilder
                        .create(classFqn)
                        .withIcon(PhpIcons.INTERFACE_ICON)
                );
            }
        }

        return result;
    }

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement) {
        return collectCompletionResult(psiElement, null);
    }
}
