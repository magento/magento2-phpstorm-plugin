/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class MagentoInitPlainTextRequireJsReferenceProvider extends PsiReferenceProvider {
    private static final Pattern DATA_MAGE_INIT_PATTERN = Pattern.compile(
            "data-mage-init\\s*=\\s*(['\"])(.*?)\\1",
            Pattern.DOTALL
    );
    private static final Pattern X_MAGENTO_INIT_PATTERN = Pattern.compile(
            "<script\\b[^>]*\\btype\\s*=\\s*(['\"])text/x-magento-init\\1[^>]*>(.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern JSON_OBJECT_KEY_PATTERN = Pattern.compile("(['\"])([^'\"]+)\\1\\s*:");

    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!Settings.isEnabled(element.getProject()) || !isPhtml(element)) {
            return PsiReference.EMPTY_ARRAY;
        }
        final List<PsiReference> references = new ArrayList<>();
        final String text = element.getText();

        addMagentoInitReferences(element, text, DATA_MAGE_INIT_PATTERN, 2, references);
        addMagentoInitReferences(element, text, X_MAGENTO_INIT_PATTERN, 2, references);

        return references.toArray(PsiReference.EMPTY_ARRAY);
    }

    private void addMagentoInitReferences(
            final @NotNull PsiElement element,
            final @NotNull String text,
            final @NotNull Pattern contextPattern,
            final int contentGroup,
            final @NotNull List<PsiReference> references
    ) {
        final Matcher contextMatcher = contextPattern.matcher(text);

        while (contextMatcher.find()) {
            final String jsonText = contextMatcher.group(contentGroup);
            final int contentOffset = contextMatcher.start(contentGroup);
            final Matcher keyMatcher = JSON_OBJECT_KEY_PATTERN.matcher(jsonText);

            while (keyMatcher.find()) {
                final String requireJsPath = keyMatcher.group(2);
                final List<PsiElement> targets = RequireJsPathResolver.getInstance()
                        .resolveJsFilesOrAlias(element.getProject(), requireJsPath);

                if (targets.isEmpty()) {
                    continue;
                }
                references.add(
                        new PolyVariantReferenceBase(
                                element,
                                new TextRange(
                                        contentOffset + keyMatcher.start(2),
                                        contentOffset + keyMatcher.end(2)
                                ),
                                targets
                        )
                );
                NavigationInstrumentation.infoOnce(
                        "magento-init-plain-text-reference-created-" + requireJsPath,
                        () -> "Magento init plain-text reference created for '" + requireJsPath
                                + "' targets=" + targets.size()
                );
            }
        }
    }

    private boolean isPhtml(final @NotNull PsiElement element) {
        if (element.getContainingFile() == null) {
            return false;
        }
        final VirtualFile virtualFile = element.getContainingFile().getVirtualFile();

        return virtualFile != null && "phtml".equals(virtualFile.getExtension());
    }
}
