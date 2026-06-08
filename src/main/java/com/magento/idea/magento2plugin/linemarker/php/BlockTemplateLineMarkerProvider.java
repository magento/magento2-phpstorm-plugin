/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.xml.LayoutBlockTemplateIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.LayoutTemplateBlockIndex;
import com.magento.idea.magento2plugin.util.magento.BlockTemplateNavigationUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockTemplateLineMarkerProvider implements LineMarkerProvider {
    private static final String TEMPLATE_TOOLTIP_TEXT = "Navigate to block template";
    private static final String BLOCK_TOOLTIP_TEXT = "Navigate to template block";

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        if (!Settings.isEnabled(psiElement.getProject())) {
            return null;
        }

        if (psiElement instanceof PhpClass) {
            return createBlockLineMarker((PhpClass) psiElement);
        }
        final PsiFile psiFile = psiElement.getContainingFile();

        if (!isPhtmlFile(psiFile) || !psiElement.equals(PsiTreeUtil.getDeepestFirst(psiFile))) {
            return null;
        }

        return createTemplateLineMarker(psiElement, psiFile);
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (psiElements.isEmpty() || !Settings.isEnabled(psiElements.get(0).getProject())) {
            return;
        }
        final Set<PsiFile> processedTemplates = new HashSet<>();

        for (final PsiElement psiElement : psiElements) {
            if (psiElement instanceof PhpClass) {
                final LineMarkerInfo<?> lineMarker = createBlockLineMarker((PhpClass) psiElement);

                if (lineMarker != null) {
                    collection.add(lineMarker);
                }
                continue;
            }
            final PsiFile psiFile = psiElement.getContainingFile();

            if (!isPhtmlFile(psiFile) || processedTemplates.contains(psiFile)) {
                continue;
            }
            processedTemplates.add(psiFile);

            final LineMarkerInfo<?> lineMarker = createTemplateLineMarker(
                    PsiTreeUtil.getDeepestFirst(psiFile),
                    psiFile
            );

            if (lineMarker != null) {
                collection.add(lineMarker);
            }
        }
    }

    private @Nullable LineMarkerInfo<?> createBlockLineMarker(final @NotNull PhpClass phpClass) {
        final List<PsiElement> templates = collectTemplates(phpClass);

        if (templates.isEmpty()) {
            return null;
        }
        final PsiElement anchor = phpClass.getNameIdentifier() == null
                ? PsiTreeUtil.getDeepestFirst(phpClass)
                : phpClass.getNameIdentifier();

        return NavigationGutterIconBuilder
                .create(AllIcons.FileTypes.Html)
                .setTargets(templates)
                .setTooltipText(TEMPLATE_TOOLTIP_TEXT)
                .createLineMarkerInfo(anchor);
    }

    private @Nullable LineMarkerInfo<?> createTemplateLineMarker(
            final @NotNull PsiElement anchor,
            final @NotNull PsiFile psiFile
    ) {
        final List<PsiElement> blocks = collectBlocks(psiFile);

        if (blocks.isEmpty()) {
            return null;
        }

        return NavigationGutterIconBuilder
                .create(AllIcons.Nodes.Class)
                .setTargets(blocks)
                .setTooltipText(BLOCK_TOOLTIP_TEXT)
                .createLineMarkerInfo(anchor);
    }

    private @NotNull List<PsiElement> collectTemplates(final @NotNull PhpClass phpClass) {
        final List<PsiElement> results = new ArrayList<>();
        final Collection<Set<String>> templateSets = FileBasedIndex.getInstance()
                .getValues(
                        LayoutBlockTemplateIndex.KEY,
                        phpClass.getPresentableFQN(),
                        GlobalSearchScope.allScope(phpClass.getProject())
                );

        for (final Set<String> templates : templateSets) {
            for (final String template : templates) {
                results.addAll(BlockTemplateNavigationUtil.getInstance()
                        .resolveTemplateFiles(phpClass.getProject(), template));
            }
        }

        return results;
    }

    private @NotNull List<PsiElement> collectBlocks(final @NotNull PsiFile psiFile) {
        final List<PsiElement> results = new ArrayList<>();
        final Project project = psiFile.getProject();
        final PhpIndex phpIndex = PhpIndex.getInstance(project);

        for (final String templatePath : BlockTemplateNavigationUtil.getInstance().getTemplatePaths(psiFile)) {
            final Collection<Set<String>> blockSets = FileBasedIndex.getInstance()
                    .getValues(
                            LayoutTemplateBlockIndex.KEY,
                            templatePath,
                            GlobalSearchScope.allScope(project)
                    );

            for (final Set<String> blockClasses : blockSets) {
                for (final String blockClass : blockClasses) {
                    results.addAll(phpIndex.getClassesByFQN(blockClass));
                }
            }
        }

        return results;
    }

    private boolean isPhtmlFile(final @NotNull PsiFile psiFile) {
        return psiFile.getVirtualFile() != null
                && "phtml".equals(psiFile.getVirtualFile().getExtension());
    }
}
