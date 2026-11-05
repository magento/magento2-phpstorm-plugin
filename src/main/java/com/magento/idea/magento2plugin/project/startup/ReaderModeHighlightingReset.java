/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.startup;

import com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightingSettingsPerFile;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.indexing.MagentoReaderModeMatcher;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Turns inspections back on, once per project, for project code that Reader Mode switched off
 * before {@link MagentoReaderModeMatcher} excluded it, because a stored per-file level keeps
 * applying after the file stops matching Reader Mode. A level the user chose by hand before
 * this ran is stored in the same form, so it is reset too; later choices are kept unless the
 * folder is then marked as a Magento content root.
 */
public final class ReaderModeHighlightingReset {
    static final String DONE_KEY = "magento2plugin.readerModeHighlightingResetDone";

    private ReaderModeHighlightingReset() {
    }

    /**
     * Runs the reset unless it already ran for the project.
     *
     * @param project the current project
     */
    public static void run(final @NotNull Project project) {
        final PropertiesComponent properties = PropertiesComponent.getInstance(project);

        if (properties.getBoolean(DONE_KEY)
                || !Settings.isEnabled(project)
                || !MagentoBasePathUtil.isMagentoFolderValid(Settings.getMagentoPath(project))) {
            return;
        }
        enableInspections(project, file -> true, () -> properties.setValue(DONE_KEY, true));
    }

    /**
     * Runs the reset for a folder that was just marked as a Magento content root.
     *
     * @param project the current project
     * @param folder the marked folder
     */
    public static void runForFolder(final @NotNull Project project, final @NotNull VirtualFile folder) {
        enableInspections(project, file -> VfsUtilCore.isAncestor(folder, file, false), () -> { });
    }

    private static void enableInspections(
            final Project project,
            final Predicate<VirtualFile> filter,
            final Runnable onDone
    ) {
        final HighlightingSettingsPerFile settings = HighlightingSettingsPerFile.getInstance(project);
        final List<PsiFile> roots = ApplicationManager.getApplication().runReadAction(
                (Computable<List<PsiFile>>) () -> findSkippedRoots(project, settings, filter)
        );

        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            for (final PsiFile root : roots) {
                if (root.isValid()) {
                    settings.setHighlightingSettingForRoot(
                            root,
                            FileHighlightingSetting.FORCE_HIGHLIGHTING
                    );
                }
            }
            onDone.run();
        });
    }

    private static List<PsiFile> findSkippedRoots(
            final Project project,
            final HighlightingSettingsPerFile settings,
            final Predicate<VirtualFile> filter
    ) {
        final List<PsiFile> roots = new ArrayList<>();

        for (final Element entry : settings.getState().getChildren()) {
            final VirtualFile file = VirtualFileManager.getInstance()
                    .findFileByUrl(entry.getAttributeValue("file"));

            if (file == null
                    || !filter.test(file)
                    || !MagentoReaderModeMatcher.isExcludedFromReaderMode(project, file)) {
                continue;
            }
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

            if (psiFile == null) {
                continue;
            }
            for (final PsiFile root : psiFile.getViewProvider().getAllFiles()) {
                if (settings.getHighlightingSettingForRoot(root)
                        == FileHighlightingSetting.SKIP_INSPECTION) {
                    roots.add(root);
                }
            }
        }

        return roots;
    }
}
