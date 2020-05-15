/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml.fix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import org.jetbrains.annotations.NotNull;

public class XmlModuleNameQuickFix implements LocalQuickFix {

    private final String expectedModuleName;

    public XmlModuleNameQuickFix(final String expectedModuleName) {
        this.expectedModuleName = expectedModuleName;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        final InspectionBundle inspectionBundle = new InspectionBundle();

        return inspectionBundle.message(
                "inspection.moduleDeclaration.fix"
        );
    }

    @Override
    public void applyFix(
            @NotNull final Project project,
            @NotNull final ProblemDescriptor descriptor
    ) {
        final XmlAttributeValue value = (XmlAttributeValue) descriptor.getPsiElement();
        applyFix(value);
    }

    private void applyFix(final XmlAttributeValue value) {
        WriteCommandAction.writeCommandAction(
                value.getManager().getProject(),
                value.getContainingFile()
        ).run(() ->
                doFix(value)
        );
    }

    protected void doFix(
            final XmlAttributeValue value
    ) {
        final XmlAttribute xmlAttribute = (XmlAttribute) value.getParent();
        xmlAttribute.setValue(expectedModuleName);
    }
}
