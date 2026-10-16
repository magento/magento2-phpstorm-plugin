/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

enum class UpdateCheckWindow(val label: String, val period: String) {
    MONTH("Month", "the past month"),
    WEEK("Week", "the past week"),
    TWO_DAYS("2 days", "the past 2 days"),
    TWELVE_HOURS("12 hours", "the past 12 hours");

    internal fun since(now: Instant): Instant = when (this) {
        MONTH -> now.atOffset(ZoneOffset.UTC).minusMonths(1).toInstant()
        WEEK -> now.minus(7, ChronoUnit.DAYS)
        TWO_DAYS -> now.minus(2, ChronoUnit.DAYS)
        TWELVE_HOURS -> now.minus(12, ChronoUnit.HOURS)
    }

    override fun toString(): String = label
}
