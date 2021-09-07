/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.ExtendsList;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeAnalyserVisitor;
import com.magento.idea.magento2uct.bundles.UctInspectionBundle;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class DeprecationInspection extends PhpInspection {

    private final PhpClass phpClass;

    public DeprecationInspection(final PhpClass phpClass) {
        this.phpClass = phpClass;
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpTypeAnalyserVisitor() {

            private final UctInspectionBundle bundle = new UctInspectionBundle();

            @Override
            public void visitPhpClass(final @NotNull PhpClass clazz) {
                PhpClass parentClass = clazz.getSuperClass();

                for (final ClassReference interfaceReference
                        : clazz.getImplementsList().getReferenceElements()) {
                    final String interfaceFqn = interfaceReference.getFQN();

                    if (interfaceFqn == null) {
                        continue;
                    }
                    if (VersionStateManager.getInstance().isDeprecated(interfaceFqn)) {
                        final String message = bundle.message(
                                "customCode.warnings.deprecated.1338",
                                interfaceFqn,
                                clazz.getFQN()
                        );
                        problemsHolder.registerProblem(
                                interfaceReference,
                                message,
                                ProblemHighlightType.WARNING
                        );
                    }
                }

                if (parentClass == null) {
                    return;
                }
                final ExtendsList extendsList = clazz.getExtendsList();
                final String parentClassFqn = parentClass.getFQN();

                while (parentClass != null) {
                    for (final ClassReference parentClassInterfaceReference
                            : parentClass.getImplementsList().getReferenceElements()) {
                        final String parentClassInterfaceFqn =
                                parentClassInterfaceReference.getFQN();

                        if (parentClassInterfaceFqn == null) {
                            continue;
                        }
                        if (VersionStateManager.getInstance()
                                .isDeprecated(parentClassInterfaceFqn)) {
                            final String message = bundle.message(
                                    "customCode.warnings.deprecated.1337",
                                    parentClassInterfaceFqn,
                                    clazz.getFQN()
                            );
                            problemsHolder.registerProblem(
                                    parentClassInterfaceReference,
                                    message,
                                    ProblemHighlightType.WARNING
                            );
                        }
                    }
                    if (VersionStateManager.getInstance().isDeprecated(parentClass.getFQN())) {
                        final String message = bundle.message(
                                "customCode.warnings.deprecated.1131",
                                parentClass.getFQN(),
                                clazz.getFQN()
                        );

                        for (final ClassReference classReference
                                : extendsList.getReferenceElements()) {
                            if (parentClassFqn.equals(classReference.getFQN())) {
                                problemsHolder.registerProblem(
                                        classReference,
                                        message,
                                        ProblemHighlightType.WARNING
                                );
                            }
                        }
                    }
                    parentClass = parentClass.getSuperClass();
                }
            }

            @Override
            public void visitPhpUseList(final PhpUseList useList) {
                for (final PhpUse use : useList.getDeclarations()) {
                    if (VersionStateManager.getInstance().isDeprecated(use.getFQN())) {
                        final PhpReference phpReference = use.getTargetReference();
                        boolean isInterface = false;

                        if (phpReference != null) {
                            final PsiElement element = phpReference.resolve();

                            if (element instanceof PhpClass
                                    && ((PhpClass) element).isInterface()) {
                                isInterface = true;
                            }
                        }
                        String bundleKey = "customCode.warnings.deprecated.1132";

                        if (isInterface) {
                            bundleKey = "customCode.warnings.deprecated.1332";
                        }
                        final String message = bundle.message(
                                bundleKey,
                                use.getFQN(),
                                phpClass.getFQN()
                        );
                        problemsHolder.registerProblem(
                                use,
                                message,
                                ProblemHighlightType.WARNING
                        );
                    }
                }
            }

            @Override
            public void visitPhpClassConstantReference(
                    final ClassConstantReference constantReference
            ) {
                final PsiElement element = constantReference.resolve();

                if (element instanceof ClassConstImpl
                        && VersionStateManager
                        .getInstance().isDeprecated(((ClassConstImpl) element).getFQN())
                ) {
                    final PhpClass containingClass =
                            ((ClassConstImpl) element).getContainingClass();

                    if (containingClass == null) {
                        return;
                    }
                    final String message = bundle.message(
                            "customCode.warnings.deprecated.1234",
                            containingClass.getFQN(),
                            ((ClassConstImpl) element).getName(),
                            phpClass.getFQN()
                    );
                    problemsHolder.registerProblem(
                            constantReference,
                            message,
                            ProblemHighlightType.WARNING
                    );
                }
            }

            /**
             * Implemented php class constant visitor.
             *
             * @param constant ClassConstImpl
             */
            public void visitPhpClassConstant(final ClassConstImpl constant) {
                PhpClass parentClass = phpClass.getSuperClass();
                boolean isFound = false;

                while (parentClass != null && !isFound) {
                    for (final Field field : parentClass.getOwnFields()) {
                        if (field instanceof ClassConstImpl
                                && field.getName().equals(constant.getName())
                                && VersionStateManager.getInstance().isDeprecated(field.getFQN())
                        ) {
                            final String message = bundle.message(
                                    "customCode.warnings.deprecated.1235",
                                    parentClass.getFQN(),
                                    field.getName(),
                                    phpClass.getFQN()
                            );
                            problemsHolder.registerProblem(
                                    constant,
                                    message,
                                    ProblemHighlightType.WARNING
                            );
                            isFound = true;
                        }
                    }
                    parentClass = parentClass.getSuperClass();
                }
            }

            @Override
            public void visitPhpField(final Field field) {
                super.visitPhpField(field);

                if (field instanceof ClassConstImpl) {
                    visitPhpClassConstant((ClassConstImpl) field);
                    return;
                }
                checkIfDeprecatedFieldOverridden(field);
                final PhpType type = this.getType().filterScalarPrimitives();
                final String fieldType = type.toString();

                if (PhpLangUtil.isFqn(fieldType)) {
                    final ClassReference phpReference =
                            PhpPsiElementFactory.createClassReference(
                                    phpClass.getProject(),
                                    fieldType
                            );
                    boolean isInterface = false;
                    final PsiElement element = phpReference.resolve();

                    if (element instanceof PhpClass
                            && ((PhpClass) element).isInterface()) {
                        isInterface = true;
                    }

                    if (VersionStateManager.getInstance().isDeprecated(fieldType)) {
                        String bundleKey = "customCode.warnings.deprecated.1134";

                        if (isInterface) {
                            bundleKey = "customCode.warnings.deprecated.1334";
                        }
                        final String message = bundle.message(
                                bundleKey,
                                fieldType,
                                phpClass.getFQN(),
                                field.getNameIdentifier().getText()
                        );
                        problemsHolder.registerProblem(
                                field,
                                message,
                                ProblemHighlightType.WARNING
                        );
                    }
                }
            }

            /**
             * Check if deprecated field is overridden.
             *
             * @param field PhpClass
             */
            private void checkIfDeprecatedFieldOverridden(final Field field) {
                PhpClass parentClass = phpClass.getSuperClass();
                boolean isFound = false;

                while (parentClass != null && !isFound) {
                    for (final Field parentField : parentClass.getOwnFields()) {
                        if (!(parentField instanceof ClassConstImpl)
                                && parentField.getName().equals(field.getName())
                                && VersionStateManager.getInstance().isDeprecated(
                                        parentField.getFQN())
                        ) {
                            final String message = bundle.message(
                                    "customCode.warnings.deprecated.1535",
                                    parentClass.getFQN(),
                                    parentField.getName(),
                                    phpClass.getFQN()
                            );
                            problemsHolder.registerProblem(
                                    field,
                                    message,
                                    ProblemHighlightType.WARNING
                            );
                            isFound = true;
                        }
                    }
                    parentClass = parentClass.getSuperClass();
                }
            }
        };
    }
}
