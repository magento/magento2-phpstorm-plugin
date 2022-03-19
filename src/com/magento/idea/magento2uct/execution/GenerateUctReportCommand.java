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
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
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
import com.magento.idea.magento2uct.util.inspection.FilterDescriptorResultsUtil;
import com.magento.idea.magento2uct.util.inspection.SortDescriptorResultsUtil;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveImports", "PMD.CognitiveComplexity"})
public class GenerateUctReportCommand {

    private static final String DEFAULT_MAGENTO_EDITION_LABEL = "Magento Open Source";

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
        final PsiDirectory rootDirectory = getTargetPsiDirectory(settingsService.getModulePath());

        if (rootDirectory == null) {
            output.print(
                    output.wrapCritical("Specified invalid `Path To Analyse` field").concat("\n")
            );
            process.destroyProcess();
            return;
        }
        final List<PsiDirectory> directoriesToScan = new ArrayList<>();
        directoriesToScan.add(rootDirectory);

        if (settingsService.getHasAdditionalPath()) {
            final PsiDirectory additionalDirectory = getTargetPsiDirectory(
                    settingsService.getAdditionalPath()
            );

            if (additionalDirectory != null) {
                directoriesToScan.add(additionalDirectory);
            }
        }

        final ModuleScanner scanner = new ModuleScanner(
                directoriesToScan,
                new ExcludeMagentoBundledFilter()
        );
        final Summary summary = new Summary(
                settingsService.getCurrentVersion(),
                settingsService.getTargetVersion()
        );
        final ReportBuilder reportBuilder = new ReportBuilder(project);
        final UctReportOutputUtil outputUtil = new UctReportOutputUtil(output);
        final Pair<String, String> version = MagentoVersionUtil.getVersionData(
                project,
                project.getBasePath()
        );
        final String resolvedEdition = version.getSecond() == null
                ? DEFAULT_MAGENTO_EDITION_LABEL
                : version.getSecond();

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                summary.trackProcessStarted();
                for (final ComponentData componentData : scanner) {
                    if (process.isProcessTerminated()) {
                        return;
                    }
                    boolean isModuleHeaderPrinted = false;

                    for (final PsiFile psiFile : new ModuleFilesScanner(componentData)) {
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
                                outputUtil.printModuleName(componentData);
                                isModuleHeaderPrinted = true;
                            }
                            outputUtil.printProblemFile(filename);
                        }
                        final List<ProblemDescriptor> problems = SortDescriptorResultsUtil.sort(
                                FilterDescriptorResultsUtil.filter(fileProblemsHolder)
                        );

                        for (final ProblemDescriptor descriptor : problems) {
                            final SupportedIssue issue = fileProblemsHolder.getIssue(descriptor);

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
                            outputUtil.printIssue(descriptor, issue.getCode());
                        }
                    }
                }
                summary.trackProcessFinished();
                summary.setProcessedModules(scanner.getModuleCount());
                summary.setProcessedThemes(scanner.getThemeCount());
                outputUtil.printSummary(summary, resolvedEdition);

                if (summary.getProcessedModules() == 0 && summary.getProcessedThemes() == 0) {
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
     * @param targetDirPath String
     *
     * @return PsiDirectory
     */
    private @Nullable PsiDirectory getTargetPsiDirectory(final String targetDirPath) {
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
