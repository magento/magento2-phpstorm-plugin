package com.magento.idea.magento2plugin.xml.completion;

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/17/15.
 */
public class ClassCompletionProvider implements CompletionProviderI<PsiElement> {
    public final static ClassCompletionProvider INSTANCE = new ClassCompletionProvider();

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement, @Nullable PsiContextMatcherI<PsiElement> context) {
        List<LookupElement> result = new ArrayList<>();
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());
        String prefix = StringUtil.unquoteString(psiElement.getText());

        Collection<String> classNames = phpIndex.getAllClassNames(new CamelHumpMatcher(prefix));

        for (String className: classNames) {
            Collection<PhpClass> classesByName = phpIndex.getClassesByName(className);
            for (PhpClass phpClass: classesByName) {
                if (context != null && !context.match(phpClass)) {
                    continue;
                }

                String classFqn = phpClass.getPresentableFQN();

                result.add(
                    LookupElementBuilder
                        .create(classFqn)
                        .withIcon(PhpIcons.CLASS_ICON)
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
