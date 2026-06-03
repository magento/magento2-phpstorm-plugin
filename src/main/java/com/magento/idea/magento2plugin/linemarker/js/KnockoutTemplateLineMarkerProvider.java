/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.KnockoutTemplateIndex;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockoutTemplateLineMarkerProvider implements LineMarkerProvider {
    private static final String TEMPLATE_TOOLTIP_TEXT = "Navigate to Knockout template";
    private static final String COMPONENT_TOOLTIP_TEXT = "Navigate to Knockout component";

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement psiElement) {
        final PsiFile psiFile = psiElement.getContainingFile();

        if (!Settings.isEnabled(psiElement.getProject()) || !isSupportedFile(psiFile)) {
            return null;
        }
        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(psiFile);

        if (!psiElement.equals(anchor)) {
            return null;
        }

        return createLineMarker(anchor, psiFile);
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

            if (!isSupportedFile(psiFile) || processedFiles.contains(psiFile)) {
                continue;
            }
            processedFiles.add(psiFile);

            final PsiElement anchor = PsiTreeUtil.getDeepestFirst(psiFile);
            final LineMarkerInfo<?> lineMarker = createLineMarker(anchor, psiFile);

            if (lineMarker != null) {
                collection.add(lineMarker);
            }
        }
    }

    private @Nullable LineMarkerInfo<?> createLineMarker(
            final @NotNull PsiElement anchor,
            final @NotNull PsiFile psiFile
    ) {
        if (psiFile instanceof JSFile) {
            final List<PsiElement> templates = collectTemplates((JSFile) psiFile);

            if (templates.isEmpty()) {
                return null;
            }

            return NavigationGutterIconBuilder
                    .create(HtmlFileType.INSTANCE.getIcon())
                    .setTargets(templates)
                    .setTooltipText(TEMPLATE_TOOLTIP_TEXT)
                    .createLineMarkerInfo(anchor);
        }
        final List<PsiElement> components = collectComponents(psiFile);

        if (components.isEmpty()) {
            return null;
        }

        return NavigationGutterIconBuilder
                .create(JavaScriptFileType.INSTANCE.getIcon())
                .setTargets(components)
                .setTooltipText(COMPONENT_TOOLTIP_TEXT)
                .createLineMarkerInfo(anchor);
    }

    private @NotNull List<PsiElement> collectTemplates(final @NotNull JSFile jsFile) {
        final List<PsiElement> results = new ArrayList<>();

        for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                .collectTemplatePaths(jsFile)) {
            results.addAll(KnockoutTemplatePathResolver.getInstance()
                    .resolveTemplateFiles(jsFile.getProject(), templatePath));
        }

        return results;
    }

    private @NotNull List<PsiElement> collectComponents(final @NotNull PsiFile psiFile) {
        final List<PsiElement> results = new ArrayList<>();
        final Project project = psiFile.getProject();

        for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                .getTemplateRequireJsPaths(psiFile)) {
            final Collection<Set<String>> componentPathSets = FileBasedIndex.getInstance()
                    .getValues(
                            KnockoutTemplateIndex.KEY,
                            templatePath,
                            GlobalSearchScope.allScope(project)
                    );

            for (final Set<String> componentPaths : componentPathSets) {
                for (final String componentUrl : componentPaths) {
                    addComponentIfFound(project, results, componentUrl);
                }
            }
        }

        return results;
    }

    private void addComponentIfFound(
            final @NotNull Project project,
            final @NotNull Collection<PsiElement> results,
            final @NotNull String componentUrl
    ) {
        final VirtualFile file = VirtualFileManager.getInstance()
                .findFileByUrl(componentUrl);

        if (file == null || file.isDirectory()) {
            return;
        }
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        if (psiFile != null) {
            results.add(psiFile);
        }
    }

    private boolean isSupportedFile(final @NotNull PsiFile psiFile) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        return psiFile instanceof JSFile
                || (virtualFile != null && "html".equals(virtualFile.getExtension()));
    }
}
