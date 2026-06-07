/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PolyVariantReferenceBase extends PsiPolyVariantReferenceBase<PsiElement> {

    /**
     * Target elements.
     */
    private final Collection<? extends PsiElement> targets;

    public PolyVariantReferenceBase(
            final PsiElement element,
            final Collection<? extends PsiElement> targets
    ) {
        super(element);
        this.targets = targets;
    }

    public PolyVariantReferenceBase(
            final PsiElement element,
            final TextRange range,
            final Collection<? extends PsiElement> targets
    ) {
        super(element, range);
        this.targets = targets;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        debugMultiResolve("start");
        ResolveResult[] resolveResults = new ResolveResult[targets.size()];

        int index = 0;
        for (final PsiElement target : targets) {
            resolveResults[index++] = new PsiElementResolveResult(target);//NOPMD
        }
        debugMultiResolve("finish");
        return resolveResults;
    }

    @SuppressWarnings("PMD.AvoidUncheckedExceptionsInSignatures")
    @Override
    public PsiElement bindToElement(
            final @NotNull PsiElement element
    ) throws IncorrectOperationException {
        return null;
    }

    private void debugMultiResolve(final @NotNull String stage) {
        final PsiElement element = getElement();

        if (element == null
                || element.getContainingFile() == null
                || element.getContainingFile().getVirtualFile() == null
                || !element.getContainingFile().getVirtualFile().getPath().contains("shipping-methods")
                || !element.getText().contains("getRegion")) {
            return;
        }
        final List<String> targetDescriptions = new ArrayList<>();

        for (final PsiElement target : targets) {
            if (target == null) {
                targetDescriptions.add("<null>");
                continue;
            }
            targetDescriptions.add(
                    target.getClass().getSimpleName()
                            + " range=" + target.getTextRange()
                            + " text='" + sanitize(target.getText()) + "'"
                            + " file=" + NavigationInstrumentation.describeFile(target.getContainingFile())
            );
        }
        NavigationInstrumentation.info(
                "ko-region-debug resolve stage=" + stage
                        + ", elementClass=" + element.getClass().getName()
                        + ", elementRange=" + element.getTextRange()
                        + ", referenceRange=" + getRangeInElement()
                        + ", elementText='" + sanitize(element.getText()) + "'"
                        + ", targets=" + targetDescriptions
        );
    }

    private @NotNull String sanitize(final String text) {
        final String singleLine = text.replace('\n', ' ').replace('\r', ' ');

        return singleLine.length() > 160 ? singleLine.substring(0, 160) + "..." : singleLine;
    }
}
