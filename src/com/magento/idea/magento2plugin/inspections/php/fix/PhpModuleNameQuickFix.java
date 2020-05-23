/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import org.jetbrains.annotations.NotNull;

public class PhpModuleNameQuickFix implements LocalQuickFix {

    private final String expectedModuleName;

    public PhpModuleNameQuickFix(final String expectedModuleName) {
        this.expectedModuleName = expectedModuleName;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        final InspectionBundle inspectionBundle = new InspectionBundle();

        return inspectionBundle.message(
                "inspection.moduleDeclaration.fix"
        );
    }

    @Override
    public void applyFix(
            @NotNull final Project project,
            @NotNull final ProblemDescriptor descriptor
    ) {
        final StringLiteralExpression expression =
                (StringLiteralExpression) descriptor.getPsiElement();
        applyFix(expression);
    }

    private void applyFix(final StringLiteralExpression expression) {
        WriteCommandAction.writeCommandAction(
                expression.getManager().getProject(),
                expression.getContainingFile()
        ).run(() ->
                expression.updateText(expectedModuleName)
        );
    }
}
