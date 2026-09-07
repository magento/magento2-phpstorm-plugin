/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ActionUpdateThreadAware;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideClassByAPreferenceDialog;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import org.jetbrains.annotations.NotNull;

public class OverrideClassByAPreferenceAction extends DumbAwareAction
        implements ActionUpdateThreadAware {
    public static final String ACTION_NAME = "Override this class by a new Preference";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 Preference";
    public static final String INTERFACE_ACTION = "Override this interface by a new Preference";
    private final GetFirstClassOfFile getFirstClassOfFile;
    private PhpClass targetClass;

    public OverrideClassByAPreferenceAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
    }

    /**
     * Updates the state of action.
     */
    @Override
    public void update(final AnActionEvent event) {
        targetClass = null;// NOPMD
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project == null) {
            return;
        }
        if (Settings.isEnabled(project)) {
            final Pair<PsiFile, PhpClass> pair = this.findPhpClass(event);
            final PsiFile psiFile = pair.getFirst();
            final PhpClass phpClass = pair.getSecond();
            targetClass = phpClass;
            if (psiFile instanceof PhpFile && phpClass != null) {
                this.setStatus(event, true);
                if (phpClass.isInterface()) {
                    event.getPresentation().setText(INTERFACE_ACTION);
                }
            } else  {
                this.setStatus(event, false);
            }
        } else {
            this.setStatus(event, false);
        }
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        OverrideClassByAPreferenceDialog.open(event.getProject(), this.targetClass);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private Pair<PsiFile, PhpClass> findPhpClass(@NotNull final AnActionEvent event) {
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        PhpClass phpClass = null;
        if (psiFile instanceof PhpFile) {
            phpClass = getFirstClassOfFile.execute((PhpFile) psiFile);
        }

        return Pair.create(psiFile, phpClass);
    }
}
