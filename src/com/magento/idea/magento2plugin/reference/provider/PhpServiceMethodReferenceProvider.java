/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
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
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.indexes.DiIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PhpServiceMethodReferenceProvider  extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!(element instanceof XmlElement)) {
            return PsiReference.EMPTY_ARRAY;
        }

        List<PsiReference> psiReferences = new ArrayList<>();

        String methodName = StringUtil.unquoteString(element.getText());

        PhpClass phpClass = DiIndex.getPhpClassOfServiceMethod((XmlElement) element);
        if (phpClass != null) {
            Collection<Method> methods = phpClass.getMethods();
            methods.removeIf(m -> !m.getName().equalsIgnoreCase(methodName));
            psiReferences.add(new PolyVariantReferenceBase(element, methods));
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }
}
