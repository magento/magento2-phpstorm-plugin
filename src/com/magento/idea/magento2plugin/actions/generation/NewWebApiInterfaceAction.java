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
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewInterfaceForServiceDialog;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import com.magento.idea.magento2plugin.util.php.PhpPsiElementsUtil;
import org.jetbrains.annotations.NotNull;

public class NewWebApiInterfaceAction extends AnAction {

    public static final String ACTION_NAME = "Create a new Web API interface for this class";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 Web API interface";
    private PhpClass currentPhpClass;

    /**
     * New Web API interface action constructor.
     */
    public NewWebApiInterfaceAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final Project project = event.getProject();

        if (project == null || !Settings.isEnabled(project)) {
            return;
        }
        final PhpClass phpClass = PhpPsiElementsUtil.getPhpClass(event);

        if (phpClass == null
                || phpClass.isAbstract()
                || !IsFileInEditableModuleUtil.execute(phpClass.getContainingFile())) {
            return;
        }
        // Excluding API generators for Test/ and *Test.php files
        // in order to not overload the context menu.
        final String filename = phpClass.getContainingFile().getName();

        if (filename.matches(RegExUtil.Magento.TEST_FILE_NAME)
                || phpClass.getPresentableFQN().matches(RegExUtil.Magento.TEST_CLASS_FQN)) {
            return;
        }

        currentPhpClass = phpClass;
        setIsAvailableForEvent(event, true);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final PsiDirectory directory =
                currentPhpClass.getContainingFile().getContainingDirectory();

        if (event.getProject() == null || currentPhpClass == null || directory == null) {
            return;
        }

        NewInterfaceForServiceDialog.open(
                event.getProject(),
                directory,
                currentPhpClass
        );
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
