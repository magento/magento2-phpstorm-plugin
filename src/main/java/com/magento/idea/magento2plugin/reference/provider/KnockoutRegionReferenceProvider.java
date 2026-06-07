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

        if (!references.isEmpty() || element.getText().contains("getRegion")) {
            debugGetRegionClick("exit", element, "references=" + references.size());
        }

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
            if (element.getText().contains("getRegion")) {
                debugGetRegionClick("no-host", element, null);
            }
            return;
        }
        final List<KnockoutRegionResolver.RegionMatch> regionMatches = KnockoutRegionResolver.getInstance()
                .collectGetRegionMatches(referenceHost.getText());
        debugGetRegionClick(
                "host",
                element,
                "host=" + referenceHost.getClass().getSimpleName()
                        + ", hostRange=" + referenceHost.getTextRange()
                        + ", matches=" + regionMatches.size()
        );

        for (final KnockoutRegionResolver.RegionMatch regionMatch : regionMatches) {
            final TextRange referenceRange = getRegionReferenceRange(element, referenceHost, regionMatch);

            if (referenceRange == null) {
                debugGetRegionClick(
                        "range-miss",
                        element,
                        "region=" + regionMatch.getRegionName()
                                + ", callOffsets=" + regionMatch.getCallStartOffset()
                                + "-" + regionMatch.getCallEndOffset()
                );
                continue;
            }
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
            final List<PsiElement> navigationTargets = new ArrayList<>(targets);
            navigationTargets.addAll(UiComponentScopeResolver.getInstance()
                    .resolveTemplateFilesForDisplayAreaTargets(
                            referenceHost.getProject(),
                            targets
                    ));
            final List<PsiElement> preparedTargets = LineMarkerTargetPresentationUtil.prepareTargets(navigationTargets);
            debugGetRegionClick(
                    "create-reference",
                    element,
                    "region=" + regionMatch.getRegionName()
                            + ", range=" + referenceRange
                            + ", displayAreaTargets=" + targets.size()
                            + ", targets=" + preparedTargets.size()
                            + ", targetLabels=" + describeTargetLabels(preparedTargets)
                            + ", targetFiles=" + describeTargetFiles(preparedTargets)
            );
            references.add(
                    new PolyVariantReferenceBase(
                            element,
                            referenceRange,
                            preparedTargets
                    )
            );
            NavigationInstrumentation.infoOnce(
                    "ko-get-region-reference-created-" + regionMatch.getRegionName(),
                    () -> "getRegion reference created for '" + regionMatch.getRegionName()
                            + "' targets=" + preparedTargets.size()
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

    private void debugGetRegionClick(
            final @NotNull String stage,
            final @NotNull PsiElement element,
            final String details
    ) {
        if (element.getContainingFile() == null
                || element.getContainingFile().getVirtualFile() == null
                || !element.getContainingFile().getVirtualFile().getPath().contains("shipping-methods")) {
            return;
        }
        NavigationInstrumentation.info(
                "ko-region-debug click stage=" + stage
                        + ", elementClass=" + element.getClass().getName()
                        + ", elementRange=" + element.getTextRange()
                        + ", elementText='" + sanitize(element.getText()) + "'"
                        + ", file=" + NavigationInstrumentation.describeFile(element.getContainingFile())
                        + (details == null ? "" : ", " + details)
        );
    }

    private @NotNull String describeTargetFiles(final @NotNull List<PsiElement> targets) {
        final List<String> result = new ArrayList<>();

        for (final PsiElement target : targets) {
            if (target == null || target.getContainingFile() == null
                    || target.getContainingFile().getVirtualFile() == null) {
                result.add("<unknown>");
                continue;
            }
            result.add(target.getContainingFile().getVirtualFile().getPath());
        }

        return result.toString();
    }

    private @NotNull String describeTargetLabels(final @NotNull List<PsiElement> targets) {
        final List<String> result = new ArrayList<>();

        for (final PsiElement target : targets) {
            result.add(
                    target.getClass().getSimpleName()
                            + ": "
                            + LineMarkerTargetPresentationUtil.getPresentableTargetName(target)
            );
        }

        return result.toString();
    }

    private @NotNull String sanitize(final String text) {
        final String singleLine = text.replace('\n', ' ').replace('\r', ' ');

        return singleLine.length() > 160 ? singleLine.substring(0, 160) + "..." : singleLine;
    }
}
