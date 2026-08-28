/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlComment;
import com.magento.idea.magento2plugin.linemarker.js.LineMarkerTargetPresentationUtil;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutRegionResolver;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class KnockoutRegionGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(
            final @Nullable PsiElement sourceElement,
            final int offset,
            final Editor editor
    ) {
        if (sourceElement == null || !Settings.isEnabled(sourceElement.getProject())) {
            return null;
        }
        final PsiElement referenceHost = getGetRegionReferenceHost(sourceElement);

        if (referenceHost == null || referenceHost.getContainingFile() == null) {
            return null;
        }
        final List<PsiElement> targets = resolveTargets(referenceHost, offset);

        if (targets.isEmpty()) {
            return null;
        }

        return targets.toArray(PsiElement.EMPTY_ARRAY);
    }

    @Override
    public @Nullable String getActionText(final DataContext context) {
        return null;
    }

    private @Nullable PsiElement getGetRegionReferenceHost(final PsiElement element) {
        PsiElement current = element;
        final PsiFile containingFile = element.getContainingFile();

        while (current != null && current != containingFile) {
            if (isGetRegionReferenceHost(current)
                    && current.getText().contains("getRegion")) {
                return current;
            }
            current = current.getParent();
        }

        return null;
    }

    private boolean isGetRegionReferenceHost(final PsiElement element) {
        return element instanceof XmlComment
                || element instanceof XmlAttributeValue
                || element.getLanguage().isKindOf(JavascriptLanguage.INSTANCE);
    }

    private List<PsiElement> resolveTargets(
            final PsiElement referenceHost,
            final int offset
    ) {
        final Set<PsiElement> targets = new LinkedHashSet<>();

        for (final KnockoutRegionResolver.RegionMatch regionMatch : KnockoutRegionResolver.getInstance()
                .collectGetRegionMatches(referenceHost.getText())) {
            if (!containsOffset(referenceHost, regionMatch, offset)) {
                continue;
            }
            final List<PsiElement> displayAreaTargets = UiComponentScopeResolver.getInstance()
                    .resolveDisplayAreaTargetsForGetRegion(
                            referenceHost.getContainingFile(),
                            regionMatch.getRegionName()
                    );

            targets.addAll(displayAreaTargets);
            targets.addAll(UiComponentScopeResolver.getInstance().resolveTemplateFilesForDisplayAreaTargets(
                    referenceHost.getProject(),
                    displayAreaTargets
            ));
        }

        return LineMarkerTargetPresentationUtil.prepareTargets(new ArrayList<>(targets));
    }

    private boolean containsOffset(
            final PsiElement referenceHost,
            final KnockoutRegionResolver.RegionMatch regionMatch,
            final int offset
    ) {
        final int hostStartOffset = referenceHost.getTextRange().getStartOffset();
        final int callStartOffset = hostStartOffset + regionMatch.getCallStartOffset();
        final int callEndOffset = hostStartOffset + regionMatch.getCallEndOffset();

        return offset >= callStartOffset && offset <= callEndOffset;
    }
}
