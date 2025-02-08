/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.inspection;

import com.intellij.codeInspection.ProblemDescriptor;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public final class SortDescriptorResultsUtil {

    private SortDescriptorResultsUtil() {
    }

    /**
     * Get problems sorted by its lines.
     *
     * @param problems List[ProblemDescriptor]
     *
     * @return List[ProblemDescriptor]
     */
    public static List<ProblemDescriptor> sort(final List<ProblemDescriptor> problems) {
        if (problems.isEmpty()) {
            return problems;
        }
        problems.sort(Comparator.comparingInt(ProblemDescriptor::getLineNumber));

        return new LinkedList<>(problems);
    }
}
