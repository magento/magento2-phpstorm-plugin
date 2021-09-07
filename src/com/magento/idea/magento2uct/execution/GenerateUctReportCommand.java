/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeAnalyserVisitor;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2uct.execution.process.OutputWrapper;
import com.magento.idea.magento2uct.inspections.php.DeprecationInspection;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.magento.idea.magento2uct.inspections.php.deprecation.ExtendingDeprecatedClass;
import com.magento.idea.magento2uct.inspections.php.deprecation.ImportingDeprecatedType;
import com.magento.idea.magento2uct.versioning.processors.DeprecationIndexProcessor;
import com.magento.idea.magento2uct.versioning.scanner.ModuleFilesScanner;
import com.magento.idea.magento2uct.versioning.scanner.ModuleScanner;
import com.magento.idea.magento2uct.versioning.scanner.data.ComponentData;
import org.jetbrains.annotations.NotNull;

public class GenerateUctReportCommand {

    private final Project project;
    private final OutputWrapper output;

    /**
     * Command constructor.
     *
     * @param project Project
     */
    public GenerateUctReportCommand(
            final @NotNull Project project,
            final @NotNull OutputWrapper output
    ) {
        this.project = project;
        this.output = output;
    }

    /**
     * Execute command.
     */
    public void execute() {
        output.write("Upgrade compatibility tool\n\n");

        for (final ComponentData componentData : new ModuleScanner(getTargetPsiDirectory())) {
            boolean isModuleHeaderPrinted = false;

            for (final PsiFile psiFile : new ModuleFilesScanner(componentData)) {
                if (!(psiFile instanceof PhpFile)) {
                    continue;
                }
                final PhpClass phpClass =
                        GetFirstClassOfFile
                                .getInstance()
                                .execute((PhpFile) psiFile);

                if (phpClass != null) {
                    final ExtendingDeprecatedClass extendingDeprecatedClass =
                            new ExtendingDeprecatedClass();
                    final ProblemsHolder extendingDeprecatedClassProblemsHolder = new ProblemsHolder(
                            InspectionManager.getInstance(project),
                            psiFile,
                            false
                    );
                    final PhpElementVisitor extendingDeprecatedClassVisitor =
                            (PhpElementVisitor) extendingDeprecatedClass.buildVisitor(
                                    extendingDeprecatedClassProblemsHolder,
                                    false
                            );
                    extendingDeprecatedClassVisitor.visitPhpClass(phpClass);

                    final ImportingDeprecatedType importingDeprecatedType =
                            new ImportingDeprecatedType();
                    final ProblemsHolder importingDeprecatedTypeProblemsHolder = new ProblemsHolder(
                            InspectionManager.getInstance(project),
                            psiFile,
                            false
                    );
                    final PhpElementVisitor importingDeprecatedTypeVisitor =
                            (PhpElementVisitor) importingDeprecatedType.buildVisitor(
                                    importingDeprecatedTypeProblemsHolder,
                                    false
                            );
                    importingDeprecatedTypeVisitor.visitPhpClass(phpClass);

                    final PhpPsiElement scopeForUseOperator =
                            PhpCodeInsightUtil.findScopeForUseOperator(phpClass);

                    if (scopeForUseOperator != null) {
                        final List<PhpUseList> imports =
                                PhpCodeInsightUtil.collectImports(scopeForUseOperator);

                        for (final PhpUseList phpUseList : imports) {
                            importingDeprecatedTypeVisitor.visitPhpUseList(phpUseList);
                        }
                    }

                    if (extendingDeprecatedClassProblemsHolder.hasResults()
                            || importingDeprecatedTypeProblemsHolder.hasResults()) {
                        if (!isModuleHeaderPrinted) {
                            final String moduleName = "Module Name: ".concat(componentData.getName());
                            output.print("<info>" + moduleName + "</info>\n");
                            output.print("<info>" + "-".repeat(moduleName.length()) + "</info>\n\n");
                            isModuleHeaderPrinted = true;
                        }

                        final String file = "File: " + psiFile.getVirtualFile().getPath();
                        output.print("<info>" + file + "</info>\n");
                        output.print("<info>" + "-".repeat(file.length()) + "</info>\n\n");
                    }

                    // temporary output
                    for (final ProblemDescriptor descriptor
                            : extendingDeprecatedClassProblemsHolder.getResults()) {
                        printErrorMessage(descriptor);
                    }
                    for (final ProblemDescriptor descriptor
                            : importingDeprecatedTypeProblemsHolder.getResults()) {
                        printErrorMessage(descriptor);
                    }
                }
            }
        }
    }

