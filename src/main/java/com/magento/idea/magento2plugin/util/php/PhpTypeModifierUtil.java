/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.php;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import com.jetbrains.php.refactoring.extract.extractInterface.PhpExtractInterfaceProcessor;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class PhpTypeModifierUtil {
    
    private static final String INHERIT_DOC_COMMENT_TEXT = "/**\n" 
            + " " + PhpDocUtil.INHERITDOC_TAG + "\n*/";

    private PhpTypeModifierUtil() {}

    /**
     * Add interface to class references (implements).
     *
     * @param classFqn String
     * @param interfaceFqn String
     * @param project Project
     */
    public static void addImplementForPhpClass(
            final @NotNull String classFqn,
            final @NotNull String interfaceFqn,
            final @NotNull Project project
    ) {
        final PhpIndex phpIndex = PhpIndex.getInstance(project);
        final Collection<PhpClass> phpClassesSearchResult = phpIndex.getClassesByFQN(classFqn);
        final Collection<PhpClass> phpInterfacesSearchResult =
                phpIndex.getInterfacesByFQN(interfaceFqn);

        if (phpClassesSearchResult.size() != 1 || phpInterfacesSearchResult.size() != 1) {
            return;
        }
        final PhpClass phpClass = phpClassesSearchResult.iterator().next();

        final PhpPsiElement phpClassScopeHolder =
                PhpCodeInsightUtil.findScopeForUseOperator(phpClass);

        if (phpClassScopeHolder == null) {
            return;
        }
        final PhpClass phpInterface = phpInterfacesSearchResult.iterator().next();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpAliasImporter.insertUseStatement(
                    phpInterface.getPresentableFQN().startsWith("\\")
                            ? phpInterface.getPresentableFQN()
                            : "\\" + phpInterface.getPresentableFQN(),
                    phpClassScopeHolder
            );
            PhpExtractInterfaceProcessor.addImplementClause(
                    project,
                    phpClass,
                    PhpClassGeneratorUtil.getNameFromFqn(phpInterface.getPresentableFQN())
            );
        });
    }

    /**
     * Insert @inheritDoc annotations for the specified methods in the class.
     *
     * @param methods List[String]
     */
    public static void insertInheritDocCommentForMethods(
            final @NotNull List<Method> methods
    ) {
        for (final Method method : methods) {
            final PhpDocComment inheritDocComment = PhpPsiElementFactory.createFromText(
                    method.getProject(),
                    PhpDocComment.class,
                    INHERIT_DOC_COMMENT_TEXT
            );

            if (inheritDocComment == null) {
                return;
            }
            final PhpDocComment methodDoc = method.getDocComment();

            if (methodDoc == null) {
                WriteCommandAction.runWriteCommandAction(method.getProject(), () -> {
                    method.getParent().addBefore(inheritDocComment, method);
                    method.getParent().addAfter(
                            PhpPsiElementFactory.createWhiteSpace(method.getProject()),
                            method.getDocComment()
                    );
                });
            } else {
                WriteCommandAction.runWriteCommandAction(method.getProject(), () -> {
                    methodDoc.replace(inheritDocComment);
                });
            }
        }
    }
}
