/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions

import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.JTextFieldUI
import com.intellij.driver.sdk.ui.components.elements.dialog
import com.magento.idea.magento2plugin.ui.assertGeneratedFile
import com.magento.idea.magento2plugin.ui.clickOkButton
import com.magento.idea.magento2plugin.ui.invokeProjectViewAction
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test

class NewEmailTemplateActionUiTest {
    @Test
    fun `creates email template files from module context`() =
        runWebStormUiTest(FILE_GENERATORS_FIXTURE) { projectPath ->
            invokeProjectViewAction(
                MODULE_PATH,
                "MagentoCreateEmailTemplate",
                waitForAction = false,
            )

            ideFrame {
                dialog(title = "Create a new Magento 2 email template") {
                    val fields = xx("//div[@class='JTextField']", JTextFieldUI::class.java).list()
                    check(fields.size == 4) {
                        "Expected four email-template text fields, found ${fields.size}"
                    }
                    fields[0].text = "shipping_update"
                    fields[1].text = "Shipping Update"
                    fields[2].text = "shipping_update"
                    fields[3].text = "Your shipping update"

                    clickOkButton()
                }
            }

            assertGeneratedFile(
                projectPath,
                "app/code/Acme/Shipping/etc/email_templates.xml",
                "id=\"shipping_update\"",
                "file=\"shipping_update.html\"",
                "module=\"Acme_Shipping\"",
                "area=\"frontend\"",
            )
            assertGeneratedFile(
                projectPath,
                "app/code/Acme/Shipping/view/frontend/email/shipping_update.html",
                "Your shipping update",
                "implement html template",
            )
        }
}
