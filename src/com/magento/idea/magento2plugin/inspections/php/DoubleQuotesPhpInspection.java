/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import org.jetbrains.annotations.NotNull;


public class DoubleQuotesPhpInspection extends PhpInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpStringLiteralExpression(final StringLiteralExpression expression) {
                final String fileText = expression.getText();

                final char doubleQuote = '"';
                if (fileText.charAt(0) != doubleQuote) {
                    return;
                }

                if (fileText.contains("$")) {
                    return;
                }

                final InspectionBundle inspectionBundle = new InspectionBundle();
                problemsHolder.registerProblem(
                        expression,
                        inspectionBundle.message(
                            "inspection.warning.double.quotes.misuse"
                        ),
                        ProblemHighlightType.WARNING
                );
            }
        };
    }
}
