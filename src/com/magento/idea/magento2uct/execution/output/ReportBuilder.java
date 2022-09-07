/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.output;

import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonPsiUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

public class ReportBuilder {

    private static final String REPORT_DIRECTORY = ".idea" + File.separator + "uctReports";
    private static final String REPORT_FILENAME_SUFFIX = "-results.json";
    private static final String BASE_FILE_CONTENT = "{}";

    private final Project project;
    private final Report report;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "dd-MMM-yyyy-HH-mm-ss",
            Locale.US
    );

    /**
     * Report builder.
     *
     * @param project Project
     */
    public ReportBuilder(final @NotNull Project project) {
        this.project = project;
        report = new Report();
    }

    /**
     * Add issue to report.
     *
     * @param line int
     * @param filename String
     * @param message String
     * @param issue SupportedIssue
     */
    public void addIssue(
            final int line,
            final String filename,
            final String message,
            final SupportedIssue issue
    ) {
        report.addIssue(line, filename, message, issue);
    }

    /**
     * Add summary to report.
     *
     * @param summary Summary
     */
    public void addSummary(final Summary summary) {
        report.addSummary(summary);
    }

    /**
     * Build report file.
     *
     * @return JsonFile
     */
    @SuppressWarnings({
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.CognitiveComplexity",
    })
    public JsonFile build() {
        if (report.getIssues().isEmpty() || report.getSummary() == null) {
            return null;
        }
        final Path reportDirPath = Path.of(
                project.getBasePath() + File.separator + REPORT_DIRECTORY
        );

        try {
            if (!Files.exists(reportDirPath)) {
                Files.createDirectories(reportDirPath);
            }
        } catch (IOException exception) {
            return null;
        }
        final VirtualFile ideaVfs = VfsUtil.findFile(reportDirPath, true);

        if (ideaVfs == null) {
            return null;
        }
        final PsiDirectory ideaDirectory = PsiManager.getInstance(project).findDirectory(ideaVfs);

        if (ideaDirectory == null) {
            return null;
        }
        final JsonFile reportFile = (JsonFile) PsiFileFactory.getInstance(project)
                .createFileFromText(
                        generateFilename(),
                        JsonLanguage.INSTANCE,
                        BASE_FILE_CONTENT,
                        true,
                        false
                );

        if (reportFile == null) {
            return null;
        }
        final List<JsonFile> savedFile = new ArrayList<>();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            savedFile.add((JsonFile) ideaDirectory.add(reportFile));

            if (savedFile.isEmpty()) {
                return;
            }
            final JsonObject topNode = (JsonObject) savedFile.get(0).getTopLevelValue();

            if (topNode == null) {
                return;
            }
            final JsonElementGenerator jsonElementGenerator = new JsonElementGenerator(project);
            final StringBuilder issuesValueBuilder = new StringBuilder();

            for (final Issue issue : report.getIssues()) {
                final JsonObject issueObject = jsonElementGenerator.createObject("\"lineNumber\": "
                        + issue.getLine() + ","
                        + "\"level\": \"" + issue.getLevel() + "\","//NOPMD
                        + "\"message\": \"" + JSONObject.escape(issue.getMessage()) + "\","
                        + "\"code\": \"" + issue.getCode() + "\","
                        + "\"fileName\": \"" + JSONObject.escape(issue.getFilename()) + "\","
                        + "\"validationType\": \"" + issue.getValidationType() + "\""
                );
                if (issuesValueBuilder.length() > 0) {
                    issuesValueBuilder.append(',');
                }
                issuesValueBuilder.append(issueObject.getText());
            }
            final Summary summary = report.getSummary();
            final JsonObject summaryObject = jsonElementGenerator.createObject("\""
                    + "installedVersion\": \"" + summary.getInstalledVersion() + "\","
                    + "\"AdobeCommerceVersion\": \"" + summary.getTargetVersion() + "\","
                    + "\"checkedModules\": " + summary.getProcessedModules() + ","
                    + "\"checkedThemes\": " + summary.getProcessedThemes() + ","
                    + "\"runningTime\": \"" + summary.getProcessRunningTime() + "\","
                    + "\"totalWarnings\": " + summary.getPhpWarnings() + ","
                    + "\"totalErrors\": " + summary.getPhpErrors() + ","
                    + "\"totalCriticalErrors\": " + summary.getPhpCriticalErrors() + ","
                    + "\"complexityScore\": " + summary.getComplexityScore()
            );
            JsonPsiUtil.addProperty(
                    topNode,
                    jsonElementGenerator.createProperty(
                            "issues",
                            "[" + issuesValueBuilder + "]"
                    ),
                    false
            );
            JsonPsiUtil.addProperty(
                    topNode,
                    jsonElementGenerator.createProperty("stats", summaryObject.getText()),
                    false
            );
        });

        return savedFile.isEmpty() ? reportFile : savedFile.get(0);
    }

    /**
     * Generate report filename.
     *
     * @return String
     */
    private String generateFilename() {
        return formatter.format(LocalDateTime.now()) + REPORT_FILENAME_SUFFIX;
    }

    private static final class Report {

        private final List<Issue> issues = new LinkedList<>();
        private Summary summary;

        /**
         * Add issue to report.
         *
         * @param line int
         * @param filename String
         * @param message String
         * @param issue SupportedIssue
         */
        public void addIssue(
                final int line,
                final String filename,
                final String message,
                final SupportedIssue issue
        ) {
            issues.add(new Issue(line, filename, message, issue));
        }

        /**
         * Add summary to report.
         *
         * @param summary Summary
         */
        public void addSummary(final Summary summary) {
            this.summary = summary;
        }

        /**
         * Get reported issues.
         *
         * @return List[Issue]
         */
        public List<Issue> getIssues() {
            return new LinkedList<>(issues);
        }

        /**
         * Get report summary.
         *
         * @return Summary
         */
        public Summary getSummary() {
            return summary;
        }
    }

    private static final class Issue {

        private static final String VALIDATION_TYPE = "php";

        private final int line;
        private final IssueSeverityLevel level;
        private final String message;
        private final int code;
        private final String filename;

        /**
         * Issue data.
         *
         * @param line int
         * @param filename String
         * @param message String
         * @param issue SupportedIssue
         */
        public Issue(
                final int line,
                final String filename,
                final String message,
                final SupportedIssue issue
        ) {
            this.line = line;
            this.filename = filename;
            this.message = message;
            level = issue.getLevel();
            code = issue.getCode();
        }

        /**
         * Get validation type.
         *
         * @return String
         */
        public String getValidationType() {
            return VALIDATION_TYPE;
        }

        /**
         * Get line number.
         *
         * @return int
         */
        public int getLine() {
            return line;
        }

        /**
         * Get severity level.
         *
         * @return String
         */
        public String getLevel() {
            return level.getLabel();
        }

        /**
         * Get error message.
         *
         * @return String
         */
        public String getMessage() {
            return message;
        }

        /**
         * Get error code.
         *
         * @return int
         */
        public int getCode() {
            return code;
        }

        /**
         * Get filename where issue was occurred.
         *
         * @return String
         */
        public String getFilename() {
            return filename;
        }
    }
}
