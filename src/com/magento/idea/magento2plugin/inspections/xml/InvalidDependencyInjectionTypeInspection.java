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
import com.magento.idea.magento2plugin.inspections.validator.VirtualTypeExistenceValidator;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
public class InvalidDependencyInjectionTypeInspection extends XmlSuppressableInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {

            // Used to show messages for inspection scope.
            private final InspectionBundle inspectionBundle = new InspectionBundle();
            // Inspection validators
            private final InspectionValidator phpClassExistenceValidator =
                    new PhpClassExistenceValidator(problemsHolder.getProject());
            private final InspectionValidator virtualTypeExistenceValidator =
                    new VirtualTypeExistenceValidator(problemsHolder.getProject());
            private final InspectionValidator notEmptyValidator = new NotEmptyValidator();

            @Override
            public void visitXmlTag(final @NotNull XmlTag xmlTag) {
                final PsiFile file = xmlTag.getContainingFile();

                if (!file.getName().equals(ModuleDiXml.FILE_NAME)
                        || !xmlTag.getName().equals(ModuleDiXml.TYPE_TAG)) {
                    return;
                }
                final XmlAttribute nameAttribute = xmlTag.getAttribute(ModuleDiXml.NAME_ATTR);

                if (nameAttribute == null
                        || nameAttribute.getValue() == null
                        || nameAttribute.getValueElement() == null
                        || nameAttribute.getValueElement().getText().isEmpty()) {
                    return;
                }

                //Check whether the name attribute is not empty
                if (!notEmptyValidator.validate(nameAttribute.getValue())) {
                    reportCouldNotBeEmpty(
                            nameAttribute.getValueElement(),
                            nameAttribute.getName()
                    );
                }

                //Check whether the class exists
                if (!phpClassExistenceValidator.validate(nameAttribute.getValue())) {
                    reportClassDoesNotExists(
                            nameAttribute.getValueElement(),
                            nameAttribute.getValue()
                    );
                }
                final XmlTag argumentsTag = xmlTag.findFirstSubTag(ModuleDiXml.ARGUMENTS_TAG);

                // Break visiting if there are no arguments
                if (argumentsTag == null) {
                    return;
                }

                final List<XmlTag> argumentsTags = XmlPsiTreeUtil.findSubTagsOfParent(
                        argumentsTag,
                        ModuleDiXml.ARGUMENT_TAG
                );

                for (final XmlTag argumentTag : argumentsTags) {
                    checkObjectArgumentsRecursively(argumentTag);
                }
            }

            /**
             * Recursively check all xsi-type object attributes.
             *
             * @param tag XmlTag
             */
            private void checkObjectArgumentsRecursively(final @NotNull XmlTag tag) {
                final XmlAttribute xsiTypeAttr = tag.getAttribute(ModuleDiXml.XSI_TYPE_ATTR);

                if (xsiTypeAttr == null
                        || xsiTypeAttr.getValue() == null
                        || xsiTypeAttr.getValueElement() == null
                        || xsiTypeAttr.getValueElement().getText().isEmpty()) {
                    return;
                }
                final String xsiTypeValue = xsiTypeAttr.getValue();

                if (xsiTypeValue.equals(ModuleDiXml.XSI_TYPE_ARRAY)) {
                    final List<XmlTag> itemsTags = XmlPsiTreeUtil.findSubTagsOfParent(
                            tag,
                            ModuleDiXml.ITEM_TAG
                    );
                    if (itemsTags.isEmpty()) {
                        return;
                    }

                    for (final XmlTag itemTag : itemsTags) {
                        checkObjectArgumentsRecursively(itemTag);
                    }
                } else if (xsiTypeValue.equals(ModuleDiXml.XSI_TYPE_OBJECT)) {
                    final String tagValue = tag.getValue().getText();

                    if (tagValue.isEmpty()) {
                        return;
                    }
                    final String cleanType = tagValue
                            .replace("Factory", "")
                            .replace("\\Proxy", "");

                    if (!phpClassExistenceValidator.validate(cleanType)
                            && !virtualTypeExistenceValidator.validate(tagValue)) {
                        reportClassDoesNotExists(tag, tagValue);
                    }
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
