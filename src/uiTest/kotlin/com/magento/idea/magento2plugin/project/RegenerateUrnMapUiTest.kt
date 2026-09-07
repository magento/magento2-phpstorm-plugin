/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project

import com.intellij.driver.client.service
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.getNotifications
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.button
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.waitFor
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RegenerateUrnMapUiTest {
    @Test
    fun `regenerates framework and module URN mappings in WebStorm`() {
        val expectedMappings = mapOf(
            "urn:magento:framework:App/etc/routes.xsd" to
                "vendor/magento/framework/App/etc/routes.xsd",
            "urn:magento:framework:Config/etc/config.xsd" to
                "vendor/magento/framework/Config/etc/config.xsd",
            "urn:magento:framework:View/Layout/etc/layout_generic.xsd" to
                "vendor/magento/framework/View/Layout/etc/layout_generic.xsd",
            "urn:magento:module:Acme_Shipping:etc/shipping_methods.xsd" to
                "app/code/Acme/Shipping/etc/shipping_methods.xsd",
            "urn:magento:module:Acme_Shipping:etc/adminhtml/system_file.xsd" to
                "app/code/Acme/Shipping/etc/adminhtml/system_file.xsd",
        )

        runWebStormUiTest("urn-mapping") {
            ideFrame {
                openSettingsDialog()
                settingsDialog {
                    searchTextField.text = "Magento"
                    waitFor("Magento settings search result", 30.seconds) {
                        settingsTree.collectExpandedPaths().any { it.path.last() == "Magento" }
                    }
                    settingsTree.clickRow { it == "Magento" }
                    content {
                        val regenerateButton = button("Regenerate URN mappings").waitFound()
                        withContext(OnDispatcher.EDT) {
                            driver.cast(regenerateButton.component, SwingButton::class).doClick()
                        }
                    }
                }
            }

            waitFor("URN mapping generation completion", 2.minutes) {
                getNotifications().any {
                    it.getTitle() == "URN map generation completed" &&
                        it.getContent().contains("Processed ${expectedMappings.size} URN mappings.")
                }
            }
            expectedMappings.forEach { (urn, expectedPath) ->
                val mappedPath = service<ExternalResources>().getResourceLocation(
                    urn,
                    singleProject(),
                )
                check(mappedPath.endsWith(expectedPath)) {
                    "$urn resolved to an unexpected path: $mappedPath"
                }
            }
            val ignoredUrn = "urn:magento:framework:Definition/config.xsd"
            val ignoredLocation = service<ExternalResources>().getResourceLocation(
                ignoredUrn,
                singleProject(),
            )
            check(ignoredLocation == ignoredUrn) {
                "Non-Magento Composer library unexpectedly received a URN mapping: $ignoredLocation"
            }
        }
    }
}

@Remote("javax.swing.JButton")
private interface SwingButton {
    fun doClick()
}

@Remote("com.intellij.javaee.ExternalResourceManager")
private interface ExternalResources {
    fun getResourceLocation(url: String, project: Project): String
}
