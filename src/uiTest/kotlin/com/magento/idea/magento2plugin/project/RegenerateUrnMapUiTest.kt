/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project

import com.intellij.driver.client.Remote
import com.intellij.driver.client.service
import com.intellij.driver.client.utility
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.AnAction
import com.intellij.driver.sdk.Notification
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.getNotifications
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.button
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.waitFor
import com.intellij.ide.starter.ci.CIServer
import com.intellij.ide.starter.ci.NoCIServer
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.junit5.hyphenateWithClass
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.CurrentTestMethod
import com.intellij.ide.starter.runner.Starter
import com.intellij.platform.testFramework.teamCity.TeamCityReporter.SyntheticTestKind
import com.intellij.tools.ide.starter.product.webstorm.WebStorm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RegenerateUrnMapUiTest {
    private val pluginPath = Path.of(System.getProperty("path.to.build.plugin"))

    init {
        di = DI {
            extend(di)
            bindSingleton<CIServer>(overrides = true) {
                object : CIServer by NoCIServer {
                    override fun reportTestFailure(
                        testName: String,
                        message: String,
                        details: String,
                        linkToLogs: String?,
                        kind: SyntheticTestKind,
                        generifyTestName: Boolean,
                    ) {
                        fail("$testName failed in WebStorm: $message\n$details")
                    }
                }
            }
        }
    }

    @Test
    fun `regenerates framework and module URN mappings in WebStorm`() {
        val projectPath = prepareProject()
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

        Starter.newContext(
            CurrentTestMethod.hyphenateWithClass(),
            TestCase(IdeInfo.WebStorm, LocalProjectInfo(projectPath))
                .withVersion(System.getProperty("ui.test.ide.version")),
        ).apply {
            PluginConfigurator(this).installPluginFromPath(pluginPath)
            applyVMOptionsPatch {
                addSystemProperty("idea.trust.all.projects", true)
                addSystemProperty("ide.show.tips.on.startup.default.value", false)
                addSystemProperty("jb.consents.confirmation.enabled", false)
            }
        }.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                waitForIndicators(5.minutes)

                waitFor("Magento support notification", 2.minutes) {
                    getNotifications().any { it.getContent() == "Enable Magento support for this project?" }
                }
                val supportNotification = getNotifications()
                    .first { it.getContent() == "Enable Magento support for this project?" }
                val enableAction = supportNotification
                    .getActions()
                    .single { it.getTemplateText() == "Enable" }
                withContext(
                    OnDispatcher.EDT,
                    semantics = LockSemantics.READ_ACTION,
                ) {
                    utility<NotificationActions>().fire(
                        supportNotification,
                        enableAction,
                        null,
                    )
                }
                waitForIndicators(5.minutes)

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

    private fun prepareProject(): Path {
        val source = Path.of("src/uiTest/resources/projects/urn-mapping").absolute().toFile()
        val target = Path.of("build/ui-test-projects/urn-mapping").absolute().toFile()

        check(target.deleteRecursively()) { "Could not clean UI test project at ${target.path}" }
        check(source.copyRecursively(target, overwrite = true)) {
            "Could not copy UI test project to ${target.path}"
        }

        return target.toPath()
    }
}

@Remote("com.intellij.openapi.actionSystem.DataContext")
private interface DataContext

@Remote("com.intellij.notification.Notification")
private interface NotificationActions {
    fun fire(notification: Notification, action: AnAction, dataContext: DataContext?)
}

@Remote("javax.swing.JButton")
private interface SwingButton {
    fun doClick()
}

@Remote("com.intellij.javaee.ExternalResourceManager")
private interface ExternalResources {
    fun getResourceLocation(url: String, project: Project): String
}
