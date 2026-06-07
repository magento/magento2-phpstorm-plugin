/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RequireJsPathReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        final String requireJsPath = getElementValue(element);

        if (requireJsPath == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        final List<PsiElement> targets = RequireJsPathResolver.getInstance()
                .resolveJsFilesOrAlias(element.getProject(), requireJsPath);

        if (targets.isEmpty()) {
            NavigationInstrumentation.info(
                    "RequireJS path reference has no targets for '" + requireJsPath + "' in "
                            + NavigationInstrumentation.describeElement(element)
            );
            return PsiReference.EMPTY_ARRAY;
        }
        NavigationInstrumentation.info(
                "RequireJS path reference created for '" + requireJsPath
                        + "' targets=" + targets.size() + " in "
                        + NavigationInstrumentation.describeElement(element)
        );

        return new PsiReference[] {
                new PolyVariantReferenceBase(element, getValueRange(element, requireJsPath), targets)
        };
    }

    private @Nullable String getElementValue(final @NotNull PsiElement element) {
        final String value = element instanceof XmlAttributeValue
                ? ((XmlAttributeValue) element).getValue()
                : element.getText().trim();

        return value.isBlank() ? null : value;
    }

    private @NotNull TextRange getValueRange(
            final @NotNull PsiElement element,
            final @NotNull String value
    ) {
        final String text = element.getText();
        final int startOffset = text.indexOf(value);

        if (startOffset >= 0) {
            return new TextRange(startOffset, startOffset + value.length());
        }

        return new TextRange(0, text.length());
    }
}
