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
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutRegionResolver;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class KnockoutRegionReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!Settings.isEnabled(element.getProject())) {
            return PsiReference.EMPTY_ARRAY;
        }
        final List<PsiReference> references = new ArrayList<>();
        addDisplayAreaReferences(element, references);
        addGetRegionReferences(element, references);

        return references.toArray(PsiReference.EMPTY_ARRAY);
    }

    private void addDisplayAreaReferences(
            final @NotNull PsiElement element,
            final @NotNull List<PsiReference> references
    ) {
        final JSProperty property = PsiTreeUtil.getParentOfType(element, JSProperty.class);
        final String displayArea = KnockoutRegionResolver.getInstance().getDisplayArea(property);

        if (displayArea == null) {
            return;
        }
        final List<PsiElement> targets = KnockoutRegionResolver.getInstance()
                .resolveGetRegionTemplateFiles(element.getProject(), displayArea);

        if (targets.isEmpty()) {
            return;
        }
        final int startOffset = element.getText().indexOf(displayArea);

        if (startOffset < 0) {
            return;
        }
        references.add(
                new PolyVariantReferenceBase(
                        element,
                        new TextRange(startOffset, startOffset + displayArea.length()),
                        targets
                )
        );
    }

    private void addGetRegionReferences(
            final @NotNull PsiElement element,
            final @NotNull List<PsiReference> references
    ) {
        for (final KnockoutRegionResolver.RegionMatch regionMatch
                : KnockoutRegionResolver.getInstance().collectGetRegionMatches(element.getText())) {
            final List<PsiElement> targets = KnockoutRegionResolver.getInstance()
                    .resolveDisplayAreaComponentFiles(element.getProject(), regionMatch.getRegionName());

            if (targets.isEmpty()) {
                continue;
            }
            references.add(
                    new PolyVariantReferenceBase(
                            element,
                            new TextRange(regionMatch.getStartOffset(), regionMatch.getEndOffset()),
                            targets
                    )
            );
        }
    }
}
