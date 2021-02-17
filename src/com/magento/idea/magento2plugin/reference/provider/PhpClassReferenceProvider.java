/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhpClassReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        List<PsiReference> psiReferences = new ArrayList<>();

        String origValue = element.getText();

        Pattern pattern = Pattern.compile(RegExUtil.PhpRegex.FQN);
        Matcher matcher = pattern.matcher(origValue);
        if (!matcher.find()) {
            return PsiReference.EMPTY_ARRAY;
        }

        String classFQN = origValue.replaceAll("^\"|\"$", "");;
        String[] fqnParts = classFQN.split("\\\\");

        PhpIndex phpIndex = PhpIndex.getInstance(element.getProject());

        StringBuilder namespace = new StringBuilder();
        String namespacePart;
        for (int i = 0; i < fqnParts.length - 1; i++) {
            namespacePart = fqnParts[i];

            namespace.append("\\");
            namespace.append(namespacePart);
            Collection<PhpNamespace> references = phpIndex.getNamespacesByName(namespace.toString().toLowerCase());
            if (references.size() > 0) {
                TextRange range = new TextRange(
                        origValue.indexOf(classFQN) + namespace.toString().lastIndexOf(92),
                        origValue.indexOf(classFQN) + namespace.toString().lastIndexOf(92) + namespacePart.length()
                );
                psiReferences.add(new PolyVariantReferenceBase(element, range, references));
            }
        }

        String className = classFQN.substring(classFQN.lastIndexOf(92) + 1);
        Collection<PhpClass> classes = phpIndex.getAnyByFQN(classFQN);
        if (classes.size() > 0) {
            TextRange range = new TextRange(
                    origValue.lastIndexOf(92) + 1,
                    origValue.lastIndexOf(92) + 1 + className.length()
            );
            psiReferences.add(new PolyVariantReferenceBase(element, range, classes));
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }
}
