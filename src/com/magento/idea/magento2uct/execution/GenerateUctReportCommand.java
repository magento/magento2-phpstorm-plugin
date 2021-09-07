/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
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
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2uct.execution.process.OutputWrapper;
import com.magento.idea.magento2uct.execution.scanner.ModuleFilesScanner;
import com.magento.idea.magento2uct.execution.scanner.ModuleScanner;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import com.magento.idea.magento2uct.inspections.UctInspectionManager;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.DeprecationInspection;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
        final UctInspectionManager inspectionManager = new UctInspectionManager(project);
        final UctReportOutputUtil outputUtil = new UctReportOutputUtil(output);

        for (final ComponentData componentData : new ModuleScanner(getTargetPsiDirectory())) {
            boolean isModuleHeaderPrinted = false;

            for (final PsiFile psiFile : new ModuleFilesScanner(componentData)) {
                if (!(psiFile instanceof PhpFile)) {
                    continue;
                }
                final UctProblemsHolder fileProblemsHolder = inspectionManager.run(psiFile);

                if (fileProblemsHolder == null) {
                    continue;
                }

                if (fileProblemsHolder.hasResults()) {
                    if (!isModuleHeaderPrinted) {
                        outputUtil.printModuleName(componentData.getName());
                        isModuleHeaderPrinted = true;
                    }
                    outputUtil.printProblemFile(psiFile.getVirtualFile().getPath());
                }

                for (final ProblemDescriptor descriptor : fileProblemsHolder.getResults()) {
                    final Integer code = fileProblemsHolder.getErrorCodeForDescriptor(descriptor);

                    if (code != null) {
                        outputUtil.printIssue(descriptor, code);
                    }
                }
            }
        }
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
                final UctProblemsHolder problemsHolder = new UctProblemsHolder(
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

    private static final class UctReportOutputUtil {

        private static final String ISSUE_FORMAT = " * {SEVERITY}[{code}] Line {line}: {message}";
        private final OutputWrapper stdout;

        /**
         * UCT report styled output util.
         *
         * @param output OutputWrapper
         */
        public UctReportOutputUtil(final @NotNull OutputWrapper output) {
            stdout = output;
        }

        /**
         * Print module name header.
         *
         * @param moduleName String
         */
        public void printModuleName(final @NotNull String moduleName) {
            final String moduleNameLine = "Module Name: ".concat(moduleName);
            stdout.print(stdout.wrapInfo(moduleNameLine).concat("\n"));
            stdout.print(stdout.wrapInfo("-".repeat(moduleNameLine.length())).concat("\n"));
        }

        /**
         * Print problem file header.
         *
         * @param filePath String
         */
        public void printProblemFile(final @NotNull String filePath) {
            final String file = "File: ".concat(filePath);
            stdout.print("\n".concat(stdout.wrapInfo(file)).concat("\n"));
            stdout.print(stdout.wrapInfo("-".repeat(file.length())).concat("\n\n"));
        }

        /**
         * Print issue message.
         *
         * @param descriptor ProblemDescriptor
         * @param code int
         */
        public void printIssue(final @NotNull ProblemDescriptor descriptor, final int code) {
            final String errorMessage = descriptor.getDescriptionTemplate().substring(6).trim();
            final SupportedIssue issue = SupportedIssue.getByCode(code);

            if (issue == null) {
                return;
            }

            final String output = ISSUE_FORMAT
                    .replace("{SEVERITY}", issue.getLevel().getFormattedLabel())
                    .replace("{code}", Integer.toString(code))
                    .replace("{line}", Integer.toString(descriptor.getLineNumber() + 1))
                    .replace("{message}", errorMessage)
                    .concat("\n");

            stdout.print(output);
        }
    }
}
