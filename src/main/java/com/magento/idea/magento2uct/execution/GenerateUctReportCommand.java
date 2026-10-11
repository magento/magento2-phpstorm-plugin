/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import com.magento.idea.magento2uct.analysis.UctAnalysisRequest;
import com.magento.idea.magento2uct.analysis.UctAnalysisResult;
import com.magento.idea.magento2uct.analysis.UctAnalysisService;
import com.magento.idea.magento2uct.analysis.UctFinding;
import com.magento.idea.magento2uct.execution.output.ReportBuilder;
import com.magento.idea.magento2uct.execution.output.Summary;
import com.magento.idea.magento2uct.execution.output.UctReportOutputUtil;
import com.magento.idea.magento2uct.execution.process.OutputWrapper;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

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

        this.process.addProcessListener(new ProcessListener() {

            @Override
            public void processTerminated(final @NotNull ProcessEvent event) {
                output.write("\nProcess finished with exit code " + event.getExitCode() + "\n");
            }
        });
    }

    /**
     * Execute command.
     */
    public void execute() {
        output.write("Upgrade compatibility tool\n");
        final String modulePath = settingsService.getModulePath();
        if (modulePath == null || modulePath.isBlank()) {
            output.print(output.wrapCritical("Specified invalid `Path To Analyse` field") + "\n");
            process.destroyProcess();
            return;
        }
        final List<String> paths = new ArrayList<>();
        paths.add(modulePath);
        if (settingsService.getHasAdditionalPath() && settingsService.getAdditionalPath() != null) {
            paths.add(settingsService.getAdditionalPath());
        }
        final String current = settingsService.getConfiguredCurrentVersion();
        final String target = settingsService.getConfiguredTargetVersion();
        if (target == null) {
            output.print(output.wrapCritical("Configure a target version before analysis.") + "\n");
            process.destroyProcess();
            return;
        }
        final IssueSeverityLevel severity = settingsService.getMinIssueLevel();
        final UctAnalysisRequest request = new UctAnalysisRequest(
                List.copyOf(paths), current, target,
                severity == null ? IssueSeverityLevel.WARNING : severity,
                Boolean.TRUE.equals(settingsService.shouldIgnoreCurrentVersion())
        );
        final Summary summary = Summary.forVersions(current, target);
        final UctReportOutputUtil outputUtil = new UctReportOutputUtil(output);
        final EmptyProgressIndicator indicator = new EmptyProgressIndicator();
        process.addProcessListener(new ProcessListener() {
            @Override
            public void processTerminated(final @NotNull ProcessEvent event) {
                indicator.cancel();
            }
        });
        if (process.isProcessTerminated()) {
            indicator.cancel();
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                summary.trackProcessStarted();
                final UctAnalysisResult result = ProgressManager.getInstance().runProcess(
                        () -> new UctAnalysisService(project).analyze(request, indicator::checkCanceled, progress -> { }),
                        indicator
                );
                indicator.checkCanceled();
                summary.trackProcessFinished();
                summary.setProcessedModules(result.getModules());
                summary.setProcessedThemes(result.getThemes());
                final ReportBuilder reportBuilder = new ReportBuilder(project);
                String previousFile = null;
                for (final UctFinding finding : result.getFindings()) {
                    if (!finding.getFilePath().equals(previousFile)) {
                        outputUtil.printProblemFile(finding.getFilePath());
                        previousFile = finding.getFilePath();
                    }
                    summary.addToSummary(finding.getIssue().getLevel());
                    reportBuilder.addIssue(finding.getLine(), finding.getFilePath(), finding.getMessage(), finding.getIssue());
                    outputUtil.printIssue(finding);
                }
                final Pair<String, String> version = ApplicationManager.getApplication().runReadAction(
                        (Computable<Pair<String, String>>) () -> MagentoVersionUtil.getVersionData(project, project.getBasePath())
                );
                outputUtil.printSummary(summary, version.getSecond() == null ? DEFAULT_MAGENTO_EDITION_LABEL : version.getSecond());
                reportBuilder.addSummary(summary);
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (project.isDisposed() || process.isProcessTerminated()) {
                        return;
                    }
                    try {
                        final JsonFile report = reportBuilder.build();
                        if (report != null) {
                            final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
                            final Document document = documentManager.getDocument(report);
                            if (document != null) {
                                documentManager.commitDocument(document);
                            }
                            outputUtil.printReportFile(report.getVirtualFile().getPath());
                        }
                    } finally {
                        process.destroyProcess();
                    }
                });
            } catch (ProcessCanceledException ignored) {
                process.destroyProcess();
            } catch (Exception exception) {
                output.print(output.wrapCritical("Analysis failed: " + exception.getMessage()) + "\n");
                process.destroyProcess();
            }
        });
    }
}
