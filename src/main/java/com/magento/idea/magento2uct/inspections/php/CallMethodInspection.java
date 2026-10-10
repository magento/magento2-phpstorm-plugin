/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.ResolveResult;
import java.util.Map;
import java.util.TreeMap;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.inspections.UctAnalysisContext;
import org.jetbrains.annotations.NotNull;

public abstract class CallMethodInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpMethodReference(final MethodReference reference) {
                final Project project = reference.getProject();

                if (!UctAnalysisContext.isEnabled(problemsHolder)
                        || !UctAnalysisContext.accepts(problemsHolder, getSeverityLevel())) {
                    return;
                }
                // resolve() may return null for a polyvariant reference or an arbitrary first
                // declaration. Inspect every distinct resolved symbol in a stable order instead.
                final Map<String, Method> methods = new TreeMap<>();
                for (final ResolveResult result : reference.multiResolve(false)) {
                    if (result.isValidResult() && result.getElement() instanceof Method method) {
                        methods.putIfAbsent(method.getFQN(), method);
                    }
                }
                for (final Method method : methods.values()) {
                    execute(project, problemsHolder, reference, method);
                }
            }
        };
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param project Project
     * @param problemsHolder ProblemsHolder
     * @param methodReference MethodReference
     * @param method Method
     */
    protected abstract void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final MethodReference methodReference,
            final Method method
    );

    /**
     * Implement this method to specify issue severity level for target inspection.
     *
     * @return IssueSeverityLevel
     */
    protected abstract IssueSeverityLevel getSeverityLevel();
}
