/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.output;

import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import org.jetbrains.annotations.Nullable;

public class Summary {

    private static final int WARNING_COMPLEXITY = 1;
    private static final int ERROR_COMPLEXITY = 2;
    private static final int CRITICAL_COMPLEXITY = 3;

    private final SupportedVersion installedVersion;
    private final SupportedVersion targetVersion;
    private int processedModules;
    private int processedThemes;
    private long processStartedTime;
    private long processEndedTime;
    private int phpWarnings;
    private int phpErrors;
    private int phpCriticalErrors;

    /**
     * Summary.
     *
     * @param installedVersion SupportedVersion
     * @param targetVersion SupportedVersion
     */
    public Summary(
            final @Nullable SupportedVersion installedVersion,
            final @Nullable SupportedVersion targetVersion
    ) {
        this.installedVersion = installedVersion;
        this.targetVersion = targetVersion;
    }

    /**
     * Get installed version.
     *
     * @return String
     */
    public String getInstalledVersion() {
        return installedVersion == null ? "Less than 2.3.0" : installedVersion.getVersion();
    }

    /**
     * Get target version.
     *
     * @return String
     */
    public String getTargetVersion() {
        return targetVersion == null ? "" : targetVersion.getVersion();
    }

    /**
     * Get processed modules qty.
     *
     * @return int
     */
    public int getProcessedModules() {
        return processedModules;
    }

    /**
     * Set processed modules qty.
     *
     * @param processedModules int
     */
    public void setProcessedModules(final int processedModules) {
        this.processedModules = processedModules;
    }

    /**
     * Get processed theme qty.
     *
     * @return int
     */
    public int getProcessedThemes() {
        return processedThemes;
    }

    /**
     * Set processed theme qty.
     *
     * @param processedThemes int
     */
    public void setProcessedThemes(final int processedThemes) {
        this.processedThemes = processedThemes;
    }

    /**
     * Track time before process is started.
     */
    public void trackProcessStarted() {
        processStartedTime = System.currentTimeMillis();
    }

    /**
     * Track time after process is finished.
     */
    public void trackProcessFinished() {
        processEndedTime = System.currentTimeMillis();
    }

    /**
     * Get process running time.
     *
     * @return String
     */
    public String getProcessRunningTime() {
        if (processStartedTime == 0L || processEndedTime == 0L) {
            return "0m:0s";
        }
        final long duration = (processEndedTime - processStartedTime) / 1000;
        final int minutes = (int) (duration / 60);
        final int reminder = (int) (duration % 60);

        return ":minutesm::secondss"
                .replace(":minutes", String.valueOf(minutes))
                .replace(":seconds", String.valueOf(reminder));
    }

    /**
     * Add qty based on issue severity level.
     *
     * @param level IssueSeverityLevel
     */
    public void addToSummary(final IssueSeverityLevel level) {
        switch (level) {
            case WARNING:
                phpWarnings++;
                break;
            case ERROR:
                phpErrors++;
                break;
            case CRITICAL:
                phpCriticalErrors++;
                break;
            default:
                break;
        }
    }

    /**
     * Check if any problem found.
     *
     * @return boolean
     */
    public boolean hasProblems() {
        return (phpWarnings + phpErrors + phpCriticalErrors) > 0;
    }

    /**
     * Get php warnings.
     *
     * @return int
     */
    public int getPhpWarnings() {
        return phpWarnings;
    }

    /**
     * Get php errors.
     *
     * @return int
     */
    public int getPhpErrors() {
        return phpErrors;
    }

    /**
     * Get php critical errors.
     *
     * @return int
     */
    public int getPhpCriticalErrors() {
        return phpCriticalErrors;
    }

    /**
     * Get complexity score.
     *
     * @return int
     */
    public int getComplexityScore() {
        return phpWarnings * WARNING_COMPLEXITY
                + phpErrors * ERROR_COMPLEXITY
                + phpCriticalErrors * CRITICAL_COMPLEXITY;
    }
}
