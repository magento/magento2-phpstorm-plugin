/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.JsIndex;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import org.jetbrains.annotations.NotNull;

public class RequireJsPreferenceReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        NavigationInstrumentation.infoOnce(
                "requirejs-reference-seen-" + element.getText(),
                () -> "RequireJS reference candidate in "
                        + NavigationInstrumentation.describeElement(element)
                        + "; " + NavigationInstrumentation.describeSettings(element.getProject())
        );
        JsIndex index = JsIndex.getInstance();
        final PsiReference[] references = index.getRequireJsPreferences(element, element.getResolveScope());

        if (references.length == 0) {
            NavigationInstrumentation.infoOnce(
                    "requirejs-reference-empty-" + element.getText(),
                    () -> "RequireJS reference produced no targets for " + element.getText()
            );
        } else {
            NavigationInstrumentation.infoOnce(
                    "requirejs-reference-created-" + element.getText(),
                    () -> "RequireJS reference created for " + element.getText()
                            + " references=" + references.length
            );
        }

        return references;
    }
}
