/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewWebApiDeclarationDialog;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.php.PhpPsiElementsUtil;
import org.jetbrains.annotations.NotNull;

public class NewWebApiDeclarationAction extends AnAction {

    public static final String ACTION_NAME = "Create a new Web API declaration for this method";
    public static final String ACTION_DESCRIPTION =
            "Create a new Magento 2 Web API XML declaration";
    private Method currentPhpMethod;

    /**
     * New WebApi declaration action constructor.
     */
    public NewWebApiDeclarationAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final Project project = event.getProject();
        final Method method = PhpPsiElementsUtil.getPhpMethod(event);

        if (project == null || !Settings.isEnabled(project) || method == null) {
            return;
        }

        if (method.getContainingClass() == null) {
            return;
        }

        if (!method.getAccess().isPublic()
                || method.getName().equals(MagentoPhpClass.CONSTRUCT_METHOD_NAME)) {
            return;
        }

        currentPhpMethod = method;
        setIsAvailableForEvent(event, true);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final PsiDirectory directory =
                currentPhpMethod.getContainingFile().getContainingDirectory();

        if (event.getProject() == null || directory == null) {
            return;
        }

        final PhpClass phpClass = currentPhpMethod.getContainingClass();

        if (phpClass == null) {
            return;
        }

        final String classFqn = phpClass.getPresentableFQN();
        final String methodName = currentPhpMethod.getName();

        NewWebApiDeclarationDialog.open(event.getProject(), directory, classFqn, methodName);
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
}
