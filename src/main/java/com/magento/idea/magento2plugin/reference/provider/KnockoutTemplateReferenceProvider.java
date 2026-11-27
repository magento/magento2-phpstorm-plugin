/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockoutTemplateReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!Settings.isEnabled(element.getProject())) {
            return PsiReference.EMPTY_ARRAY;
        }
        final JSProperty property = PsiTreeUtil.getParentOfType(element, JSProperty.class);
        final String templatePath = getTemplatePath(property);

        if (templatePath == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        final List<PsiElement> targets = KnockoutTemplatePathResolver.getInstance()
                .resolveTemplateFiles(element.getProject(), templatePath);

        if (targets.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }
        final int startOffset = element.getText().indexOf(templatePath);

        if (startOffset < 0) {
            return PsiReference.EMPTY_ARRAY;
        }

        return new PsiReference[] {
                new PolyVariantReferenceBase(
                        element,
                        new TextRange(startOffset, startOffset + templatePath.length()),
                        targets
                )
        };
    }

    private static @Nullable String getTemplatePath(final @Nullable JSProperty property) {
        if (property == null) {
            return null;
        }
        final JSExpression value = property.getValue();
        if (!(value instanceof JSLiteralExpression)) {
            return null;
        }
        final PsiElement parent = property.getParent();
        final JSProperty parentProperty = parent == null ? null : PsiTreeUtil.getParentOfType(parent, JSProperty.class);

        return KnockoutTemplatePathResolver.getInstance().getTemplatePath(
                property.getName(),
                parentProperty == null ? null : parentProperty.getName(),
                value.getText()
        );
    }
}
