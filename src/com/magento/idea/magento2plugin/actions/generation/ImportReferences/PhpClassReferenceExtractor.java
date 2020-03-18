/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.ImportReferences;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public abstract class PhpClassReferenceExtractor {
    private final PhpElementVisitor myCollectorVisitor = new PhpElementVisitor() {
        public void visitPhpClass(PhpClass phpClass) {
            PhpClassReferenceExtractor.this.processClassReference(phpClass);
        }

        public void visitPhpParameter(Parameter parameter) {
            PhpClassReferenceExtractor.this.processParameterReference(parameter);
        }

        public void visitPhpReturnType(PhpReturnType returnType) {
            PhpClassReferenceExtractor.this.processReturnTypeReference(returnType);
        }
    };

    public PhpClassReferenceExtractor() {
    }

    private void processClassReference(@NotNull PhpClass resolvedClass) {
        String name = resolvedClass.getName();
        this.processReference(name, resolvedClass.getFQN(), resolvedClass);
    }

    private void processParameterReference(@NotNull Parameter parameter) {
        PhpType parameterType = parameter.getDeclaredType();
        if (parameterType.isEmpty() && !PhpType.isPrimitiveType(parameterType.toString())) {
            return;
        }
        String fqn = parameterType.toString();
        String name = getNameFromFqn(parameterType);
        this.processReference(name, fqn, parameter);
    }

    public void processReturnTypeReference(PhpReturnType returnType) {
        PhpType parameterType = returnType.getDeclaredType();
        if (parameterType.isEmpty() && !PhpType.isPrimitiveType(parameterType.toString())) {
            return;
        }
        String fqn = parameterType.toString();
        String name = getNameFromFqn(parameterType);
        this.processReference(name, fqn, returnType);
    }

    private String getNameFromFqn(PhpType parameterType) {
        String[] fqnArray = parameterType.toString().split("\\\\");
        return fqnArray[fqnArray.length - 1];
    }

    protected abstract void processReference(@NotNull String name, @NotNull String fqn, @NotNull PsiElement identifier);

    public void processElement(@NotNull PsiElement element) {
        element.accept(this.myCollectorVisitor);
    }

    public void processElements(@NotNull Collection<? extends PsiElement> originalElements) {
        for (PsiElement element : originalElements) {
            this.processElement(element);
        }
    }
}
