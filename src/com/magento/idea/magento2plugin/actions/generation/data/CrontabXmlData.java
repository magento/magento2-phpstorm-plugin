/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

public class CrontabXmlData {
    private String moduleName;
    private String cronGroup;
    private String cronjobName;
    private String cronjobInstance;
    private String cronjobSchedule;
    private String cronjobScheduleConfigPath;

    /**
     * @param moduleName
     * @param cronGroup
     * @param cronjobName
     * @param cronjobInstance
     * @param cronjobSchedule
     */
    public CrontabXmlData(
        String moduleName,
        String cronGroup,
        String cronjobName,
        String cronjobInstance,
        String cronjobSchedule,
        String cronjobScheduleConfigPath
    ) {
        this.moduleName = moduleName;
        this.cronGroup = cronGroup;
        this.cronjobName = cronjobName;
        this.cronjobInstance = cronjobInstance;
        this.cronjobSchedule = cronjobSchedule;
        this.cronjobScheduleConfigPath = cronjobScheduleConfigPath;
    }

    public String getCronGroup() {
        return cronGroup;
    }

    public void setCronGroup(String cronGroup) {
        this.cronGroup = cronGroup;
    }

    public String getCronjobName() {
        return cronjobName;
    }

    public void setCronjobName(String cronjobName) {
        this.cronjobName = cronjobName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getCronjobInstance() {
        return cronjobInstance;
    }

    public void setCronjobInstance(String cronjobInstance) {
        this.cronjobInstance = cronjobInstance;
    }

    public String getCronjobSchedule() {
        return cronjobSchedule;
    }

    public void setCronjobSchedule(String cronjobSchedule) {
        this.cronjobSchedule = cronjobSchedule;
    }

    public String getCronjobScheduleConfigPath() {
        return cronjobScheduleConfigPath;
    }

    public void setCronjobScheduleConfigPath(String cronjobScheduleConfigPath) {
        this.cronjobScheduleConfigPath = cronjobScheduleConfigPath;
    }
}
