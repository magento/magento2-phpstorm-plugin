/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Method;
import com.magento.idea.magento2plugin.indexes.FixtureIndex;
import com.magento.idea.magento2plugin.magento.files.TestFixture;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestFixtureLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> elements,
            final @NotNull Collection<? super LineMarkerInfo<?>> result
    ) {
        if (elements.isEmpty() || !Settings.isEnabled(elements.get(0).getProject())) {
            return;
        }
        final FixtureIndex fixtureIndex = new FixtureIndex(elements.get(0).getProject());
        for (final PsiElement psiElement: elements) {
            if (!(psiElement instanceof Method)) {
                continue;
            }
            final PhpDocComment phpDocComment = ((Method) psiElement).getDocComment();
            if (phpDocComment == null) {
                continue;
            }
            final List<PsiFile> dataFixtures = collectDataFixtures(phpDocComment, fixtureIndex);
            if (dataFixtures.isEmpty()) {
                continue;
            }

            final String tooltipText = "Navigate to fixtures";
            final NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                    .create(PhpIcons.PHP_FILE)
                    .setTargets(dataFixtures)
                    .setTooltipText(tooltipText);

            result.add(builder.createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement)));
        }
    }

    private @NotNull List<PsiFile> collectDataFixtures(
            final @NotNull PhpDocComment phpDocComment,
            final @NotNull FixtureIndex fixtureIndex
    ) {
        final List<PsiFile> dataFixtures = new ArrayList<>();
        for (final PhpDocTag phpDocTag : PsiTreeUtil.getChildrenOfTypeAsList(
                phpDocComment,
                PhpDocTag.class
        )) {
            @NotNull final String tagName = normalizeTagName(phpDocTag);
            if (!isFixtureTagName(tagName)) {
                continue;
            }
            @NotNull final String tagValue = phpDocTag.getTagValue().trim();
            if (tagValue.isEmpty()) {
                continue;
            }
            dataFixtures.addAll(fixtureIndex.getDataFixtures(tagValue));
        }

        return dataFixtures;
    }

    private boolean isFixtureTagName(final @NotNull String tagName) {
        return tagName.equals(stripTagPrefix(TestFixture.PHP_DOC_TAG_NAME))
                || tagName.equals(stripTagPrefix(TestFixture.PHP_DOC_TAG_NAME_API));
    }

    private @NotNull String normalizeTagName(final @NotNull PhpDocTag phpDocTag) {
        return stripTagPrefix(phpDocTag.getName().trim());
    }

    private @NotNull String stripTagPrefix(final @NotNull String tagName) {
        return tagName.startsWith("@") ? tagName.substring(1) : tagName;
    }
}
