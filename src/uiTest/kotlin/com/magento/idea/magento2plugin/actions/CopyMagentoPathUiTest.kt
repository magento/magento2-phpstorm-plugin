/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions

import com.intellij.driver.sdk.ui.getClipboardText
import com.intellij.driver.sdk.waitFor
import com.magento.idea.magento2plugin.ui.invokeProjectViewAction
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class CopyMagentoPathUiTest {
    @Test
    fun `copies magento javascript asset path`() = runWebStormUiTest(FILE_GENERATORS_FIXTURE) {
        invokeProjectViewAction(
            MODULE_PATH + listOf("view", "frontend", "web", "js", "shipping.js"),
            "CopyMagentoPath",
        )

        waitFor("Magento asset path in clipboard", 30.seconds) {
            getClipboardText().toString() == "Acme_Shipping/js/shipping"
        }
    }
}
