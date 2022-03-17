/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.comparator;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.actions.BlankDiffWindowUtil;
import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompareTemplateAction extends AnAction {

    public static final String ACTION_NAME = "Compare Template with Original";
    public static final String ACTION_DESCRIPTION = "Compare Template with Original";

    private static final String PHTML_EXTENSION = "phtml";
    protected VirtualFile selectedFile;
    protected VirtualFile originalFile;

    /**
     * Inject constructor argument action constructor.
     */
    public CompareTemplateAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    /**
     * Updates the state of action.
     */
    @Override
    public void update(final @NotNull AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return;
        }

        if (!Settings.isEnabled(project)) {
            this.setStatus(event, false);
            return;
        }
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);
        selectedFile = psiFile != null ? psiFile.getVirtualFile() : null;//NOPMD

        if (selectedFile != null
                && !PHTML_EXTENSION.equals(selectedFile.getExtension())
        ) {
            this.setStatus(event, false);
            return;
        }

        final String fullPath = selectedFile.getPath();
        final String area = getArea(fullPath);
        final String originalModuleName = getOriginalModuleName(project, psiFile);
        final PsiDirectory originalModuleDirectory =
                new ModuleIndex(project).getModuleDirectoryByModuleName(originalModuleName);

        if (originalModuleDirectory == null
                || area == null
        ) {
            this.setStatus(event, false);
            return;
        }

        final String originalFilePath = originalModuleDirectory.getVirtualFile().getPath()
                + "/view/"
                + area
                + StringUtils.substringAfter(fullPath, originalModuleName);

        originalFile = VfsUtil.findFile(Path.of(originalFilePath), false);

        if (originalFile != null) {
            this.setStatus(event, true);
            return;
        }

        this.setStatus(event, false);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();
        final DiffRequestChain chain =
                createMutableChainFromFiles(project, selectedFile, originalFile);

        DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT);
    }

    @Nullable
    private String getArea(final String fullPath) {
        final Pattern pattern = Pattern.compile(RegExUtil.ViewArea.AREA);
        final Matcher matcher = pattern.matcher(fullPath);
        String areaName = null;
        if (matcher.find()) {
            areaName =  matcher.group(1);
        }

        return areaName;
    }

    private String getOriginalModuleName(final Project project, final PsiFile psiFile) {
        final PsiDirectory directory = psiFile.getContainingDirectory();

        return GetModuleNameByDirectoryUtil.execute(directory, project);
    }

    @NotNull
    private MutableDiffRequestChain createMutableChainFromFiles(
            final @Nullable Project project,
            final @NotNull VirtualFile file1,
            final @NotNull VirtualFile file2
    ) {
        final DiffContentFactory contentFactory = DiffContentFactory.getInstance();
        final DiffRequestFactory requestFactory = DiffRequestFactory.getInstance();

        final DiffContent content1 = contentFactory.create(project, file1);
        final DiffContent content2 = contentFactory.create(project, file2);

        final MutableDiffRequestChain chain = BlankDiffWindowUtil.createBlankDiffRequestChain(
                (DocumentContent)content1,
                (DocumentContent)content2,
                null
        );
        chain.setWindowTitle(requestFactory.getTitle(file1, file2));

        return chain;
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }
}