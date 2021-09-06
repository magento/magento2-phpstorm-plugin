/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public enum IssueSeverityLevel {

    WARNING(3),
    ERROR(2),
    CRITICAL(1);

    private final int level;

    /**
     * ENUM constructor.
     *
     * @param level int
     */
    IssueSeverityLevel(final int level) {
        this.level = level;
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
