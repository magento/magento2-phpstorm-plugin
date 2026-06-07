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
import com.magento.idea.magento2plugin.linemarker.js.LineMarkerTargetPresentationUtil;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutRegionResolver;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
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
        String resolvedDisplayArea = property == null ? null : KnockoutRegionResolver.getInstance().getDisplayArea(property);

        if (resolvedDisplayArea == null) {
            resolvedDisplayArea = UiComponentScopeResolver.getInstance().getDisplayAreaValue(element);
        }

        if (resolvedDisplayArea == null) {
            return;
        }
        final String displayArea = resolvedDisplayArea;
        final List<PsiElement> targets = UiComponentScopeResolver.getInstance()
                .resolveGetRegionTargetsForDisplayArea(element, displayArea);

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
        final PsiElement referenceHost = getGetRegionReferenceHost(element);

        if (referenceHost == null) {
            return;
        }
        final List<KnockoutRegionResolver.RegionMatch> regionMatches = KnockoutRegionResolver.getInstance()
                .collectGetRegionMatches(referenceHost.getText());

        for (final KnockoutRegionResolver.RegionMatch regionMatch : regionMatches) {
            final TextRange referenceRange = getRegionReferenceRange(element, referenceHost, regionMatch);

            if (referenceRange == null) {
                continue;
            }
            final List<PsiElement> targets = UiComponentScopeResolver.getInstance()
                    .resolveDisplayAreaTargetsForGetRegion(
                            referenceHost.getContainingFile(),
                            regionMatch.getRegionName()
                    );

            if (targets.isEmpty()) {
                continue;
            }
            final List<PsiElement> navigationTargets = new ArrayList<>(targets);
            navigationTargets.addAll(UiComponentScopeResolver.getInstance()
                    .resolveTemplateFilesForDisplayAreaTargets(
                            referenceHost.getProject(),
                            targets
                    ));
            final List<PsiElement> preparedTargets = LineMarkerTargetPresentationUtil.prepareTargets(navigationTargets);
            references.add(
                    new PolyVariantReferenceBase(
                            element,
                            referenceRange,
                            preparedTargets
                    )
            );
        }
    }

    private PsiElement getGetRegionReferenceHost(final @NotNull PsiElement element) {
        PsiElement current = element;
        final PsiElement containingFile = element.getContainingFile();

        while (current != null && current != containingFile) {
            if (current.getText().contains("getRegion")) {
                return current;
            }
            current = current.getParent();
        }

        return null;
    }

    private TextRange getRegionReferenceRange(
            final @NotNull PsiElement element,
            final @NotNull PsiElement referenceHost,
            final @NotNull KnockoutRegionResolver.RegionMatch regionMatch
    ) {
        if (element == referenceHost) {
            return new TextRange(regionMatch.getCallStartOffset(), regionMatch.getCallEndOffset());
        }
        final int absoluteStart = referenceHost.getTextRange().getStartOffset() + regionMatch.getCallStartOffset();
        final int absoluteEnd = referenceHost.getTextRange().getStartOffset() + regionMatch.getCallEndOffset();
        final int elementStart = element.getTextRange().getStartOffset();
        final int elementEnd = element.getTextRange().getEndOffset();
        final int overlapStart = Math.max(absoluteStart, elementStart);
        final int overlapEnd = Math.min(absoluteEnd, elementEnd);

        if (overlapStart >= overlapEnd) {
            return null;
        }

        return new TextRange(overlapStart - elementStart, overlapEnd - elementStart);
    }
}
