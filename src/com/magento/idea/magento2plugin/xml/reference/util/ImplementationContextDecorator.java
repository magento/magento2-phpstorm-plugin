package com.magento.idea.magento2plugin.xml.reference.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.php.util.ImplementationMatcher;

import java.util.List;

/**
 * Created by dkvashnin on 12/18/15.
 */
public class ImplementationContextDecorator implements ReferenceResultsFiller {
    private ReferenceResultsFiller subject;
    private ImplementationMatcher implementationMatcher;

    public ImplementationContextDecorator(ReferenceResultsFiller subject, ImplementationMatcher implementationMatcher) {
        this.subject = subject;
        this.implementationMatcher = implementationMatcher;
    }

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        subject.fillResolveResults(psiElement, results, typeName);

        for (ResolveResult resolveResult : results) {
            PsiElement element = resolveResult.getElement();

            if (element instanceof PhpClass && !implementationMatcher.match(element)) {
                results.remove(resolveResult);
            }
        }

    }
}
