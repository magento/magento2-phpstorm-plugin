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
import com.jetbrains.php.refactoring.extract.extractInterface.PhpExtractInterfaceProcessor;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolver;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;

public class PhpImplementResolverClassQuickFix implements LocalQuickFix {
    private InspectionBundle inspectionBundle = new InspectionBundle();

    @NotNull
    @Override
    public String getFamilyName() {
        return inspectionBundle.message("inspection.graphql.resolver.fix.family");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        String[] expectedInterface = {
            GraphQlResolver.BATCH_RESOLVER_INTERFACE,
            GraphQlResolver.BATCH_SERVICE_CONTRACT_RESOLVER_INTERFACE,
            GraphQlResolver.RESOLVER_INTERFACE
        };
        final JComboBox expectedInterfaceDropdown = new JComboBox(expectedInterface);
        ((JLabel)expectedInterfaceDropdown.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        DialogBuilder dialogBox = createDialogBox(expectedInterfaceDropdown, project);
        if (dialogBox.showAndGet()) {
            String getSelectedInterface = expectedInterfaceDropdown.getSelectedItem().toString();
            final PsiElement erroredElement = descriptor.getPsiElement();
            final PsiElement correctInterface = PhpPsiElementFactory.createImplementsList(project, getSelectedInterface);
            final PhpClass graphQlResolverClass = (PhpClass) erroredElement.getParent();
            String[] implementedInterfaceNames = graphQlResolverClass.getInterfaceNames();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                if (implementedInterfaceNames.length == 0) {
                    PhpExtractInterfaceProcessor.addImplementClause(project, graphQlResolverClass, getSelectedInterface);
                } else {
                    graphQlResolverClass.getImplementsList().replace(correctInterface);
                }
            });
        }
    }

    private DialogBuilder createDialogBox(JComboBox selectedClass, Project project)
    {
        JPanel panel = new JPanel();
        panel.add(selectedClass);
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle(inspectionBundle.message("inspection.graphql.resolver.fix.title"));
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
