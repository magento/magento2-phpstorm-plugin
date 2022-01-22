/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpNamespaceIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
public class PhpClassReferenceProvider extends PsiReferenceProvider {

    @Override
    public @NotNull PsiReference[] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        final String origValue = element.getText();

        final Pattern pattern = Pattern.compile(RegExUtil.PhpRegex.FQN);
        final Matcher matcher = pattern.matcher(origValue);
        if (!matcher.find()) {
            return PsiReference.EMPTY_ARRAY;
        }

        final String classFQN = origValue.replaceAll("^\"|\"$", "");
        final String[] fqnParts = classFQN.split("\\\\");

        final PhpIndex phpIndex = PhpIndex.getInstance(element.getProject());

        final StringBuilder namespace = new StringBuilder();
        String namespacePart;
        final List<PsiReference> psiReferences = new ArrayList<>();

        for (int i = 0; i < fqnParts.length - 1; i++) {
            namespacePart = fqnParts[i];
            namespace.append('\\');
            namespace.append(namespacePart);

            final String namespaceId = namespace
                    .toString()
                    .toLowerCase(new Locale("en","EN"));

            final Collection<PhpNamespace> references = hasNamespaceInIndex(
                    namespaceId,
                    element.getProject()
            ) ? phpIndex.getNamespacesByName(namespaceId) : new ArrayList<>();

            if (!references.isEmpty()) {
                final TextRange range = new TextRange(
                        origValue.indexOf(classFQN) + namespace.toString().lastIndexOf(92),
                        origValue.indexOf(classFQN) + namespace.toString().lastIndexOf(92)
                                + namespacePart.length()
                );
                psiReferences.add(new PolyVariantReferenceBase(element, range, references));
            }
        }
        final String className = classFQN.substring(classFQN.lastIndexOf(92) + 1);

        try {
            final Collection<PhpClass> classes = phpIndex.getAnyByFQN(classFQN);

            if (!classes.isEmpty()) {
                final TextRange range = new TextRange(
                        origValue.lastIndexOf(92) + 1,
                        origValue.lastIndexOf(92) + 1 + className.length()
                );
                psiReferences.add(new PolyVariantReferenceBase(element, range, classes));
            }
        } catch (Exception exception) { //NOPMD
            return psiReferences.toArray(new PsiReference[0]);
        }

        return psiReferences.toArray(new PsiReference[0]);
    }

    /**
     * Check if php namespace index has specified identifier.
     *
     * @param namespaceIdentifier String
     * @param project Project
     *
     * @return boolean
     */
    private boolean hasNamespaceInIndex(
            final @NotNull String namespaceIdentifier,
            final @NotNull Project project
    ) {
        final Collection<String> keys = StubIndex.getInstance().getAllKeys(
                PhpNamespaceIndex.KEY,
                project
        );

        return keys.contains(namespaceIdentifier);
    }
}
