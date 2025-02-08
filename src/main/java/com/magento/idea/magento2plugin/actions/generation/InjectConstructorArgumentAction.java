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
    private PhpClass currentPhpClass;
    private Parameter currentParameter;

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

        if (project == null || !Settings.isEnabled(project)) {
            return;
        }
        final PhpClass phpClass = PhpPsiElementsUtil.getPhpClass(event);

        if (phpClass == null) {
            return;
        }
        // Excluding argument injection generators for Test/ and *Test.php files
        // in order to not overload the context menu.
        final String filename = phpClass.getContainingFile().getName();

        if (filename.matches(RegExUtil.Magento.TEST_FILE_NAME)
                || phpClass.getPresentableFQN().matches(RegExUtil.Magento.TEST_CLASS_FQN)) {
            return;
        }
        final Parameter parameter = PhpPsiElementsUtil.getMethodArgument(event);

        if (parameter == null) {
            return;
        }
        final Method method = parameter.getParent().getParent() instanceof Method
                ? (Method) parameter.getParent().getParent() : null;

        if (method == null) {
            return;
        }

        if (!method.getAccess().isPublic()
                || !MagentoPhpClass.CONSTRUCT_METHOD_NAME.equals(method.getName())) {
            return;
        }
        currentPhpClass = phpClass;
        currentParameter = parameter;
        setIsAvailableForEvent(event, true);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        if (event.getProject() == null
                || currentPhpClass == null
                || currentParameter == null) {
            return;
        }

        NewArgumentInjectionDialog.open(
                event.getProject(),
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
}
