/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UctInspectionManager {

    private final Project project;

    /**
     * UCT inspection manager.
     *
     * @param project Project
     */
    public UctInspectionManager(final @NotNull Project project) {
        this.project = project;
    }

    /**
     * Execute all UCT inspections for the specified file.
     *
     * @param psiFile PsiFile
     *
     * @return ProblemsHolder
     */
    public @Nullable UctProblemsHolder run(final PsiFile psiFile) {
        if (!(psiFile instanceof PhpFile)) {
            return null;
        }
        final PhpClass phpClass = GetFirstClassOfFile.getInstance().execute((PhpFile) psiFile);

        if (phpClass == null) {
            return null;
        }
        final UctProblemsHolder problemsHolder = new UctProblemsHolder(
                InspectionManager.getInstance(project),
                psiFile,
                false
        );

        for (final PsiElement element : collectElements(phpClass)) {
            for (final PsiElementVisitor visitor : SupportedIssue.getVisitors(problemsHolder)) {
                element.accept(visitor);
            }
        }

        return problemsHolder;
    }

    /**
     * Collect elements for PHP based inspections.
     *
     * @param phpClass PhpClass
     *
     * @return List[PsiElement]
     */
    private List<PsiElement> collectElements(final @NotNull PhpClass phpClass) {
        final List<PsiElement> elements = new LinkedList<>();
        elements.add(phpClass);

        final PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(
                phpClass
        );
        if (scopeForUseOperator != null) {
            elements.addAll(PhpCodeInsightUtil.collectImports(scopeForUseOperator));
        }

        return elements;
    }
}
