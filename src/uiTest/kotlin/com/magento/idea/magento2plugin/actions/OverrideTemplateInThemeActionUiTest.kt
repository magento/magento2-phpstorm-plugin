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
import org.junit.jupiter.api.Test

class OverrideTemplateInThemeActionUiTest {
    @Test
    fun `copies module javascript into selected theme`() =
        runWebStormUiTest(
            FILE_GENERATORS_FIXTURE,
            "app/design/frontend/Acme/storefront/Acme_Shipping/web/js/shipping.js",
        ) { projectPath ->
            invokeProjectViewAction(
                MODULE_PATH + listOf("view", "frontend", "web", "js", "shipping.js"),
                "OverrideTemplateInTheme.Menu",
                waitForAction = false,
            )

            ideFrame {
                dialog(title = "Override javascript file in project theme") {
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
                "app/design/frontend/Acme/storefront/Acme_Shipping/web/js/shipping.js",
                "return 'shipping'",
            )
        }
}
