/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.indexes.DiIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import gnu.trove.THashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PhpConstructorArgumentReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final List<PsiReference> psiReferences = new ArrayList<>();
        final String parameterName = StringUtil.unquoteString(element.getText());

        if (!parameterName.isEmpty()) {
            final DiIndex diIndex = DiIndex.getInstance(element.getProject());
            final PhpClass phpClass = diIndex.getPhpClassOfArgument((XmlElement) element);
            if (phpClass != null && phpClass.getConstructor() != null) {
                final Method constructor = phpClass.getConstructor();
                final Collection<Parameter> parameterList
                        = new THashSet<>(Arrays.asList(constructor.getParameters()));
                parameterList.removeIf(p -> !p.getName().contains(parameterName));
                if (!parameterList.isEmpty()) { // NOPMD
                    psiReferences.add(new PolyVariantReferenceBase(element, parameterList));
                }
            }
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
