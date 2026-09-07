/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui

import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.AnAction
import com.intellij.driver.sdk.Notification
import com.intellij.driver.sdk.getNotifications
import com.intellij.driver.sdk.ui.components.common.ideFrame
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
import org.junit.jupiter.api.fail
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.time.Duration.Companion.minutes

private const val MAGENTO_SUPPORT_NOTIFICATION = "Enable Magento support for this project?"

internal fun runWebStormUiTest(
    fixtureName: String,
    vararg generatedFilesToCleanup: String,
    test: Driver.(projectPath: Path) -> Unit,
) {
    WebStormUiTestEnvironment.initialize()

    val testName = CurrentTestMethod.hyphenateWithClass()
    val projectPath = UiTestProjectFixture.copy(fixtureName, testName)
    cleanupGeneratedFiles(projectPath, *generatedFilesToCleanup)
    val pluginPath = Path.of(System.getProperty("path.to.build.plugin"))

    Starter.newContext(
        testName,
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
        enableMagentoSupport()
        test(projectPath)
    }
}

internal fun Driver.enableMagentoSupport() {
    ideFrame {
        waitForIndicators(5.minutes)
        waitFor("Magento support notification", 2.minutes) {
            getNotifications().any { it.getContent() == MAGENTO_SUPPORT_NOTIFICATION }
        }

        val supportNotification = getNotifications()
            .first { it.getContent() == MAGENTO_SUPPORT_NOTIFICATION }
        val enableAction = supportNotification
            .getActions()
            .single { it.getTemplateText() == "Enable" }

        withContext(OnDispatcher.EDT, semantics = LockSemantics.READ_ACTION) {
            utility<NotificationActions>().fire(supportNotification, enableAction, null)
        }
        waitForIndicators(5.minutes)
    }
}

private object WebStormUiTestEnvironment {
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

    fun initialize() = Unit
}

private object UiTestProjectFixture {
    fun copy(fixtureName: String, testName: String): Path {
        val source = Path.of("src/uiTest/resources/projects", fixtureName).absolute().toFile()
        val target = Path.of("build/ui-test-projects", testName).absolute().toFile()

        check(source.isDirectory) { "UI test fixture does not exist: ${source.path}" }
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
