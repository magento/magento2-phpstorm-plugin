/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpFile;
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
        final List<PhpFile> results = new ArrayList();
        for (final PsiElement psiElement: elements) {
            if (psiElement instanceof PhpDocTag) {
                @NotNull final String tagName = ((PhpDocTag) psiElement).getName();
                if (!tagName.equals(TestFixture.PHP_DOC_TAG_NAME)
                        && !tagName.equals(TestFixture.PHP_DOC_TAG_NAME_API)) {
                    continue;
                }
                @NotNull final String tagValue = ((PhpDocTag) psiElement).getTagValue();
                if (tagValue.isEmpty()) {
                    continue;
                }

                final List<PhpFile> dataFixtures = fixtureIndex.getDataFixtures(tagValue);

                if (dataFixtures.isEmpty()) {
                    continue;
                }

                results.addAll(dataFixtures);

                final String tooltipText = "Navigate to fixtures";
                final NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                        .create(PhpIcons.PHP_FILE)
                        .setTargets(results)
                        .setTooltipText(tooltipText);

                result.add(builder.createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(psiElement)));
            }
        }
    }
}
