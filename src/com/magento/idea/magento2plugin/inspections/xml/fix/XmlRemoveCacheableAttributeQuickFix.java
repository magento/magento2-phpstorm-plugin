/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.xml.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class XmlRemoveCacheableAttributeQuickFix implements LocalQuickFix {
    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove cacheable attribute";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        descriptor.getPsiElement().delete();
    }
}
