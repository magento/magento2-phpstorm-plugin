/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiElement;
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

public class PluginAttributeTypeInspection extends XmlSuppressableInspectionTool {

    @Override
    public @NotNull
    PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {

            private final InspectionBundle inspectionBundle = new InspectionBundle();
            private final InspectionValidator phpClassExistenceValidator =
                    new PhpClassExistenceValidator(problemsHolder.getProject());
            private final InspectionValidator notEmptyValidator = new NotEmptyValidator();

            @Override
            public void visitXmlTag(final XmlTag xmlTag) {
                final PsiFile file = xmlTag.getContainingFile();

                if (!file.getName().equals(ModuleDiXml.FILE_NAME)
                        || !xmlTag.getName().equals(ModuleDiXml.PLUGIN_TAG_NAME)) {
                    return;
                }

                final XmlAttribute pluginTypeAttribute =
                        xmlTag.getAttribute(ModuleDiXml.TYPE_ATTR);

                if (pluginTypeAttribute == null
                        || pluginTypeAttribute.getValue() == null
                        || pluginTypeAttribute.getValueElement() == null
                        || pluginTypeAttribute.getValueElement().getText().isEmpty()) {
                    return;
                }

                if (!notEmptyValidator.validate(pluginTypeAttribute.getValue())) {
                    reportCouldNotBeEmpty(
                            pluginTypeAttribute.getValueElement(),
                            pluginTypeAttribute.getName()
                    );
                }

                if (!phpClassExistenceValidator.validate(pluginTypeAttribute.getValue())) {
                    reportClassDoesNotExists(
                            pluginTypeAttribute.getValueElement(),
                            pluginTypeAttribute.getValue()
                    );
                }
            }

            /**
             * Report Attribute Value could not be empty.
             *
             * @param psiElement PsiElement
             * @param messageParams Object...
             */
            private void reportCouldNotBeEmpty(
                    final @NotNull PsiElement psiElement,
                    final Object... messageParams
            ) {
                problemsHolder.registerProblem(
                        psiElement,
                        inspectionBundle.message(
                                "inspection.error.idAttributeCanNotBeEmpty",
                                messageParams
                        ),
                        ProblemHighlightType.ERROR
                );
            }

            /**
             * Report class does not exists.
             *
             * @param psiElement PsiElement
             * @param messageParams Object...
             */
            private void reportClassDoesNotExists(
                    final @NotNull PsiElement psiElement,
                    final Object... messageParams
            ) {
                problemsHolder.registerProblem(
                        psiElement,
                        inspectionBundle.message(
                                "inspection.warning.class.does.not.exist",
                                messageParams
                        ),
                        ProblemHighlightType.WARNING
                );
            }
        };
    }
}
