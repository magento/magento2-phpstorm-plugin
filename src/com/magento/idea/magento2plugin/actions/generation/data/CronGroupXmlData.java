/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class CronGroupXmlData {
    private final String module;
    private final String groupName;
    private final Integer scheduleGenerateEvery;
    private final Integer scheduleAheadFor;
    private final Integer scheduleLifetime;
    private final Integer historyCleanupEvery;
    private final Integer historySuccessLifetime;
    private final Integer historyFailureLifetime;
    private final Integer useSeparateProcess;

    /**
     * Cron group XML data constructor.
     *
     * @param module Module name
     * @param groupName CRON group name
     * @param scheduleGenerateEvery Schedule generate every
     * @param scheduleAheadFor Schedule ahead for
     * @param scheduleLifetime Schedule lifetime
     * @param historyCleanupEvery History cleanup every
     * @param historySuccessLifetime History success lifetime
     * @param historyFailureLifetime History failure lifetime
     * @param useSeparateProcess Use separate process
     */
    public CronGroupXmlData(
            final String module,
            final String groupName,
            final Integer scheduleGenerateEvery,
            final Integer scheduleAheadFor,
            final Integer scheduleLifetime,
            final Integer historyCleanupEvery,
            final Integer historySuccessLifetime,
            final Integer historyFailureLifetime,
            final Integer useSeparateProcess
    ) {
        this.module = module;
        this.groupName = groupName;
        this.scheduleGenerateEvery = scheduleGenerateEvery;
        this.scheduleAheadFor = scheduleAheadFor;
        this.scheduleLifetime = scheduleLifetime;
        this.historyCleanupEvery = historyCleanupEvery;
        this.historySuccessLifetime = historySuccessLifetime;
        this.historyFailureLifetime = historyFailureLifetime;
        this.useSeparateProcess = useSeparateProcess;
    }

    /**
     * Get module name.
     *
     * @return String
     */
    public String getModule() {
        return module;
    }

    /**
     * Get CRON group name.
     *
     * @return String
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Get schedule ahead for.
     *
     * @return Integer
     */
    public Integer getScheduleAheadFor() {
        return scheduleAheadFor;
    }

    /**
     * Get schedule generate every.
     *
     * @return Integer
     */
    public Integer getScheduleGenerateEvery() {
        return scheduleGenerateEvery;
    }

    /**
     * Get schedule lifetime.
     *
     * @return Integer
     */
    public Integer getScheduleLifetime() {
        return scheduleLifetime;
    }

    /**
     * Get history cleanup every.
     *
     * @return Integer
     */
    public Integer getHistoryCleanupEvery() {
        return historyCleanupEvery;
    }

    /**
     * Get history success lifetime.
     *
     * @return Integer
     */
    public Integer getHistorySuccessLifetime() {
        return historySuccessLifetime;
    }

    /**
     * Get history failure lifetime.
     *
     * @return Integer
     */
    public Integer getHistoryFailureLifetime() {
        return historyFailureLifetime;
    }

    /**
     * Get use separate process.
     *
     * @return Integer
     */
    public Integer getUseSeparateProcess() {
        return useSeparateProcess;
    }
}
