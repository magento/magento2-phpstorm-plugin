/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class UctProblemsHolder extends ProblemsHolder {

    private final Map<ProblemDescriptor, SupportedIssue> myProblemCodes = new HashMap<>();
    private SupportedIssue issue;
    private final UctAnalysisContext analysisContext;

    /**
     * UCT problems holder constructor.
     *
     * @param manager InspectionManager
     * @param file PsiFile
     * @param isOnTheFly boolean
     */
    public UctProblemsHolder(
            final @NotNull InspectionManager manager,
            final @NotNull PsiFile file,
            final boolean isOnTheFly
    ) {
        this(manager, file, isOnTheFly, null);
    }

    public UctProblemsHolder(
            final InspectionManager manager,
            final PsiFile file,
            final boolean isOnTheFly,
            final UctAnalysisContext analysisContext
    ) {
        super(manager, file, isOnTheFly);
        this.analysisContext = analysisContext;
    }

    public UctAnalysisContext getAnalysisContext() {
        return analysisContext;
    }

    /**
     * Set reserved issue.
     *
     * @param issue SupportedIssue
     */
    public void setIssue(final @NotNull SupportedIssue issue) {
        this.issue = issue;
    }

    /**
     * Get issue by problem descriptor.
     *
     * @param problemDescriptor ProblemDescriptor
     *
     * @return Integer
     */
    public @NotNull SupportedIssue getIssue(
            final @NotNull ProblemDescriptor problemDescriptor
    ) {
        return myProblemCodes.get(problemDescriptor);
    }

    @Override
    public void registerProblem(final @NotNull ProblemDescriptor problemDescriptor) {
        if (issue == null) {
            throw new InputMismatchException(
                    "For the UCT CLI inspection it is mandatory to set an issue via "
                            + "UctProblemsHolder.setIssue method"
            );
        }
        if (analysisContext != null && issue.getLevel().getLevel() > analysisContext.minimumSeverity().getLevel()) {
            return;
        }
        final int problemCount = getResultCount();
        super.registerProblem(problemDescriptor);

        // if problem has been added successfully
        if (problemCount != getResultCount()) {
            myProblemCodes.put(problemDescriptor, issue);
        }
    }
}
