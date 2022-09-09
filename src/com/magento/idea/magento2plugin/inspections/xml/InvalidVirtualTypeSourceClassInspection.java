/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.validator.InspectionValidator;
import com.magento.idea.magento2plugin.inspections.validator.NotEmptyValidator;
import com.magento.idea.magento2plugin.inspections.validator.PhpClassExistenceValidator;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import org.jetbrains.annotations.NotNull;

public class InvalidVirtualTypeSourceClassInspection extends XmlSuppressableInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {

            private final InspectionBundle inspectionBundle = new InspectionBundle();
            private final InspectionValidator phpClassExistenceValidator =
                    new PhpClassExistenceValidator(problemsHolder.getProject());
            private final InspectionValidator notEmptyValidator = new NotEmptyValidator();

            @Override
            public void visitXmlAttribute(final XmlAttribute attribute) {
                final PsiFile file = attribute.getContainingFile();
                final XmlTag tag = attribute.getParent();

                if (file == null
                        || tag == null
                        || !file.getName().equals(ModuleDiXml.FILE_NAME)
                        || !attribute.getName().equals(ModuleDiXml.TYPE_ATTR)
                        || !tag.getName().equals(ModuleDiXml.VIRTUAL_TYPE_TAG)
                ) {
                    return;
                }

                if (attribute.getValue() == null
                        || attribute.getValueElement() == null
                        || attribute.getValueElement().getText().isEmpty()
                ) {
                    return;
                }

                if (!notEmptyValidator.validate(attribute.getValue())) {
                    problemsHolder.registerProblem(
                            attribute.getValueElement(),
                            inspectionBundle.message(
                                    "inspection.error.idAttributeCanNotBeEmpty",
                                    attribute.getName()
                            ),
                            ProblemHighlightType.ERROR
                    );
                }

                if (!phpClassExistenceValidator.validate(attribute.getValue())) {
                    problemsHolder.registerProblem(
                            attribute.getValueElement(),
                            inspectionBundle.message(
                                    "inspection.warning.class.does.not.exist",
                                    attribute.getValue()
                            ),
                            ProblemHighlightType.WARNING
                    );
                }
            }
        };
    }
}
