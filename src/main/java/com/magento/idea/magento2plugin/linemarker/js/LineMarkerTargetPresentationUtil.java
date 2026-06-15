/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.js;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.codeInsight.navigation.PsiTargetNavigator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LineMarkerTargetPresentationUtil {
    private LineMarkerTargetPresentationUtil() {
    }

    static @NotNull GutterIconNavigationHandler<PsiElement> createNavigationHandler(
            final @NotNull List<PsiElement> targets,
            final @NotNull String title
    ) {
        final List<PsiElement> preparedTargets = prepareTargets(targets);

        return (event, element) -> {
            if (preparedTargets.isEmpty()) {
                return;
            }
            if (preparedTargets.size() == 1) {
                NavigationUtil.activateFileWithPsiElement(preparedTargets.get(0), true);
                return;
            }
            final Project project = element.getProject();
            final JBPopup popup = new PsiTargetNavigator<>(preparedTargets)
                    .presentationProvider(LineMarkerTargetPresentationUtil::getTargetPresentation)
                    .title(title)
                    .createPopup(project, title);

            if (event != null && event.getComponent().isShowing()) {
                popup.show(new RelativePoint(event));
            } else {
                popup.showCenteredInCurrentWindow(project);
            }
        };
    }

    public static @NotNull List<PsiElement> prepareTargets(final @NotNull List<PsiElement> targets) {
        final Map<String, PsiElement> uniqueTargets = new LinkedHashMap<>();

        for (final PsiElement target : targets) {
            uniqueTargets.putIfAbsent(getStableKey(target), target);
        }
        final List<PsiElement> sortedTargets = new ArrayList<>(uniqueTargets.values());
        sortedTargets.sort(Comparator
                .comparingInt(LineMarkerTargetPresentationUtil::getPriority)
                .thenComparing(LineMarkerTargetPresentationUtil::getPresentableTargetName)
                .thenComparing(LineMarkerTargetPresentationUtil::getStableKey));

        return sortedTargets;
    }

    public static @NotNull List<PsiElement> prepareJsFileTargets(final @NotNull List<PsiElement> targets) {
        final List<PsiElement> normalizedTargets = new ArrayList<>();

        for (final PsiElement target : targets) {
            normalizedTargets.add(normalizeJsFileTarget(target));
        }

        return prepareTargets(normalizedTargets);
    }

    private static @NotNull PsiElement normalizeJsFileTarget(final @NotNull PsiElement target) {
        final PsiFile psiFile = target instanceof PsiFile
                ? (PsiFile) target
                : target.getContainingFile();
        final VirtualFile virtualFile = psiFile == null ? null : psiFile.getVirtualFile();

        if (virtualFile != null && "js".equals(virtualFile.getExtension())) {
            return psiFile;
        }

        return target;
    }

    private static @NotNull TargetPresentation getTargetPresentation(final @NotNull PsiElement target) {
        return TargetPresentation
                .builder(getPresentableTargetName(target))
                .icon(target.getIcon(Iconable.ICON_FLAG_VISIBILITY))
                .presentation();
    }

    public static @NotNull String getPresentableTargetName(final @NotNull PsiElement target) {
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
        final String relativePath = getRelativePath(psiFile.getProject(), virtualFile);
        final String targetText = getTargetText(target);

        if (targetText == null) {
            return virtualFile.getName() + " (" + relativePath + ")";
        }

        return virtualFile.getName() + ": " + targetText + " (" + relativePath + ")";
    }

    private static @Nullable String getTargetText(final @NotNull PsiElement target) {
        if (target instanceof PsiFile) {
            return null;
        }
        final String text = target.getText()
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();

        if (text.isEmpty() || text.startsWith("/*") || text.startsWith("//") || text.startsWith("<!--")) {
            return null;
        }

        return text.length() > 80 ? text.substring(0, 77) + "..." : text;
    }

    private static int getPriority(final @NotNull PsiElement target) {
        final String path = getPath(target);

        if (path.contains("/app/design/")) {
            return 10;
        }
        if (path.contains("/app/code/")) {
            return 20;
        }
        if (path.contains("/vendor/")) {
            return 30;
        }
        if (path.contains("/lib/web/")) {
            return 40;
        }

        return 50;
    }

    private static @NotNull String getRelativePath(
            final @NotNull Project project,
            final @NotNull VirtualFile virtualFile
    ) {
        final String projectPath = project.getBasePath();
        final String filePath = virtualFile.getPath();

        if (projectPath != null && filePath.startsWith(projectPath + "/")) {
            return filePath.substring(projectPath.length() + 1);
        }

        return filePath;
    }

    private static @NotNull String getStableKey(final @NotNull PsiElement target) {
        final String path = getPath(target);

        if (!path.isEmpty() && target instanceof PsiFile) {
            return path;
        }
        if (!path.isEmpty()) {
            return path + ":" + target.getTextRange();
        }

        return target.getTextRange().toString() + ":" + target.getText();
    }

    private static @NotNull String getPath(final @NotNull PsiElement target) {
        final VirtualFile virtualFile = getVirtualFile(target);

        return virtualFile == null ? "" : virtualFile.getPath();
    }

    private static @Nullable VirtualFile getVirtualFile(final @NotNull PsiElement target) {
        final PsiFile psiFile = target instanceof PsiFile
                ? (PsiFile) target
                : target.getContainingFile();

        return psiFile == null ? null : psiFile.getVirtualFile();
    }
}
