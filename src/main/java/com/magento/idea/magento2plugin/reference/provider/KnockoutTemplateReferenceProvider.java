/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class KnockoutTemplateReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!Settings.isEnabled(element.getProject())) {
            NavigationInstrumentation.infoOnce(
                    "ko-template-reference-disabled-" + element.getProject().getLocationHash(),
                    () -> "Knockout template references skipped: Magento support disabled; "
                            + NavigationInstrumentation.describeSettings(element.getProject())
            );
            return PsiReference.EMPTY_ARRAY;
        }
        final JSProperty property = PsiTreeUtil.getParentOfType(element, JSProperty.class);
        final String templatePath = KnockoutTemplatePathResolver.getInstance().getTemplatePath(property);

        if (templatePath == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        NavigationInstrumentation.infoOnce(
                "ko-template-reference-seen-" + templatePath,
                () -> "Knockout template reference candidate '" + templatePath + "' in "
                        + NavigationInstrumentation.describeElement(element)
        );
        final List<PsiElement> targets = KnockoutTemplatePathResolver.getInstance()
                .resolveTemplateFiles(element.getProject(), templatePath);

        if (targets.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "ko-template-reference-empty-" + templatePath,
                    () -> "Knockout template reference has no targets for '" + templatePath + "'"
            );
            return PsiReference.EMPTY_ARRAY;
        }
        final int startOffset = element.getText().indexOf(templatePath);

        if (startOffset < 0) {
            return PsiReference.EMPTY_ARRAY;
        }
        NavigationInstrumentation.infoOnce(
                "ko-template-reference-created-" + templatePath,
                () -> "Knockout template reference created for '" + templatePath
                        + "' targets=" + targets.size()
        );

        return new PsiReference[] {
                new PolyVariantReferenceBase(
                        element,
                        new TextRange(startOffset, startOffset + templatePath.length()),
                        targets
                )
        };
    }
}