    private void printErrorMessage(final ProblemDescriptor descriptor) {
        final String errorCode =
                descriptor.getDescriptionTemplate().substring(0, 5);
        final String errorMessage =
                descriptor.getDescriptionTemplate().substring(5).trim();

        final String outputText = " * <warning>[WARNING]</warning>"
                + errorCode + " Line "
                + descriptor.getLineNumber() + ": "
                + errorMessage;

        output.print(outputText + "\n");
    }

    /**
     * Visit directory recursively.
     *
     * @param directory PsiDirectory
     */
    private void visitDirectory(final @NotNull PsiDirectory directory) {
        final PsiFile[] files = directory.getFiles();

        for (final PsiFile file : files) {
            if (!(file instanceof PhpFile)) {
                continue;
            }
            final PhpClass phpClass = GetFirstClassOfFile.getInstance().execute((PhpFile) file);

            if (phpClass != null) {
                final DeprecationInspection deprecationInspection =
                        new DeprecationInspection(phpClass);
                final ProblemsHolder problemsHolder = new ProblemsHolder(
                        InspectionManager.getInstance(project),
                        phpClass.getContainingFile(),
                        false
                );
                final PhpTypeAnalyserVisitor visitor =
                        (PhpTypeAnalyserVisitor) deprecationInspection.buildVisitor(
                                problemsHolder,
                                false
                        );
                visitor.visitPhpClass(phpClass);

                final PhpPsiElement scopeForUseOperator =
                        PhpCodeInsightUtil.findScopeForUseOperator(phpClass);

                if (scopeForUseOperator != null) {
                    final List<PhpUseList> imports =
                            PhpCodeInsightUtil.collectImports(scopeForUseOperator);

                    for (final PhpUseList phpUseList : imports) {
                        visitor.visitPhpUseList(phpUseList);
                    }
                }

                final List<ClassConstantReference> classConstantReferences =
                        new ArrayList<>(
                                PsiTreeUtil.findChildrenOfType(
                                        phpClass,
                                        ClassConstantReference.class
                                )
                        );
                for (final ClassConstantReference constantReference : classConstantReferences) {
                    visitor.visitPhpClassConstantReference(constantReference);
                }

                for (final Field field : phpClass.getOwnFields()) {
                    visitor.visitPhpField(field);
                }

                // temporary output
                for (final ProblemDescriptor descriptor : problemsHolder.getResults()) {
                    output.print(descriptor.getDescriptionTemplate());
                }
            }
        }

        for (final PsiDirectory subDirectory : directory.getSubdirectories()) {
            visitDirectory(subDirectory);
        }
    }

    /**
     * Get target psi directory.
     *
     * @return PsiDirectory
     */
    private PsiDirectory getTargetPsiDirectory() {
        final String targetDirPath = project.getBasePath() + File.separator + "CE" + File.separator + "app" + File.separator + "code" + File.separator + "Foo";
        final VirtualFile targetDirVirtualFile = VfsUtil.findFile(Paths.get(targetDirPath), false);

        if (targetDirVirtualFile == null) {
            return null;
        }

        return PsiManager
                .getInstance(project)
                .findDirectory(targetDirVirtualFile);
    }
}
