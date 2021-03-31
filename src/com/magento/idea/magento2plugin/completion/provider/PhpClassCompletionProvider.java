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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class PhpClassCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final String PHP_CLASS_COMPLETION_REGEX
            = "\\\\?" + RegExUtil.PhpRegex.FQN + "\\\\?";

    @SuppressWarnings({
            "PMD.CyclomaticComplexity",
            "PMD.NPathComplexity"
    })
    @Override
    protected void addCompletions(
            final @NotNull CompletionParameters parameters,
            final ProcessingContext context,
            final @NotNull CompletionResultSet result
    ) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }
        final String prefix = result.getPrefixMatcher().getPrefix();
        final Matcher matcher = Pattern.compile(PHP_CLASS_COMPLETION_REGEX)
                .matcher(prefix);
        if (!matcher.matches()) {
            return;
        }

        final String className = prefix.lastIndexOf(92) < 0 ? prefix : prefix
                .substring(prefix.lastIndexOf(92) + 1);
        final String namespace = prefix.lastIndexOf(92) < 0 ? "" : prefix
                .substring(0, prefix.lastIndexOf(92));

        final PhpIndex phpIndex = PhpIndex.getInstance(
                parameters.getPosition().getProject()
        );

        final Collection<PhpClass> phpClasses = new THashSet<>();
        Collection<String> namespaceNames = new ArrayList<>();

        if (className.isEmpty()) {
            // add namespaces
            final Collection<PhpNamespace> namespaces
                    = phpIndex.getNamespacesByName(("\\" + namespace)
                        .toLowerCase(Locale.ROOT));
            for (final PhpNamespace nsp: namespaces) {
                phpClasses.addAll(PsiTreeUtil.getChildrenOfTypeAsList(
                        nsp.getStatements(),
                        PhpClass.class
                    )
                );
            }

            // add namespaces and classes (string representation)
            namespaceNames
                = phpIndex.getChildNamespacesByParentName("\\".concat(namespace)
                .concat("\\").toLowerCase(Locale.ROOT));
            namespaceNames
                = namespaceNames.stream().map(n -> namespace.concat("\\")
                .concat(n)).collect(Collectors.toList());
        } else {
            // add interfaces
            final Collection<String> interfaceNames = phpIndex.getAllInterfaceNames();
            interfaceNames.removeIf(i -> !i.contains(className.toLowerCase(Locale.ROOT)));
            for (final String iName: interfaceNames) {
                phpClasses.addAll(phpIndex.getInterfacesByName(iName));
            }
            // add classes
            final Collection<String> classNames = phpIndex.getAllClassNames(
                    new CamelHumpMatcher(className)
            );
            for (final String cName: classNames) {
                phpClasses.addAll(phpIndex.getClassesByName(cName));
            }
            if (namespace.isEmpty()) {
                namespaceNames = phpIndex.getChildNamespacesByParentName("\\");
                namespaceNames.removeIf(n -> !n.contains(prefix));
            } else {
                phpClasses.removeIf(c -> !c.getPresentableFQN().startsWith(namespace));
            }
        }

        // add all above founded items to lookup builder
        // order is important (items with the same name override each other),
        // add classes first
        for (final PhpClass phpClass : phpClasses) {
            result.addElement(
                    LookupElementBuilder
                            .create(phpClass.getPresentableFQN())
                            .withIcon(phpClass.getIcon())
            );
        }

        for (final String nsName : namespaceNames) {
            result.addElement(
                    LookupElementBuilder
                            .create(nsName)
                            .withIcon(PhpIcons.NAMESPACE)
            );
        }
    }
}
