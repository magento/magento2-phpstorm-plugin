/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.AnAction
import com.intellij.driver.sdk.Notification
import com.intellij.driver.sdk.getNotifications
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.waitFor
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

class MagentoStartupUiTest {
    @Test
    fun `opens settings when the Magento installation is missing without PHP`() = runWebStormUiTest(
        "file-generators",
        prepareProject = { root ->
            check(root.resolve("vendor").toFile().deleteRecursively())
            Files.createDirectories(root.resolve(".idea"))
            root.resolve(".idea/workspace.xml").writeText("""
                <project version="4">
                    <component name="Magento2PluginSettings">
                        <option name="pluginEnabled" value="true"/>
                    </component>
                </project>
            """.trimIndent())
        },
        enableSupport = false,
    ) {
        check(!isPluginLoaded("com.jetbrains.php")) { "This test must run without the PHP plugin" }
        val message = "Magento 2 support is disabled. Please configure Magento 2 installation path."
        waitFor("Missing Magento installation notification", 30.seconds) {
            getNotifications().any { it.getContent() == message }
        }
        val notification = getNotifications().single { it.getContent() == message }
        val showSettings = notification.getActions().single { it.getTemplateText() == "Show settings" }
        withContext(OnDispatcher.EDT, semantics = LockSemantics.READ_ACTION) {
            utility<StartupNotificationActions>().fire(notification, showSettings, null)
        }
        ideFrame {
            settingsDialog {
                waitFor("Magento settings selected", 30.seconds) {
                    settingsTree.collectExpandedPaths().any { it.path.last() == "Magento" }
                }
            }
        }
    }
}

@Remote("com.intellij.openapi.actionSystem.DataContext")
private interface StartupDataContext

@Remote("com.intellij.notification.Notification")
private interface StartupNotificationActions {
    fun fire(notification: Notification, action: AnAction, dataContext: StartupDataContext?)
}
