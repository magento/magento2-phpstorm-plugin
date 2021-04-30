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
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.validator.InspectionValidator;
import com.magento.idea.magento2plugin.inspections.validator.NotEmptyValidator;
import com.magento.idea.magento2plugin.inspections.validator.PhpClassExistenceValidator;
import com.magento.idea.magento2plugin.inspections.xml.fix.MethodNotPublicAccessQuickFix;
import com.magento.idea.magento2plugin.magento.files.ModuleWebApiXmlFile;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
public class WebApiServiceInspection extends XmlSuppressableInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {

            private final InspectionBundle inspectionBundle = new InspectionBundle();
            // Inspection validators
            private final InspectionValidator notEmptyValidator = new NotEmptyValidator();
            private final InspectionValidator phpClassExistenceValidator =
                    new PhpClassExistenceValidator(problemsHolder.getProject());

            @Override
            public void visitXmlTag(final XmlTag xmlTag) {
                final PsiFile file = xmlTag.getContainingFile();
                final String filename = file.getName();
                if (!filename.equals(ModuleWebApiXmlFile.FILE_NAME)) {
                    return;
                }

                if (!xmlTag.getName().equals(ModuleWebApiXmlFile.SERVICE_TAG_NAME)) {
                    return;
                }

                //Check whether the class attribute is not empty
                final XmlAttribute classAttribute = xmlTag.getAttribute(
                        ModuleWebApiXmlFile.CLASS_ATTR
                );
                if (classAttribute == null) {
                    return;
                }
                final String classFqn = classAttribute.getValue();

                if (!notEmptyValidator.validate(classFqn)) {
                    problemsHolder.registerProblem(
                            classAttribute,
                            inspectionBundle.message(
                                "inspection.error.idAttributeCanNotBeEmpty",
                                ModuleWebApiXmlFile.CLASS_ATTR
                            ),
                            ProblemHighlightType.WARNING
                    );

                    return;
                }

                if (!phpClassExistenceValidator.validate(classFqn)) {
                        problemsHolder.registerProblem(
                                classAttribute,
                                inspectionBundle.message(
                                    "inspection.warning.class.does.not.exist",
                                     classFqn
                                ),
                                ProblemHighlightType.WARNING
                        );
                        return;
                }

                //Check whether the method attribute is not empty
                final XmlAttribute methodAttribute = xmlTag.getAttribute(
                        ModuleWebApiXmlFile.METHOD_ATTR
                );
                if (methodAttribute == null) {
                    return;
                }
                final String methodName = methodAttribute.getValue();

                if (!notEmptyValidator.validate(methodName)) {
                    problemsHolder.registerProblem(
                            classAttribute,
                            inspectionBundle.message(
                                "inspection.error.idAttributeCanNotBeEmpty",
                                ModuleWebApiXmlFile.METHOD_ATTR
                            ),
                            ProblemHighlightType.WARNING
                    );

                    return;
                }

                //Check whether method exists
                final PhpIndex phpIndex = PhpIndex.getInstance(problemsHolder.getProject());
                final @NotNull Collection<PhpClass> classes = phpIndex.getClassesByFQN(classFqn);
                final @NotNull Collection<PhpClass> interfaces = phpIndex
                        .getInterfacesByFQN(classFqn);

                Method targetMethod = findTargetMethod(classes, methodName);
                if (targetMethod == null) {
                    targetMethod = findTargetMethod(interfaces, methodName);
                }

                if (targetMethod == null && methodAttribute.getValueElement() != null) {
                    problemsHolder.registerProblem(
                            methodAttribute.getValueElement(),
                            inspectionBundle.message(
                                "inspection.warning.method.does.not.exist",
                                methodName
                            ),
                            ProblemHighlightType.WARNING
                    );

                    return;
                }

                //API method should have public access
                if (targetMethod != null
                        && targetMethod.getAccess() != null
                        && !targetMethod.getAccess().isPublic()) {
                    problemsHolder.registerProblem(
                            methodAttribute,
                            inspectionBundle.message(
                                "inspection.warning.method.should.have.public.access",
                                methodName
                            ),
                            ProblemHighlightType.WARNING,
                            new MethodNotPublicAccessQuickFix(targetMethod)
                    );
                }
            }

            private @Nullable Method findTargetMethod(
                    final Collection<PhpClass> classes,
                    final String methodName
            ) {
                for (final PhpClass phpClass : classes) {
                    for (final Method method : phpClass.getMethods()) {
                        if (method.getName().equals(methodName)) {
                            return method;
                        }
                    }
                }
                return null;
            }
        };
    }
}
