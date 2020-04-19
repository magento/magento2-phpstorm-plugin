/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolver;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;

public class PhpImplementResolverClassQuickFix implements LocalQuickFix {
    public static final String FAMILY_NAME = "Implements Resolver interface";
    public static final String DIALOG_TITLE = "Select one of the following interface";
    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        String[] expectedInterface = { GraphQlResolver.BATCH_RESOLVER_INTERFACE,
                GraphQlResolver.BATCH_SERVICE_CONTRACT_RESOLVER_INTERFACE,
                GraphQlResolver.RESOLVER_INTERFACE
        };
        final JComboBox expectedInterfaceDropdown = new JComboBox(expectedInterface);
        DialogBuilder dialogBox = createDialogBox(expectedInterfaceDropdown, project);
        if (dialogBox.showAndGet()) {
            String getSelectedInterface = expectedInterfaceDropdown.getSelectedItem().toString();
            PsiElement correctInterface = PhpPsiElementFactory.createImplementsList(project, getSelectedInterface);
            PhpClass graphQlResolverClass = (PhpClass) descriptor.getPsiElement().getParent();
            WriteCommandAction.writeCommandAction(project).run(() -> {
                graphQlResolverClass.getImplementsList().replace(correctInterface);
            });
        }
    }

    private DialogBuilder createDialogBox(JComboBox selectedClass, Project project)
    {
        JPanel panel = new JPanel();
        panel.add(selectedClass);
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle(DIALOG_TITLE);
        builder.setCenterPanel(panel);
        builder.addOkAction();
        builder.addCancelAction();
        return builder;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
