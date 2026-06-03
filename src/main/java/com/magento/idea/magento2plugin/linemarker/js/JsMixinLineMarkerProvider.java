/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.JsMixinIndex;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsPathResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsMixinLineMarkerProvider implements LineMarkerProvider {
    private static final String TARGET_TOOLTIP_TEXT = "Navigate to target JS";
    private static final String MIXINS_TOOLTIP_TEXT = "Navigate to JS mixins";

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        final PsiFile psiFile = psiElement.getContainingFile();

        if (!(psiFile instanceof JSFile) || !Settings.isEnabled(psiElement.getProject())) {
            return null;
        }

        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(psiFile);

        if (!psiElement.equals(anchor)) {
            return null;
        }

        final String requireJsPath = RequireJsPathResolver.getInstance().getRequireJsPath(psiFile);

        if (requireJsPath == null) {
            return null;
        }

        final List<PsiElement> targets = collectTargetsForMixin(
                psiElement.getProject(),
                requireJsPath
        );

        if (!targets.isEmpty()) {
            return NavigationGutterIconBuilder
                    .create(JavaScriptFileType.INSTANCE.getIcon())
                    .setTargets(targets)
                    .setTooltipText(TARGET_TOOLTIP_TEXT)
                    .createLineMarkerInfo(anchor);
        }

        final List<PsiElement> mixins = collectMixinsForTarget(
                psiElement.getProject(),
                requireJsPath
        );

        if (!mixins.isEmpty()) {
            return NavigationGutterIconBuilder
                    .create(AllIcons.Nodes.Plugin)
                    .setTargets(mixins)
                    .setTooltipText(MIXINS_TOOLTIP_TEXT)
                    .createLineMarkerInfo(anchor);
        }

        return null;
    }

    @Override
    public void collectSlowLineMarkers(
            final @NotNull List<? extends PsiElement> psiElements,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        if (psiElements.isEmpty() || !Settings.isEnabled(psiElements.get(0).getProject())) {
            return;
        }

        final Set<PsiFile> processedFiles = new HashSet<>();

        for (final PsiElement psiElement : psiElements) {
            final PsiFile psiFile = psiElement.getContainingFile();

            if (!(psiFile instanceof JSFile) || processedFiles.contains(psiFile)) {
                continue;
            }
            processedFiles.add(psiFile);

            final String requireJsPath = RequireJsPathResolver.getInstance().getRequireJsPath(psiFile);

            if (requireJsPath == null) {
                continue;
            }

            addTargetLineMarker(psiElement, requireJsPath, collection);
            addMixinLineMarker(psiElement, requireJsPath, collection);
        }
    }

    private void addTargetLineMarker(
            final @NotNull PsiElement anchorContext,
            final @NotNull String requireJsPath,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> targets = collectTargetsForMixin(
                anchorContext.getProject(),
                requireJsPath
        );

        if (targets.isEmpty()) {
            return;
        }

        collection.add(NavigationGutterIconBuilder
                .create(JavaScriptFileType.INSTANCE.getIcon())
                .setTargets(targets)
                .setTooltipText(TARGET_TOOLTIP_TEXT)
                .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(anchorContext.getContainingFile())));
    }

    private void addMixinLineMarker(
            final @NotNull PsiElement anchorContext,
            final @NotNull String requireJsPath,
            final @NotNull Collection<? super LineMarkerInfo<?>> collection
    ) {
        final List<PsiElement> mixins = collectMixinsForTarget(
                anchorContext.getProject(),
                requireJsPath
        );

        if (mixins.isEmpty()) {
            return;
        }

        collection.add(NavigationGutterIconBuilder
                .create(AllIcons.Nodes.Plugin)
                .setTargets(mixins)
                .setTooltipText(MIXINS_TOOLTIP_TEXT)
                .createLineMarkerInfo(PsiTreeUtil.getDeepestFirst(anchorContext.getContainingFile())));
    }

    private @NotNull List<PsiElement> collectMixinsForTarget(
            final @NotNull Project project,
            final @NotNull String targetPath
    ) {
        final List<PsiElement> results = new ArrayList<>();
        final Collection<Set<String>> mixinSets = FileBasedIndex.getInstance()
                .getValues(JsMixinIndex.KEY, targetPath, GlobalSearchScope.allScope(project));

        for (final Set<String> mixins : mixinSets) {
            for (final String mixin : mixins) {
                results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(project, mixin));
            }
        }

        return results;
    }

    private @NotNull List<PsiElement> collectTargetsForMixin(
            final @NotNull Project project,
            final @NotNull String mixinPath
    ) {
        final List<PsiElement> results = new ArrayList<>();
        final Collection<String> targetPaths = FileBasedIndex.getInstance()
                .getAllKeys(JsMixinIndex.KEY, project);

        for (final String targetPath : targetPaths) {
            final Collection<Set<String>> mixinSets = FileBasedIndex.getInstance()
                    .getValues(JsMixinIndex.KEY, targetPath, GlobalSearchScope.allScope(project));

            for (final Set<String> mixins : mixinSets) {
                if (mixins.contains(mixinPath)) {
                    results.addAll(RequireJsPathResolver.getInstance().resolveJsFiles(project, targetPath));
                }
            }
        }

        return results;
    }
}
