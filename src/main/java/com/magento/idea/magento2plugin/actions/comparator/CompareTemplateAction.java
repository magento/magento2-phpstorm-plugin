/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.comparator;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.comparator.util.DiffRequestChainUtil;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import com.magento.idea.magento2plugin.util.magento.area.AreaResolverUtil;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompareTemplateAction extends AnAction {

    public static final String ACTION_NAME = "Compare overridden template with the original one";
    public static final String ACTION_DESCRIPTION = "The Magento 2 overridden template comparing";

    private static final String PHTML_EXTENSION = "phtml";

    /**
     * Compare template action constructor.
     */
    public CompareTemplateAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    /**
     * Updates the state of action.
     *
     * @param event AnActionEvent
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void update(final @NotNull AnActionEvent event) {
        setStatus(event, false);
        if (resolveComparisonFiles(event) != null) {
            this.setStatus(event, true);
        }
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();
        final Pair<VirtualFile, VirtualFile> comparisonFiles = resolveComparisonFiles(event);

        if (project == null || comparisonFiles == null) {
            return;
        }
        final DiffRequestChain chain = DiffRequestChainUtil.createMutableChain(
                project,
                comparisonFiles.getFirst(),
                comparisonFiles.getSecond()
        );

        if (chain == null) {
            return;
        }
        DiffManager.getInstance().showDiff(
                project,
                chain,
                DiffDialogHints.DEFAULT
        );
    }

    private @Nullable String getOriginalModuleName(
            final @NotNull Project project,
            final @NotNull PsiFile psiFile
    ) {
        final PsiDirectory directory = psiFile.getContainingDirectory();
        final GetMagentoModuleUtil.MagentoModuleData component = GetMagentoModuleUtil.getByContext(
                directory,
                project
        );

        if (component == null) {
            return null;
        }
        if (ComponentType.module.equals(component.getType())) {
            return component.getName();
        }
        if (!ComponentType.theme.equals(component.getType())) {
            return null;
        }
        final String relativePath = VfsUtilCore.getRelativePath(
                psiFile.getVirtualFile(),
                component.getModuleDir().getVirtualFile(),
                '/'
        );

        if (relativePath == null || !relativePath.contains("/")) {
            return null;
        }

        return relativePath.substring(0, relativePath.indexOf('/'));
    }

    private @Nullable Pair<VirtualFile, VirtualFile> resolveComparisonFiles(
            final @NotNull AnActionEvent event
    ) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project == null || !Settings.isEnabled(project)) {
            return null;
        }
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        if (psiFile == null) {
            return null;
        }
        final VirtualFile targetFileCandidate = psiFile.getVirtualFile();

        if (targetFileCandidate == null || !PHTML_EXTENSION.equals(targetFileCandidate.getExtension())) {
            return null;
        }
        final Areas area = AreaResolverUtil.getForFileInCustomTheme(targetFileCandidate);

        if (area == null) {
            return null;
        }
        final String originalModuleName = getOriginalModuleName(project, psiFile);

        if (originalModuleName == null) {
            return null;
        }
        final PsiDirectory originalModuleDirectory =
                new ModuleIndex(project).getModuleDirectoryByModuleName(originalModuleName);

        if (originalModuleDirectory == null) {
            return null;
        }
        final String originalFilePath = originalModuleDirectory.getVirtualFile().getPath()
                + "/view/"
                + area
                + StringUtils.substringAfter(targetFileCandidate.getPath(), originalModuleName);
        final VirtualFile originalFile = VfsUtil.findFile(Path.of(originalFilePath), false);

        if (originalFile == null) {
            return null;
        }

        return Pair.create(targetFileCandidate, originalFile);
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
