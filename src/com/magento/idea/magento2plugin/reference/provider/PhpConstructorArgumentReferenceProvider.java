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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class PhpConstructorArgumentReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

        String parameterName = StringUtil.unquoteString(element.getText());
        if (parameterName.isEmpty() || !(element instanceof XmlElement)) {
            return PsiReference.EMPTY_ARRAY;
        }


        DiIndex diIndex = DiIndex.getInstance(element.getProject());
        PhpClass phpClass = diIndex.getPhpClassOfArgument((XmlElement) element);
        if (phpClass != null) {
            Method constructor = phpClass.getConstructor();
            if (constructor != null) {
                Collection<Parameter> parameterList = new THashSet<>(Arrays.asList(constructor.getParameters()));
                parameterList.removeIf(p -> !p.getName().contains(parameterName));
                if (parameterList.size() > 0) {
                    return new PsiReference[] {new PolyVariantReferenceBase(element, parameterList)};
                }
            }
        }

        return PsiReference.EMPTY_ARRAY;
    }
}
