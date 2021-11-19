/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2uct.execution.output.ReportBuilder;
import com.magento.idea.magento2uct.execution.output.Summary;
import com.magento.idea.magento2uct.execution.output.UctReportOutputUtil;
import com.magento.idea.magento2uct.execution.process.OutputWrapper;
import com.magento.idea.magento2uct.execution.scanner.ModuleFilesScanner;
import com.magento.idea.magento2uct.execution.scanner.ModuleScanner;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import com.magento.idea.magento2uct.execution.scanner.filter.ExcludeMagentoBundledFilter;
import com.magento.idea.magento2uct.inspections.UctInspectionManager;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.util.inspection.SortDescriptorResultsUtil;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveImports", "PMD.CognitiveComplexity"})
public class GenerateUctReportCommand {

    private final Project project;
    private final OutputWrapper output;
    private final ProcessHandler process;
    private final UctSettingsService settingsService;

    /**
     * Command constructor.
     *
     * @param project Project
     * @param output OutputWrapper
     * @param process ProcessHandler
     */
    public GenerateUctReportCommand(
            final @NotNull Project project,
            final @NotNull OutputWrapper output,
            final @NotNull ProcessHandler process
    ) {
        this.project = project;
        this.output = output;
        this.process = process;
        settingsService = UctSettingsService.getInstance(project);

        this.process.addProcessListener(new ProcessAdapter() {

            @Override
            public void processTerminated(final @NotNull ProcessEvent event) {
                super.processTerminated(event);
                output.write("\nProcess finished with exit code " + event.getExitCode() + "\n");
            }
        });
    }

    /**
     * Execute command.
     */
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidInstantiatingObjectsInLoops"})
    public void execute() {
        output.write("Upgrade compatibility tool\n");
        final PsiDirectory rootDirectory = getTargetPsiDirectory();

        if (rootDirectory == null) {
            output.print(
                    output.wrapCritical("Specified invalid `Path To Analyse` field").concat("\n")
            );
            process.destroyProcess();
            return;
        }
        final ModuleScanner scanner = new ModuleScanner(
                rootDirectory,
                new ExcludeMagentoBundledFilter()
        );
        final Summary summary = new Summary(
                settingsService.getCurrentVersion(),
                settingsService.getTargetVersion()
        );
        final ReportBuilder reportBuilder = new ReportBuilder(project);
        final UctReportOutputUtil outputUtil = new UctReportOutputUtil(output);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                summary.trackProcessStarted();
                for (final ComponentData componentData : scanner) {
                    if (process.isProcessTerminated()) {
                        return;
                    }
                    boolean isModuleHeaderPrinted = false;

                    for (final PsiFile psiFile : new ModuleFilesScanner(componentData)) {
                        if (!(psiFile instanceof PhpFile)) {
                            continue;
                        }
                        final String filename = psiFile.getVirtualFile().getPath();
                        final UctInspectionManager inspectionManager = new UctInspectionManager(
                                project
                        );
                        final UctProblemsHolder fileProblemsHolder = inspectionManager.run(psiFile);

                        if (fileProblemsHolder == null) {
                            continue;
                        }

                        if (fileProblemsHolder.hasResults()) {
                            if (!isModuleHeaderPrinted) {
                                outputUtil.printModuleName(componentData.getName());
                                isModuleHeaderPrinted = true;
                            }
                            outputUtil.printProblemFile(filename);
                        }

                        for (final ProblemDescriptor descriptor
                                : SortDescriptorResultsUtil.sort(
                                        fileProblemsHolder.getResults()
                        )) {
                            final Integer code = fileProblemsHolder.getErrorCodeForDescriptor(
                                    descriptor
                            );
                            if (code != null) {
                                final SupportedIssue issue = SupportedIssue.getByCode(code);

                                if (issue != null) {
                                    final String errorMessage = descriptor
                                            .getDescriptionTemplate()
                                            .substring(6)
                                            .trim();
                                    summary.addToSummary(issue.getLevel());
                                    reportBuilder.addIssue(
                                            descriptor.getLineNumber() + 1,
                                            filename,
                                            errorMessage,
                                            issue
                                    );
                                }
                                outputUtil.printIssue(descriptor, code);
                            }
                        }
                    }
                }
                summary.trackProcessFinished();
                summary.setProcessedModules(scanner.getModuleCount());
                outputUtil.printSummary(summary);

                if (summary.getProcessedModules() == 0) {
                    process.destroyProcess();
                    return;
                }
                reportBuilder.addSummary(summary);
            });

            ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
                final JsonFile report = reportBuilder.build();

                if (report != null) {
                    final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(
                            project
                    );
                    final Document document = psiDocumentManager.getDocument(report);

                    if (document != null) {
                        psiDocumentManager.commitDocument(document);
                    }
                    outputUtil.printReportFile(report.getVirtualFile().getPath());
                }
                process.destroyProcess();
            });
        });
    }

    /**
     * Get target psi directory.
     *
     * @return PsiDirectory
     */
    private @Nullable PsiDirectory getTargetPsiDirectory() {
        final String targetDirPath = settingsService.getModulePath();

        if (targetDirPath == null) {
            return null;
        }
        final VirtualFile targetDirVirtualFile = VfsUtil.findFile(Paths.get(targetDirPath), false);

        if (targetDirVirtualFile == null) {
            return null;
        }

        return PsiManager.getInstance(project).findDirectory(targetDirVirtualFile);
    }
}
