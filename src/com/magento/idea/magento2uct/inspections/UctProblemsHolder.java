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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class UctProblemsHolder extends ProblemsHolder {

    private final Map<ProblemDescriptor, SupportedIssue> myProblemCodes = new HashMap<>();
    private SupportedIssue issue;

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
        super(manager, file, isOnTheFly);
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
        final int problemCount = getMyProblems().size();
        super.registerProblem(problemDescriptor);

        // if problem has been added successfully
        if (problemCount != getMyProblems().size()) {
            myProblemCodes.put(problemDescriptor, issue);
        }
    }

    /**
     * Get my problems.
     *
     * @return List[ProblemDescriptor]
     */
    private List<ProblemDescriptor> getMyProblems() {
        Field myProblemsField = null;

        try {
            myProblemsField = ProblemsHolder.class.getDeclaredField("myProblems");
            myProblemsField.setAccessible(true);

            @SuppressWarnings("unchecked") final List<ProblemDescriptor> myProblems =
                    (List<ProblemDescriptor>) myProblemsField.get(this);
            myProblemsField.setAccessible(false);

            return myProblems;
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException exception) {
            return new ArrayList<>();
        } finally {
            if (myProblemsField != null) {
                myProblemsField.setAccessible(false);
            }
        }
    }
}
