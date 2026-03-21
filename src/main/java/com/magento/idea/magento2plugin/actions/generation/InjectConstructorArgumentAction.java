/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewArgumentInjectionDialog;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.php.PhpPsiElementsUtil;
import org.jetbrains.annotations.NotNull;

public class InjectConstructorArgumentAction extends AnAction {

    public static final String ACTION_NAME = "Inject argument";
    public static final String ACTION_DESCRIPTION = "Inject argument through the DI";
    public static final String GATHER_ARRAY_VALUES_ACTION_DESCRIPTION = "Specify array values";

    /**
     * Inject constructor argument action constructor.
     */
    public InjectConstructorArgumentAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void update(final @NotNull AnActionEvent event) {
        setIsAvailableForEvent(event, false);
        final Project project = event.getProject();

        if (project == null
                || !Settings.isEnabled(project)
                || getActionPhpClass(event) == null
                || getConstructorParameter(event) == null) {
            return;
        }
        setIsAvailableForEvent(event, true);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();
        final PhpClass currentPhpClass = getActionPhpClass(event);
        final Parameter currentParameter = getConstructorParameter(event);

        if (project == null || currentPhpClass == null || currentParameter == null) {
            return;
        }

        NewArgumentInjectionDialog.open(
                project,
                currentPhpClass,
                currentParameter
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

    private Parameter getConstructorParameter(final @NotNull AnActionEvent event) {
        final Parameter parameter = PhpPsiElementsUtil.getMethodArgument(event);

        if (parameter == null) {
            return null;
        }
        final Method method = parameter.getParent().getParent() instanceof Method
                ? (Method) parameter.getParent().getParent() : null;

        if (method == null
                || !method.getAccess().isPublic()
                || !MagentoPhpClass.CONSTRUCT_METHOD_NAME.equals(method.getName())) {
            return null;
        }

        return parameter;
    }

    private PhpClass getActionPhpClass(final @NotNull AnActionEvent event) {
        final PhpClass phpClass = PhpPsiElementsUtil.getPhpClass(event);

        if (phpClass == null) {
            return null;
        }
        final String filename = phpClass.getContainingFile().getName();

        return filename.matches(RegExUtil.Magento.TEST_FILE_NAME)
                || phpClass.getPresentableFQN().matches(RegExUtil.Magento.TEST_CLASS_FQN)
                ? null
                : phpClass;
    }
}
