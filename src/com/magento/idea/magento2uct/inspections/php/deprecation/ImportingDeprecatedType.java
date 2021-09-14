/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public abstract class ImportingDeprecatedType extends PhpInspection {

    /**
     * Register type specific problem.
     *
     * @param problemsHolder ProblemsHolder
     * @param use PhpUse
     * @param isInterface boolean
     */
    protected abstract void registerProblem(
            final @NotNull ProblemsHolder problemsHolder,
            final PhpUse use,
            final boolean isInterface
    );

    /**
     * Get issue severity level for concrete inspection.
     *
     * @return IssueSeverityLevel
     */
    protected abstract IssueSeverityLevel getSeverityLevel();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpUseList(final PhpUseList useList) {
                final Project project = useList.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled()
                        || !settings.isIssueLevelSatisfiable(getSeverityLevel())) {
                    return;
                }
                final PhpClass phpClass = getParentClass(useList);

                if (phpClass == null) {
                    return;
                }
                for (final PhpUse use : useList.getDeclarations()) {
                    if (VersionStateManager.getInstance(project).isDeprecated(use.getFQN())) {
                        final PhpReference phpReference = use.getTargetReference();
                        boolean isInterface = false;

                        if (phpReference != null) {
                            final PsiElement element = phpReference.resolve();

                            if (element instanceof PhpClass
                                    && ((PhpClass) element).isInterface()) {
                                isInterface = true;
                            }
                        }
                        registerProblem(problemsHolder, use, isInterface);
                    }
                }
            }

            /**
             * Get parent class for use list.
             *
             * @param useList PhpUseList
             *
             * @return PhpClass
             */
            private PhpClass getParentClass(final PhpUseList useList) {
                final PsiFile file = useList.getContainingFile();

                if (!(file instanceof PhpFile)) {
                    return null;
                }

                return GetFirstClassOfFile.getInstance().execute((PhpFile) file);
            }
        };
    }
}
