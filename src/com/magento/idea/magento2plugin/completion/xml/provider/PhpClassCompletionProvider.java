package com.magento.idea.magento2plugin.completion.xml.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.magento.idea.magento2plugin.php.util.PhpRegex;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhpClassCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        String prefix = result.getPrefixMatcher().getPrefix();
        prefix = prefix.startsWith("\\") ? prefix : "\\" + prefix;

        Matcher matcher = Pattern.compile(PhpRegex.Xml.CLASS_NAME).matcher(prefix);

        if (!matcher.matches()) {
            return;
        }

        String prefixShortName = prefix.substring(prefix.lastIndexOf(92) + 1);
        String namespace = prefix.substring(0, prefix.lastIndexOf(92));
        namespace = namespace.startsWith("\\") ? namespace : "\\" + namespace;

        PhpIndex phpIndex = PhpIndex.getInstance(parameters.getPosition().getProject());

        final Collection<PhpClass> phpClasses = new THashSet<>();
        for (PhpNamespace phpNamespace : phpIndex.getNamespacesByName(namespace.toLowerCase())) {
            phpClasses.addAll(PsiTreeUtil.getChildrenOfTypeAsList(phpNamespace.getStatements(), PhpClass.class));
        }
        phpClasses.removeIf(c -> !c.getName().contains(prefixShortName));

        for (PhpClass phpClass : phpClasses) {
            result.addElement(
                    LookupElementBuilder
                            .create(phpClass.getPresentableFQN())
                            .withIcon(phpClass.getIcon())
            );
        }

        final Collection<String> namespaces = phpIndex.getChildNamespacesByParentName(namespace + "\\");
        for (String namespaceName : namespaces) {
            if (namespaceName.contains(prefixShortName)) {
                result.addElement(
                        LookupElementBuilder
                                .create(namespace.substring(1) + "\\" + namespaceName)
                                .withIcon(PhpIcons.NAMESPACE)
                );
            }
        }
    }
}
