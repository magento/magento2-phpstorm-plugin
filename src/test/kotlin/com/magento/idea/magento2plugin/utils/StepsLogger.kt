/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.utils

import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker

object StepsLogger {
    private var initialized = false
    @JvmStatic
    fun init() {
        if (initialized.not()) {
            StepWorker.registerProcessor(StepLogger())
            initialized = true
        }
    }
}