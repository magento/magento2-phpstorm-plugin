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
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
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
                final Map<String, Method> methods = receiverMethods(reference);
                // When no receiver declaration is available, retain PHP's polyvariant
                // resolution (including dynamic/magic calls) instead of choosing one result.
                if (methods.isEmpty()) {
                    for (final ResolveResult result : reference.multiResolve(false)) {
                        if (result.isValidResult() && result.getElement() instanceof Method method) {
                            methods.putIfAbsent(method.getFQN(), method);
                        }
                    }
                }
                for (final Method method : methods.values()) {
                    execute(project, problemsHolder, reference, method);
                }
            }
        };
    }

    /**
     * Inspect the contract on each receiver type. multiResolve also returns interface
     * implementations and overrides that the call does not depend on (e.g. test loggers).
     * Keep explicit union/intersection members and inherited declarations in stable order.
     */
    private static Map<String, Method> receiverMethods(final MethodReference reference) {
        final Map<String, Method> methods = new TreeMap<>();
        final PhpExpression receiver = reference.getClassReference();
        if (receiver == null) {
            return methods;
        }
        final PhpIndex index = PhpIndex.getInstance(reference.getProject());
        for (final String type : receiver.getType().global(reference.getProject()).removeParametrisedParts().getTypes()) {
            for (final String member : PhpType.splitTopLevel(type, '&')) {
                for (final PhpClass phpClass : index.getAnyByFQN(member)) {
                    final Method method = phpClass.findMethodByName(reference.getName());
                    if (method != null) {
                        methods.putIfAbsent(method.getFQN(), method);
                    }
                }
            }
        }
        return methods;
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
