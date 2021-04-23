/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.inspections.quickfix.PhpChangeMethodModifiersQuickFix;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import org.jetbrains.annotations.NotNull;

public class MethodNotPublicAccessQuickFix implements LocalQuickFix {
    private final InspectionBundle inspectionBundle = new InspectionBundle();
    private final Method method;

    public MethodNotPublicAccessQuickFix(final Method method) {
        this.method = method;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return inspectionBundle.message("inspection.warning.method.should.have.public.access.fix");
    }

    @Override
    public void applyFix(
            final @NotNull Project project,
            final @NotNull ProblemDescriptor descriptor
    ) {
        PhpChangeMethodModifiersQuickFix.changeMethodModifier(
                this.method,
                PhpModifier.PUBLIC_IMPLEMENTED_DYNAMIC
        );
    }
}
