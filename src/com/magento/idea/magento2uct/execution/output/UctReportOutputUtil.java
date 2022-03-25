/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.output;

import com.intellij.codeInspection.ProblemDescriptor;
import com.magento.idea.magento2uct.bundles.UctInspectionBundle;
import com.magento.idea.magento2uct.execution.process.OutputWrapper;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class UctReportOutputUtil {

    private static final String ISSUE_FORMAT = " * {SEVERITY}[{code}] Line {line}: {message}";
    private static final String DETAILED_ISSUES_LIST = "inspection.issues.description.link";
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
     * @param componentData ComponentData
     */
    public void printModuleName(final @NotNull ComponentData componentData) {
        final String componentType = componentData.getType().toString();
        final String componentTypeFormatted = componentType
                .substring(0, 1)
                .toUpperCase(new Locale("en","EN"))
                .concat(componentType.substring(1));

        final String moduleNameLine = componentTypeFormatted
                .concat(" Name: ")
                .concat(componentData.getName());

        stdout.print("\n\n" + stdout.wrapInfo(moduleNameLine).concat("\n"));
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
        final SupportedIssue issue = SupportedIssue.getByCode(code);

        if (issue == null) {
            return;
        }
        final String errorMessage = descriptor.getDescriptionTemplate().substring(6).trim();

        final String output = ISSUE_FORMAT
                .replace("{SEVERITY}", issue.getLevel().getFormattedLabel())
                .replace("{code}", Integer.toString(code))
                .replace("{line}", Integer.toString(descriptor.getLineNumber() + 1))
                .replace("{message}", errorMessage)
                .concat("\n");

        stdout.print(output);
    }

    /**
     * Print summary information.
     *
     * @param summary Summary
     * @param platformName String
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.CyclomaticComplexity"})
    public void printSummary(final Summary summary, final String platformName) {
        if (summary.getProcessedModules() == 0 && summary.getProcessedThemes() == 0) {
            stdout.print(stdout.wrapInfo("Couldn't find modules to analyse").concat("\n"));
            return;
        }
        if (!summary.hasProblems()) {
            stdout.print("\n" + stdout.wrapInfo("No problems found").concat("\n"));
        }
        printNavigateToDetailedErrorsLink();

        final Map<String, String> summaryMap = new LinkedHashMap<>();
        summaryMap.put("Installed version", summary.getInstalledVersion());
        summaryMap.put(platformName + " version", summary.getTargetVersion());
        summaryMap.put("Running time", summary.getProcessRunningTime());
        summaryMap.put("Checked modules", String.valueOf(summary.getProcessedModules()));
        summaryMap.put("Checked themes", String.valueOf(summary.getProcessedThemes()));
        summaryMap.put("Total warnings found", String.valueOf(summary.getPhpWarnings()));
        summaryMap.put("Total errors found", String.valueOf(summary.getPhpErrors()));
        summaryMap.put("Total critical errors found",
                String.valueOf(summary.getPhpCriticalErrors()));
        summaryMap.put("Complexity score", String.valueOf(summary.getComplexityScore()));

        int longestKey = 0;

        for (final String key : summaryMap.keySet()) {
            if (key.length() > longestKey) {
                longestKey = key.length();
            }
        }

        int longestValue = 0;

        for (final String value : summaryMap.values()) {
            if (value.length() > longestValue) {
                longestValue = value.length();
            }
        }
        printSummarySeparator(longestKey + 2, longestValue + 2);

        for (final Map.Entry<String, String> summaryEntry : summaryMap.entrySet()) {
            String header = summaryEntry.getKey();
            final String value = "  " + summaryEntry.getValue();

            if (header.length() < longestKey) {
                header += " ".repeat(longestKey - header.length());//NOPMD
            }
            stdout.print(" " + stdout.wrapSummary(header).concat("  ").concat(value).concat("\n"));
        }
        printSummarySeparator(longestKey + 2, longestValue + 2);
    }

    /**
     * Print report file.
     *
     * @param filename String
     */
    public void printReportFile(final @NotNull String filename) {
        stdout.print("\n" + stdout.wrapInfo("Result exported to '" + filename + "'") + "\n");
    }

    /**
     * Print summary separator line.
     *
     * @param headerLength int
     * @param valueColumnLength int
     */
    private void printSummarySeparator(final int headerLength, final int valueColumnLength) {
        final String headerSeparator = "-".repeat(headerLength);
        final String valueSeparator = "-".repeat(valueColumnLength);

        stdout.write(headerSeparator.concat("  ").concat(valueSeparator).concat("\n"));
    }

    /**
     * Print navigate to detailed issues description link message.
     */
    private void printNavigateToDetailedErrorsLink() {
        final String detailedIssuesListMessage =
                stdout.wrapInfo(new UctInspectionBundle().message(DETAILED_ISSUES_LIST));

        stdout.print("\n" + detailedIssuesListMessage.concat("\n"));
    }
}
