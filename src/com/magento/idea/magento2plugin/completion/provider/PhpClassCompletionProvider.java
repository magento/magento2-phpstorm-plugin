/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.magento.idea.magento2plugin.util.RegExUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PhpClassCompletionProvider extends CompletionProvider<CompletionParameters> {

    final private static String PHP_CLASS_COMPLETION_REGEX
            = "\\\\?" + RegExUtil.PhpRegex.FQN + "\\\\?";

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }
        String prefix = result.getPrefixMatcher().getPrefix();
        Matcher matcher = Pattern.compile(PHP_CLASS_COMPLETION_REGEX).matcher(prefix);
        if (!matcher.matches()) {
            return;
        }

        String className = prefix.lastIndexOf(92) < 0 ? prefix : prefix.substring(prefix.lastIndexOf(92) + 1);
        String namespace = prefix.lastIndexOf(92) < 0 ? "" : prefix.substring(0, prefix.lastIndexOf(92));

        PhpIndex phpIndex = PhpIndex.getInstance(parameters.getPosition().getProject());

        final Collection<PhpClass> phpClasses = new THashSet<>();
        Collection<String> namespaceNames = new ArrayList<>();

        if (!className.isEmpty()) {
            // case for input: "SomeClassOrNamespace"

            // add classes
            Collection<String> classNames = phpIndex.getAllClassNames(new CamelHumpMatcher(className));
            for (String cName: classNames) {
                phpClasses.addAll(phpIndex.getClassesByName(cName));
            }
            // add interfaces
            Collection<String> interfaceNames = phpIndex.getAllInterfaceNames();
            interfaceNames.removeIf(i -> !i.contains(className));
            for (String iName: interfaceNames) {
                phpClasses.addAll(phpIndex.getInterfacesByName(iName));
            }
            if (!namespace.isEmpty()) {
                phpClasses.removeIf(c -> !c.getPresentableFQN().startsWith(namespace));
            } else {
                namespaceNames = phpIndex.getChildNamespacesByParentName("\\");
                namespaceNames.removeIf(n -> !n.contains(prefix));
            }
        } else {
            // case for input: "Some\Namespace\ + ^+<Space>"

            // add namespaces
            Collection<PhpNamespace> namespaces = phpIndex.getNamespacesByName(("\\" + namespace).toLowerCase());
            for (PhpNamespace nsp: namespaces) {
                phpClasses.addAll(PsiTreeUtil.getChildrenOfTypeAsList(nsp.getStatements(), PhpClass.class));
            }

            // add namespaces and classes (string representation)
            namespaceNames
                    = phpIndex.getChildNamespacesByParentName("\\".concat(namespace).concat("\\").toLowerCase());
            namespaceNames
                    = namespaceNames.stream().map(n -> namespace.concat("\\").concat(n)).collect(Collectors.toList());
        }

        // add all above founded items to lookup builder
        // order is important (items with the same name override each other), add classes first
        for (PhpClass phpClass : phpClasses) {
            result.addElement(
                    LookupElementBuilder
                            .create(phpClass.getPresentableFQN())
                            .withIcon(phpClass.getIcon())
            );
        }

        for (String nsName : namespaceNames) {
            result.addElement(
                    LookupElementBuilder
                            .create(nsName)
                            .withIcon(PhpIcons.NAMESPACE)
            );
        }
    }
}
