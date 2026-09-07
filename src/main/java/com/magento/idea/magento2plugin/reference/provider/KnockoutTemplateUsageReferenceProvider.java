/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockoutTemplateUsageReferenceProvider extends PsiReferenceProvider {
    private static final Pattern CHILD_TEMPLATE_PATTERN = Pattern.compile(
            "template\\s*:\\s*(childTemplate)"
    );
    private static final Pattern GET_TEMPLATE_PATTERN = Pattern.compile(
            "template\\s*:\\s*(getTemplate)\\s*\\(\\s*\\)"
    );
    private static final Pattern NAMED_TEMPLATE_PATTERN = Pattern.compile(
            "getTemplate\\s*\\(\\s*(['\\\"])([^'\\\"]+)\\1\\s*\\)"
    );

    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!Settings.isEnabled(element.getProject())) {
            return PsiReference.EMPTY_ARRAY;
        }
        final PsiElement referenceHost = getTemplateReferenceHost(element);

        if (referenceHost == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        final List<PsiReference> references = new ArrayList<>();

        addChildTemplateReferences(referenceHost, references);
        addTemplateReferences(referenceHost, references);
        addNamedTemplateReferences(referenceHost, references);

        return references.toArray(PsiReference.EMPTY_ARRAY);
    }

    private void addChildTemplateReferences(
            final @NotNull PsiElement referenceHost,
            final @NotNull List<PsiReference> references
    ) {
        final Matcher matcher = CHILD_TEMPLATE_PATTERN.matcher(referenceHost.getText());

        while (matcher.find()) {
            addReference(
                    referenceHost,
                    references,
                    UiComponentNavigationData.KIND_CHILD_TEMPLATE,
                    null,
                    matcher.start(1),
                    matcher.end(1)
            );
        }
    }

    private void addTemplateReferences(
            final @NotNull PsiElement referenceHost,
            final @NotNull List<PsiReference> references
    ) {
        final Matcher matcher = GET_TEMPLATE_PATTERN.matcher(referenceHost.getText());

        while (matcher.find()) {
            addReference(
                    referenceHost,
                    references,
                    UiComponentNavigationData.KIND_TEMPLATE,
                    null,
                    matcher.start(1),
                    matcher.end(1)
            );
        }
    }

    private void addNamedTemplateReferences(
            final @NotNull PsiElement referenceHost,
            final @NotNull List<PsiReference> references
    ) {
        final Matcher matcher = NAMED_TEMPLATE_PATTERN.matcher(referenceHost.getText());

        while (matcher.find()) {
            addReference(
                    referenceHost,
                    references,
                    UiComponentNavigationData.KIND_TEMPLATES,
                    matcher.group(2),
                    matcher.start(2),
                    matcher.end(2)
            );
        }
    }

    private void addReference(
            final @NotNull PsiElement referenceHost,
            final @NotNull List<PsiReference> references,
            final @NotNull String declarationKind,
            final @Nullable String templateKey,
            final int startOffset,
            final int endOffset
    ) {
        final List<PsiElement> targets = UiComponentScopeResolver.getInstance()
                .resolveTemplateUsageDeclarations(
                        referenceHost.getContainingFile(),
                        declarationKind,
                        templateKey
                );

        if (targets.isEmpty()) {
            return;
        }
        references.add(new PolyVariantReferenceBase(
                referenceHost,
                new TextRange(startOffset, endOffset),
                targets
        ));
    }

    private @Nullable PsiElement getTemplateReferenceHost(final @NotNull PsiElement element) {
        PsiElement current = element;
        final PsiElement containingFile = element.getContainingFile();

        while (current != null && current != containingFile) {
            final String text = current.getText();

            if (text.contains("template:") || text.contains("getTemplate") || text.contains("childTemplate")) {
                return current;
            }
            current = current.getParent();
        }

        return null;
    }
}
