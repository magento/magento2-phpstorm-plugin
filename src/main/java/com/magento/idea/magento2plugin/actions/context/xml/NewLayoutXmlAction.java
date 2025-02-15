/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.xml;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewLayoutTemplateDialog;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewLayoutXmlAction extends AnAction {

    public static final String ACTION_NAME = "Magento 2 Layout File";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 layout.xml file";
    private PsiDirectory targetDirectory;

    /**
     * New layout.xml file generation action constructor.
     */
    public NewLayoutXmlAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final Project project = event.getProject();

        if (project == null || !Settings.isEnabled(project)) {
            return;
        }
        final DataContext context = event.getDataContext();
        final PsiElement targetElement = LangDataKeys.PSI_ELEMENT.getData(context);
        final PsiDirectory targetDirectoryCandidate = resolveTargetDirectory(targetElement);

        if (targetDirectoryCandidate == null) {
            return;
        }
        final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                .getByContext(targetDirectoryCandidate, project);

        if (moduleData == null) {
            return;
        }
        final PsiDirectory viewDir = moduleData.getViewDir();

        if (viewDir == null) {
            return;
        }
        final List<String> allowedDirectories = Arrays.asList(
                Package.moduleViewDir,
                Areas.adminhtml.toString(),
                Areas.frontend.toString()
        );
        if (!allowedDirectories.contains(targetDirectoryCandidate.getName())
                || !moduleData.getType().equals(ComponentType.module)) {
            return;
        }
        final PsiDirectory parentDir = targetDirectoryCandidate.getParentDirectory();

        if (parentDir == null
                || !targetDirectoryCandidate.equals(viewDir) && !parentDir.equals(viewDir)) {
            return;
        }
        targetDirectory = targetDirectoryCandidate;
        setIsAvailableForEvent(event, true);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        if (event.getProject() == null || targetDirectory == null) {
            return;
        }

        NewLayoutTemplateDialog.open(event.getProject(), targetDirectory);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * Set is action available for event.
     *
     * @param event AnActionEvent
     * @param isAvailable boolean
     */
    private void setIsAvailableForEvent(
            final @NotNull AnActionEvent event,
            final boolean isAvailable
    ) {
        event.getPresentation().setVisible(isAvailable);
        event.getPresentation().setEnabled(isAvailable);
    }

    /**
     * Resolve target directory.
     *
     * @param targetElement PsiElement
     *
     * @return PsiDirectory
     */
    private @Nullable PsiDirectory resolveTargetDirectory(final PsiElement targetElement) {
        PsiDirectory target = null;

        if (targetElement instanceof PsiDirectory) {
            target = (PsiDirectory) targetElement;
        } else if (targetElement instanceof PsiFile) {
            target = ((PsiFile) targetElement).getContainingDirectory();
        }

        if (target == null) {
            return null;
        }

        if (LayoutXml.PARENT_DIR.equals(target.getName())) {
            target = target.getParentDirectory();
        }

        return target;
    }
}
