/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.php.util.PhpClassFieldsSorterUtil;
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import org.jetbrains.annotations.NotNull;

public class ClassParamsNotSortedAlphabeticallyInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpClass(final PhpClass clazz) {
                final PsiFile file = clazz.getContainingFile();

                if (!IsFileInEditableModuleUtil.execute(file)) {
                    return;
                }

                if (!PhpClassFieldsSorterUtil.isSortedAlphabetically(clazz)) {
                    for (final Field field : PhpClassFieldsSorterUtil.getClassFields(clazz)) {
                        problemsHolder.registerProblem(
                                field,
                                new InspectionBundle().message(
                                        "inspection.weak.warning.php.class.fields.should.be.sorted"
                                ),
                                ProblemHighlightType.WEAK_WARNING
                        );
                    }
                }
            }
        };
    }
}
