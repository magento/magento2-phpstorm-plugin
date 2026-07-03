/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.codeInsight.navigation.PsiTargetNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.xml.LayoutBlockTemplateIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.LayoutTemplateBlockIndex;
import com.magento.idea.magento2plugin.util.magento.BlockTemplateNavigationUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

        final List<PsiElement> preparedTemplates = prepareTargets(
                templates,
                BlockTemplateLineMarkerProvider::getPresentableTemplateTargetName
        );

        return NavigationGutterIconBuilder
                .create(AllIcons.FileTypes.Html)
                .setTargets(preparedTemplates)
                .setNamer(BlockTemplateLineMarkerProvider::getPresentableTemplateTargetName)
                .setTooltipText(TEMPLATE_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        anchor,
                        createNavigationHandler(
                                preparedTemplates,
                                TEMPLATE_TOOLTIP_TEXT,
                                BlockTemplateLineMarkerProvider::getPresentableTemplateTargetName
                        )
                );
    }

    private @Nullable LineMarkerInfo<?> createTemplateLineMarker(
            final @NotNull PsiElement anchor,
            final @NotNull PsiFile psiFile
    ) {
        final List<PsiElement> blocks = collectBlocks(psiFile);

        if (blocks.isEmpty()) {
            return null;
        }

        final List<PsiElement> preparedBlocks = prepareTargets(
                blocks,
                BlockTemplateLineMarkerProvider::getPresentableBlockTargetName
        );

        return NavigationGutterIconBuilder
                .create(AllIcons.Nodes.Class)
                .setTargets(preparedBlocks)
                .setNamer(BlockTemplateLineMarkerProvider::getPresentableBlockTargetName)
                .setTooltipText(BLOCK_TOOLTIP_TEXT)
                .createLineMarkerInfo(
                        anchor,
                        createNavigationHandler(
                                preparedBlocks,
                                BLOCK_TOOLTIP_TEXT,
                                BlockTemplateLineMarkerProvider::getPresentableBlockTargetName
                        )
                );
    }

    @NotNull List<PsiElement> collectTemplates(final @NotNull PhpClass phpClass) {
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

    @NotNull List<PsiElement> collectBlocks(final @NotNull PsiFile psiFile) {
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

    static @NotNull String getPresentableBlockTargetName(final @NotNull PsiElement target) {
        if (target instanceof PhpClass) {
            return ((PhpClass) target).getPresentableFQN();
        }

        return target.getText();
    }

    static @NotNull String getPresentableTemplateTargetName(final @NotNull PsiElement target) {
        final PsiFile psiFile = target instanceof PsiFile
                ? (PsiFile) target
                : target.getContainingFile();

        if (psiFile == null) {
            return target.getText();
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null) {
            return psiFile.getName();
        }
        final String templatePath = getTemplatePath(psiFile);

        if (templatePath == null) {
            return virtualFile.getName();
        }

        return templatePath;
    }

    private static @NotNull GutterIconNavigationHandler<PsiElement> createNavigationHandler(
            final @NotNull List<PsiElement> targets,
            final @NotNull String title,
            final @NotNull Function<PsiElement, String> namer
    ) {
        return (event, element) -> {
            if (targets.isEmpty()) {
                return;
            }
            if (targets.size() == 1) {
                NavigationUtil.activateFileWithPsiElement(targets.get(0), true);
                return;
            }
            final Project project = element.getProject();
            final JBPopup popup = new PsiTargetNavigator<>(targets)
                    .presentationProvider(target -> TargetPresentation
                            .builder(namer.apply(target))
                            .icon(target.getIcon(Iconable.ICON_FLAG_VISIBILITY))
                            .presentation())
                    .title(title)
                    .createPopup(project, title);

            if (event != null && event.getComponent().isShowing()) {
                popup.show(new RelativePoint(event));
            } else {
                popup.showCenteredInCurrentWindow(project);
            }
        };
    }

    private static @NotNull List<PsiElement> prepareTargets(
            final @NotNull List<PsiElement> targets,
            final @NotNull Function<PsiElement, String> namer
    ) {
        final Map<String, PsiElement> uniqueTargets = new LinkedHashMap<>();

        for (final PsiElement target : targets) {
            uniqueTargets.putIfAbsent(getStableKey(target), target);
        }
        final List<PsiElement> sortedTargets = new ArrayList<>(uniqueTargets.values());
        sortedTargets.sort(Comparator
                .comparing(namer)
                .thenComparing(BlockTemplateLineMarkerProvider::getStableKey));

        return sortedTargets;
    }

    private static @NotNull String getStableKey(final @NotNull PsiElement target) {
        final PsiFile psiFile = target instanceof PsiFile
                ? (PsiFile) target
                : target.getContainingFile();
        final VirtualFile virtualFile = psiFile == null ? null : psiFile.getVirtualFile();

        if (virtualFile != null) {
            return virtualFile.getPath() + ":" + target.getTextRange();
        }

        return target.getTextRange().toString() + ":" + target.getText();
    }

    private static @Nullable String getTemplatePath(final @NotNull PsiFile psiFile) {
        final Set<String> templatePaths = BlockTemplateNavigationUtil.getInstance().getTemplatePaths(psiFile);

        return templatePaths.isEmpty() ? null : templatePaths.iterator().next();
    }

    private boolean isPhtmlFile(final @NotNull PsiFile psiFile) {
        return psiFile.getVirtualFile() != null
                && "phtml".equals(psiFile.getVirtualFile().getExtension());
    }
}
