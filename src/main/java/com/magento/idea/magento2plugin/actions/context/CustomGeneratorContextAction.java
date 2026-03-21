/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.context.util.GetTargetElementUtil;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomGeneratorContextAction extends AnAction {

    /**
     * Abstract context action with custom generation constructor.
     *
     * @param text String
     * @param description String
     */
    public CustomGeneratorContextAction(
            final @Nullable @NlsActions.ActionText String text,
            final @Nullable @NlsActions.ActionDescription String description
    ) {
        super(text, description, MagentoIcons.MODULE);
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final ActionContext context = resolveActionContext(event);

        if (context == null || context.moduleData.getName() == null) {
            return;
        }
        setIsAvailableForEvent(event, true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * Implement check if an action should be shown in the context defined by the module,
     * target directory or target file.
     *
     * @param moduleData GetMagentoModuleUtil.MagentoModuleData
     * @param targetDirectory PsiDirectory
     * @param targetFile PsiFile
     *
     * @return boolean
     */
    protected abstract boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    );

    protected @Nullable ActionContext resolveActionContext(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null || !Settings.isEnabled(project)) {
            return null;
        }
        final PsiDirectory targetDirectory = GetTargetElementUtil.getDirFromEvent(event);

        if (targetDirectory == null) {
            return null;
        }
        final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                .getByContext(targetDirectory, project);
        final PsiFile targetFile = GetTargetElementUtil.getFileFromEvent(event);

        if (moduleData == null || !isVisible(moduleData, targetDirectory, targetFile)) {
            return null;
        }

        return new ActionContext(moduleData, targetDirectory, targetFile);
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

    protected static final class ActionContext {
        private final GetMagentoModuleUtil.MagentoModuleData moduleData;
        private final PsiDirectory directory;
        private final PsiFile file;

        private ActionContext(
                final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
                final @NotNull PsiDirectory directory,
                final @Nullable PsiFile file
        ) {
            this.moduleData = moduleData;
            this.directory = directory;
            this.file = file;
        }

        public @NotNull GetMagentoModuleUtil.MagentoModuleData getModuleData() {
            return moduleData;
        }

        public @NotNull PsiDirectory getDirectory() {
            return directory;
        }

        public @Nullable PsiFile getFile() {
            return file;
        }
    }
}
