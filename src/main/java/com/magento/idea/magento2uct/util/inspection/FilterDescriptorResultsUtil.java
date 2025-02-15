/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.inspection;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class FilterDescriptorResultsUtil {

    private FilterDescriptorResultsUtil() {
    }

    /**
     * Filter problems by its severity.
     *
     * @param problemsHolder UctProblemsHolder
     *
     * @return List[ProblemDescriptor]
     */
    public static List<ProblemDescriptor> filter(final UctProblemsHolder problemsHolder) {
        final List<ProblemDescriptor> problems = problemsHolder.getResults();

        if (problems.isEmpty()) {
            return problems;
        }
        final List<ProblemDescriptor> filtered = new LinkedList<>();
        final Map<PsiElement, List<ProblemDescriptor>> grouped = groupDescriptors(problems);

        for (final Map.Entry<PsiElement, List<ProblemDescriptor>> descriptors
                : grouped.entrySet()) {
            final List<ProblemDescriptor> equalCandidates = new ArrayList<>();//NOPMD
            ProblemDescriptor candidate = null;

            for (final ProblemDescriptor descriptor : descriptors.getValue()) {
                if (candidate == null) {
                    candidate = descriptor;
                    continue;
                }
                final int newIssueSeverity = problemsHolder
                        .getIssue(descriptor).getLevel().getLevel();
                final int candidateIssueSeverity = problemsHolder
                        .getIssue(candidate).getLevel().getLevel();

                if (newIssueSeverity == candidateIssueSeverity) {
                    equalCandidates.add(descriptor);
                    continue;
                }

                if (newIssueSeverity < candidateIssueSeverity) {
                    candidate = descriptor;
                    equalCandidates.clear();
                }
            }
            filtered.add(candidate);
            filtered.addAll(equalCandidates);
        }

        return filtered;
    }

    /**
     * Group problems by its PSI element.
     *
     * @param problems List[ProblemDescriptor]
     *
     * @return Map[PsiElement, List[ProblemDescriptor]]
     */
    private static Map<PsiElement, List<ProblemDescriptor>> groupDescriptors(
            final List<ProblemDescriptor> problems
    ) {
        final Map<PsiElement, List<ProblemDescriptor>> grouped = new HashMap<>();

        for (final ProblemDescriptor descriptor : problems) {
            final PsiElement element = descriptor.getPsiElement();
            List<ProblemDescriptor> elementProblems;

            if (grouped.containsKey(element)) {
                elementProblems = grouped.get(element);
            } else {
                elementProblems = new ArrayList<>();//NOPMD
            }

            elementProblems.add(descriptor);
            grouped.put(element, elementProblems);
        }

        return grouped;
    }
}
