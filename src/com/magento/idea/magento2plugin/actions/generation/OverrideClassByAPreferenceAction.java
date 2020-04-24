/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

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

public class OverrideClassByAPreferenceAction extends DumbAwareAction {
    public static String ACTION_NAME = "Override Class By a Preference...";
    public static String ACTION_DESCRIPTION = "Create a new Magento 2 preference for the class";
    private final GetFirstClassOfFile getFirstClassOfFile;
    private PhpClass targetClass;

    public OverrideClassByAPreferenceAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
    }

    public void update(AnActionEvent event) {
        targetClass = null;
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if (Settings.isEnabled(project)) {
            Pair<PsiFile, PhpClass> pair = this.findPhpClass(event);
            PsiFile psiFile = pair.getFirst();
            PhpClass phpClass = pair.getSecond();
            targetClass = phpClass;
            if (psiFile instanceof PhpFile && phpClass != null) {
                this.setStatus(event, true);
            } else {
                this.setStatus(event, false);
            }
        } else {
            this.setStatus(event, false);
        }
    }

    private void setStatus(AnActionEvent event, boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        OverrideClassByAPreferenceDialog.open(e.getProject(), this.targetClass);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    private Pair<PsiFile, PhpClass> findPhpClass(@NotNull AnActionEvent event) {
        PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        PhpClass phpClass = null;
        if (psiFile instanceof PhpFile) {
            phpClass = getFirstClassOfFile.execute((PhpFile) psiFile);
        }

        return Pair.create(psiFile, phpClass);
    }
}
