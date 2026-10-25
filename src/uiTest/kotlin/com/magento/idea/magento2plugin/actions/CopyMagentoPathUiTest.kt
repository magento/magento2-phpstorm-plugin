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

    @Test
    fun `copies magento asset paths from custom theme override`() =
        runWebStormUiTest(FILE_GENERATORS_FIXTURE) {
            invokeProjectViewAction(
                THEME_PATH + listOf("Magento_Catalog", "templates", "product", "image.phtml"),
                "CopyMagentoPath",
            )

            waitFor("Magento theme override asset path in clipboard", 30.seconds) {
                getClipboardText().toString() == "Magento_Catalog::product/image.phtml"
            }

            invokeProjectViewAction(
                THEME_PATH + listOf("Magento_Catalog", "web", "js", "product.js"),
                "CopyMagentoPath",
            )

            waitFor("Magento theme override JavaScript path in clipboard", 30.seconds) {
                getClipboardText().toString() == "Magento_Catalog/js/product"
            }

            invokeProjectViewAction(
                THEME_PATH + listOf(
                    "Magento_Catalog",
                    "web",
                    "template",
                    "product",
                    "image.html",
                ),
                "CopyMagentoPath",
            )

            waitFor("Magento theme override HTML path in clipboard", 30.seconds) {
                getClipboardText().toString() == "Magento_Catalog/template/product/image"
            }
        }
}
