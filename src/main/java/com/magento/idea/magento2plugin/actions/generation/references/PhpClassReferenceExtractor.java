/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.references;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReturnType;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstantReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.ParameterImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public abstract class PhpClassReferenceExtractor {

    /**
     * Visitor that collects references from the class elements.
     */
    private final PhpElementVisitor myCollectorVisitor = new PhpElementVisitor() {

        /**
         * Visit class processor.
         *
         * @param phpClass PhpClass
         */
        @Override
        public void visitPhpClass(final PhpClass phpClass) {
            PhpClassReferenceExtractor.this.processClassReference(phpClass);
        }

        /**
         * Visit parameter processor.
         *
         * @param parameter Parameter
         */
        @Override
        public void visitPhpParameter(final Parameter parameter) {
            PhpClassReferenceExtractor.this.processParameterReference(parameter);
            PhpClassReferenceExtractor.this.processUsedForDefaultValueTypeReference(parameter);
        }

        /**
         * Visit return type processor.
         *
         * @param returnType PhpReturnType
         */
        @Override
        public void visitPhpReturnType(final PhpReturnType returnType) {
            PhpClassReferenceExtractor.this.processReturnTypeReference(returnType);
        }
    };

    /**
     * Process class reference.
     *
     * @param resolvedClass PhpClass
     */
    public void processClassReference(final @NotNull PhpClass resolvedClass) {
        final String name = resolvedClass.getName();
        this.processReference(name, resolvedClass.getFQN(), resolvedClass);
    }

    /**
     * Process reference for non primitive parameter type.
     *
     * @param parameter Parameter
     */
    public void processParameterReference(final @NotNull Parameter parameter) {
        final PhpType parameterType = parameter.getDeclaredType();
        final String complexType = extractComplexType(parameterType);

        if (parameterType.isEmpty() || complexType == null) {
            return;
        }
        this.processReference(getNameFromFqn(complexType), complexType, parameter);
    }

    /**
     * Process reference for complex type if it is exists in the default parameter value.
     *
     * @param parameter Parameter
     */
    public void processUsedForDefaultValueTypeReference(final @NotNull Parameter parameter) {
        if (!(parameter instanceof ParameterImpl)) {
            return;
        }
        final PsiElement defaultValue = parameter.getDefaultValue();

        if (!(defaultValue instanceof ClassConstantReferenceImpl)) {
            return;
        }
        final PsiElement constant = ((ClassConstantReferenceImpl) defaultValue).resolve();

        if (!(constant instanceof ClassConstImpl)) {
            return;
        }
        final PhpClass usedTypeClass = ((ClassConstImpl) constant).getContainingClass();

        if (usedTypeClass == null) {
            return;
        }
        final String complexType = extractComplexType(usedTypeClass.getType());

        if (complexType == null) {
            return;
        }
        
        this.processReference(getNameFromFqn(complexType), complexType, parameter);
    }

    /**
     * Process reference for non primitive return type.
     *
     * @param returnType PhpReturnType
     */
    public void processReturnTypeReference(final @NotNull PhpReturnType returnType) {
        final PhpType returnPhpType = returnType.getDeclaredType();
        final String complexType = extractComplexType(returnPhpType);

        if (returnPhpType.isEmpty() || complexType == null) {
            return;
        }
        this.processReference(getNameFromFqn(complexType), complexType, returnType);
    }

    /**
     * Extract complex type.
     *
     * @param type PhpType
     *
     * @return String
     */
    private String extractComplexType(final @NotNull PhpType type) {
        String complexType = type.toString();

        for (final String complexTypeCandidate : type.getTypes()) {
            if (!PhpType.isPrimitiveType(complexTypeCandidate)) {
                complexType = complexTypeCandidate;
            }
        }

        return PhpType.isPrimitiveType(complexType) ? null : complexType;
    }

    /**
     * Get name from FQN string.
     *
     * @param fqn String
     *
     * @return String
     */
    private String getNameFromFqn(final String fqn) {
        final String[] fqnArray = fqn.split("\\\\");

        return fqnArray[fqnArray.length - 1];
    }

    /**
     * Process type reference.
     *
     * @param name String
     * @param fqn String
     * @param identifier PsiElement
     */
    protected abstract void processReference(
            @NotNull String name,
            @NotNull String fqn,
            @NotNull PsiElement identifier
    );

    /**
     * Run visitor on an element.
     *
     * @param element PsiElement
     */
    public void processElement(final @NotNull PsiElement element) {
        element.accept(this.myCollectorVisitor);
    }

    /**
     * Visit elements to collect complex types references.
     *
     * @param originalElements Collection
     */
    public void processElements(final @NotNull Collection<? extends PsiElement> originalElements) {
        for (final PsiElement element : originalElements) {
            this.processElement(element);
        }
    }
}
