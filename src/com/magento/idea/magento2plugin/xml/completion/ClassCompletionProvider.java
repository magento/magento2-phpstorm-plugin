package com.magento.idea.magento2plugin.xml.completion;

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/17/15.
 */
public class ClassCompletionProvider implements CompletionProviderI {
    public final static CompletionProviderI INSTANCE = new ClassCompletionProvider();

    @Override
    public List<LookupElement> collectCompletionResult(PsiElement psiElement) {
        List<LookupElement> result = new ArrayList<>();
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());
        String prefix = StringUtil.unquoteString(psiElement.getText());

        Collection<String> classNames = phpIndex.getAllClassNames(new CamelHumpMatcher(prefix));

        for (String className: classNames) {
            Collection<PhpClass> classesByName = phpIndex.getClassesByName(className);
            for (PhpClass phpClass: classesByName) {
                String classFqn = phpClass.getPresentableFQN();
                if (classFqn == null) {
                    continue;
                }

                result.add(
                    LookupElementBuilder
                        .create(classFqn)
                        .withIcon(PhpIcons.CLASS_ICON)
                );
            }
        }

        return result;
    }
}
