/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions

import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.JComboBoxUiComponent
import com.intellij.driver.sdk.ui.components.elements.dialog
import com.magento.idea.magento2plugin.ui.assertGeneratedFile
import com.magento.idea.magento2plugin.ui.clickOkButton
import com.magento.idea.magento2plugin.ui.invokeProjectViewAction
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files

class OverrideEmailTemplateInThemeActionUiTest {
    @Test
    fun `copies nested email into theme`() =
        runWebStormUiTest(
            FILE_GENERATORS_FIXTURE,
            "app/design/frontend/Acme/storefront/Acme_Shipping/email/order/shipping.html",
        ) { projectPath ->
            val sourcePath = projectPath.resolve(
                "app/code/Acme/Shipping/view/frontend/email/order/shipping.html",
            )
            val originalText = Files.readString(sourcePath)
            invokeProjectViewAction(
                MODULE_PATH + listOf("view", "frontend", "email", "order", "shipping.html"),
                "OverrideEmailTemplateInTheme.Menu",
                waitForAction = false,
            )

            ideFrame {
                dialog(title = "Override email template file in project theme") {
                    val theme = xx(
                        "//div[@class='JComboBox']",
                        JComboBoxUiComponent::class.java,
                    ).list().single()
                    theme.selectItem("frontend/Acme/storefront")
                    clickOkButton()
                }
            }

            assertGeneratedFile(
                projectPath,
                "app/design/frontend/Acme/storefront/Acme_Shipping/email/order/shipping.html",
                "{{var order.increment_id}}",
            )
            assertEquals(
                originalText,
                Files.readString(projectPath.resolve(
                    "app/design/frontend/Acme/storefront/Acme_Shipping/email/order/shipping.html",
                )),
            )
            assertEquals(originalText, Files.readString(sourcePath))
        }
}
