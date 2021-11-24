/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.api;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.actions.PhpExpressionTypeProvider;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.inspections.php.util.PhpClassImplementsInterfaceUtil;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.CallMethodInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class CalledNonInterfaceMethod extends CallMethodInspection {

    private final PhpExpressionTypeProvider typeProvider = new PhpExpressionTypeProvider();

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final MethodReference methodReference,
            final Method method
    ) {
        final PhpClass parentClass = method.getContainingClass();

        if (parentClass == null) {
            return;
        }
        final String informationHint = typeProvider.getInformationHint(methodReference);

        if (informationHint.isEmpty()) {
            return;
        }
        final Collection<PhpClass> searchResult = PhpIndex.getInstance(project)
                .getInterfacesByFQN(informationHint);

        if (searchResult.isEmpty()) {
            return;
        }
        final PhpClass interfaceCandidate = searchResult.stream().iterator().next();

        if (interfaceCandidate == null
                || !interfaceCandidate.isInterface()
                || !PhpClassImplementsInterfaceUtil.execute(interfaceCandidate, parentClass)
        ) {
            return;
        }
        final Collection<Method> methodList = interfaceCandidate.getMethods();
        final List<String> methodNames = methodList
                .stream()
                .map(Method::getName)
                .collect(Collectors.toList());

        if (methodNames.contains(method.getName())) {
            return;
        }

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.CALLED_NON_INTERFACE_METHOD
            );
        }
        problemsHolder.registerProblem(
                methodReference,
                SupportedIssue.CALLED_NON_INTERFACE_METHOD.getMessage(
                        interfaceCandidate.getFQN()
                ),
                ProblemHighlightType.WARNING
        );
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.CALLED_NON_INTERFACE_METHOD.getLevel();
    }
}
