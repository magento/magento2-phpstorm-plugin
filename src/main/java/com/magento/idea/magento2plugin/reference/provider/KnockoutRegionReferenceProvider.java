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
            NavigationInstrumentation.infoOnce(
                    "ko-region-reference-disabled-" + element.getProject().getLocationHash(),
                    () -> "Knockout region references skipped: Magento support disabled; "
                            + NavigationInstrumentation.describeSettings(element.getProject())
            );
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
        NavigationInstrumentation.infoOnce(
                "ko-display-area-reference-seen-" + displayArea,
                () -> "displayArea reference candidate '" + displayArea + "' in "
                        + NavigationInstrumentation.describeElement(element)
        );
        final List<PsiElement> targets = UiComponentScopeResolver.getInstance()
                .resolveGetRegionTargetsForDisplayArea(element, displayArea);

        if (targets.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "ko-display-area-reference-empty-" + displayArea,
                    () -> "displayArea reference has no getRegion template targets for '" + displayArea + "'"
            );
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
        NavigationInstrumentation.infoOnce(
                "ko-display-area-reference-created-" + displayArea,
                () -> "displayArea reference created for '" + displayArea + "' targets=" + targets.size()
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
        for (final KnockoutRegionResolver.RegionMatch regionMatch
                : KnockoutRegionResolver.getInstance().collectGetRegionMatches(referenceHost.getText())) {
            final List<PsiElement> targets = UiComponentScopeResolver.getInstance()
                    .resolveDisplayAreaTargetsForGetRegion(
                            referenceHost.getContainingFile(),
                            regionMatch.getRegionName()
                    );

            if (targets.isEmpty()) {
                NavigationInstrumentation.infoOnce(
                        "ko-get-region-reference-empty-" + regionMatch.getRegionName(),
                        () -> "getRegion reference has no displayArea component targets for '"
                                + regionMatch.getRegionName() + "' in "
                                + NavigationInstrumentation.describeElement(referenceHost)
                );
                continue;
            }
            references.add(
                    new PolyVariantReferenceBase(
                            referenceHost,
                            new TextRange(regionMatch.getStartOffset(), regionMatch.getEndOffset()),
                            targets
                    )
            );
            NavigationInstrumentation.infoOnce(
                    "ko-get-region-reference-created-" + regionMatch.getRegionName(),
                    () -> "getRegion reference created for '" + regionMatch.getRegionName()
                            + "' targets=" + targets.size()
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
}
