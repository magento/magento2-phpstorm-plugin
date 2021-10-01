/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiFile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UctProblemsHolder extends ProblemsHolder {

    private final Map<ProblemDescriptor, Integer> myProblemCodes = new HashMap<>();
    private Integer reservedErrorCode;

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
     * Set reserved error code.
     *
     * @param errorCode int
     */
    public void setReservedErrorCode(final int errorCode) {
        reservedErrorCode = errorCode;
    }

    /**
     * Get issue code by problem descriptor.
     *
     * @param problemDescriptor ProblemDescriptor
     *
     * @return Integer
     */
    public @Nullable Integer getErrorCodeForDescriptor(
            final @NotNull ProblemDescriptor problemDescriptor
    ) {
        return myProblemCodes.get(problemDescriptor);
    }

    @Override
    public void registerProblem(final @NotNull ProblemDescriptor problemDescriptor) {
        final int problemCount = getMyProblems().size();
        super.registerProblem(problemDescriptor);

        // if problem has been added successfully
        if (problemCount != getMyProblems().size() && reservedErrorCode != null) {
            myProblemCodes.put(problemDescriptor, reservedErrorCode);
            reservedErrorCode = null;//NOPMD
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
            final List<ProblemDescriptor> myProblems =
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
