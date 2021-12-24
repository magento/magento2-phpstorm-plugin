/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.php.fix.PhpModuleNameQuickFix;
import com.magento.idea.magento2plugin.inspections.util.GetEditableModuleNameByRootFileUtil;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import org.jetbrains.annotations.NotNull;

public class ModuleDeclarationInRegistrationPhpInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpStringLiteralExpression(final StringLiteralExpression expression) {
                final PsiFile file =  expression.getContainingFile();
                final String filename = file.getName();

                if (!RegistrationPhp.FILE_NAME.equals(filename)) {
                    return;
                }
                final MethodReference callerReference = PsiTreeUtil.getParentOfType(
                        expression,
                        MethodReference.class
                );

                if (callerReference == null) {
                    return;
                }
                final PsiElement caller = callerReference.resolve();

                if (!(caller instanceof Method)) {
                    return;
                }
                final PhpClass callerOwner = ((Method) caller).getContainingClass();

                if (callerOwner == null
                        || !FrameworkLibraryType.COMPONENT_REGISTRAR.getType().equals(
                                callerOwner.getPresentableFQN()
                )) {
                    return;
                }

                if (!IsFileInEditableModuleUtil.execute(file)) {
                    return;
                }
                final String expectedName = GetEditableModuleNameByRootFileUtil.execute(file);
                final String actualName = expression.getContents();

                if (actualName.equals(expectedName)) {
                    return;
                }
                final InspectionBundle inspectionBundle = new InspectionBundle();

                problemsHolder.registerProblem(
                        expression,
                        inspectionBundle.message(
                                "inspection.moduleDeclaration.warning.wrongModuleName",
                                actualName,
                                expectedName
                        ),
                        ProblemHighlightType.ERROR,
                        new PhpModuleNameQuickFix(expectedName)
                );
            }
        };
    }
}
