/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.packages;

import com.magento.idea.magento2uct.execution.process.OutputWrapper;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public enum IssueSeverityLevel {

    WARNING(3, OutputWrapper.WARNING_WRAPPER.replace("{text}", "[WARNING]")),
    ERROR(2, OutputWrapper.ERROR_WRAPPER.replace("{text}", "[ERROR]")),
    CRITICAL(1, OutputWrapper.CRITICAL_WRAPPER.replace("{text}", "[CRITICAL]"));

    private final int level;
    private final String formattedLabel;

    /**
     * ENUM constructor.
     *
     * @param level int
     * @param formattedLabel String
     */
    IssueSeverityLevel(final int level, final String formattedLabel) {
        this.level = level;
        this.formattedLabel = formattedLabel;
    }

    /**
     * Get severity level.
     *
     * @return int
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get severity label.
     *
     * @return String
     */
    public String getLabel() {
        return this.toString();
    }

    /**
     * Get formatted label.
     *
     * @return String
     */
    public String getFormattedLabel() {
        return formattedLabel;
    }

    /**
     * Get issue severity level by level value.
     *
     * @param level int
     *
     * @return IssueSeverityLevel
     */
    public static IssueSeverityLevel getByLevel(final int level) {
        for (final IssueSeverityLevel issueSeverityLevel : IssueSeverityLevel.values()) {
            if (issueSeverityLevel.getLevel() == level) {
                return issueSeverityLevel;
            }
        }

        return getDefaultIssueSeverityLevel();
    }

    /**
     * Get severity levels.
     *
     * @return List[IssueSeverityLevel]
     */
    public static List<IssueSeverityLevel> getSeverityLabels() {
        return new LinkedList<>(Arrays.asList(IssueSeverityLevel.values()));
    }

    /**
     * Get default issue severity level.
     *
     * @return IssueSeverityLevel
     */
    public static IssueSeverityLevel getDefaultIssueSeverityLevel() {
        return WARNING;
    }
}
