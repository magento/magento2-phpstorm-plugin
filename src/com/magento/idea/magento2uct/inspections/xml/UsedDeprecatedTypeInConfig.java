/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.xml;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UsedDeprecatedTypeInConfig extends ModuleConfigFileInspection {

    @Override
    protected void doInspection(
            final @NotNull String fqn,
            final @NotNull PsiElement target,
            final @NotNull InspectionManager manager,
            final boolean isOnTheFly,
            final @NotNull List<ProblemDescriptor> descriptors
    ) {
        if (VersionStateManager.getInstance(manager.getProject()).isDeprecated(fqn)) {
            final String message = SupportedIssue.USED_DEPRECATED_TYPE_IN_CONFIG.getMessage(
                    fqn,
                    VersionStateManager.getInstance(
                            manager.getProject()
                    ).getDeprecatedInVersion(fqn)
            );

            final ProblemDescriptor descriptor = manager.createProblemDescriptor(
                    target,
                    message,
                    null,
                    ProblemHighlightType.WARNING,
                    isOnTheFly,
                    false
            );
            descriptors.add(descriptor);
        }
    }
}