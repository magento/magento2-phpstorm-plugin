/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.userInterface.content

import com.automation.remarks.junit5.Video
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitForIgnoringError
import com.magento.idea.magento2plugin.pages.*
import com.magento.idea.magento2plugin.steps.SharedSteps
import com.magento.idea.magento2plugin.utils.RemoteRobotExtension
import com.magento.idea.magento2plugin.utils.StepsLogger
import java.awt.event.KeyEvent.VK_A
import java.awt.event.KeyEvent.VK_CONTROL
import java.awt.event.KeyEvent.VK_DELETE
import java.time.Duration.ofMinutes
import org.assertj.swing.core.MouseButton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(RemoteRobotExtension::class)
class MarkDirectoryAsMagentoRootTest {
    init {
        StepsLogger.init()
    }

    @BeforeEach
    fun waitForIde(remoteRobot: RemoteRobot) {
        waitForIgnoringError(ofMinutes(3)) { remoteRobot.callJs("true") }
    }

    @AfterEach
    fun closeProject(remoteRobot: RemoteRobot) = with(remoteRobot) {
        SharedSteps(remoteRobot).closeProject()
    }

    @Test
    @Video
    fun testMarkDirectoryAsMagentoRoot(remoteRobot: RemoteRobot) = with(remoteRobot) {
        SharedSteps(remoteRobot).createOrOpenTestProject()

        idea {
            step("Create a new Plugin") {
                with(projectViewTree) {
                    findText("vendor").doubleClick()
                    findText("module-catalog").doubleClick()
                    findText("Block").doubleClick()
                    findText("Navigation.php").doubleClick()
                }

                createAPluginWithoutMagentoRootInVendor(this@idea, remoteRobot)

                with(projectViewTree) {
                    //add magento code to project
                    findText("magento").click(MouseButton.RIGHT_BUTTON)
                    contextMenu("Mark Directory as").click()
                    contextMenuItem("Sources Root").click()

                    findText("module-catalog").click(MouseButton.RIGHT_BUTTON)
                    contextMenu("Mark Directory as").click()
                    contextMenuItem("Mark Directory As Magento Code Root").click()
                }

                with(textEditor()) {
                    step("Create a new Plugin with marking as code root") {
                        Thread.sleep(1_000)
                        editor.findText("someMethod").click(MouseButton.RIGHT_BUTTON)
                        contextMenuItem("Create a new Plugin").click()

                        createAPluginDialog {
                            step("Ensure target module includes 'Magento_Catalog'") {
                                pluginName.click()
                                pluginName.keyboard {
                                    enterText("test_plugin")
                                }
                                className.click()
                                className.keyboard {
                                    enterText("TestPlugin")
                                }

                                targetModule.click()
                                targetModule.keyboard {
                                    hotKey(VK_CONTROL, VK_A) // Select all text
                                    key(VK_DELETE) // Delete selected text
                                    enterText("Magento_Catalog")
                                    button("OK").click()
                                }
                            }
                        }
                    }
                }

                with(projectViewTree) {
                    findText("Plugin").doubleClick()
                    findText("TestPlugin.php").doubleClick()
                }

                with(textEditor()) {
                    step("Check created files") {
                        editor.findText("beforeSomeMethod")
                    }
                }

                with(projectViewTree) {
                    findText("module-catalog").click(MouseButton.RIGHT_BUTTON)
                    contextMenu("Mark Directory as").click()
                    contextMenuItem("Unmark Directory As Magento Code Root").click()
                    findText("Navigation.php").doubleClick()
                }

                createAPluginWithoutMagentoRootInVendor(this@idea, remoteRobot)
            }
        }
    }

    /**
     * Creates a new plugin in a project without marking the target module as a Magento code root.
     *
     * @param ideaFrame
     * @param remoteRobot
     */
    private fun createAPluginWithoutMagentoRootInVendor(
        ideaFrame: IdeaFrame,
        remoteRobot1: RemoteRobot
    ) {
        with(ideaFrame.textEditor()) {
            step("Create a new Plugin without marking as code root") {
                Thread.sleep(1_000)
                editor.findText("someMethod").click(MouseButton.RIGHT_BUTTON)
                remoteRobot1.contextMenuItem("Create a new Plugin").click()

                remoteRobot1.createAPluginDialog {
                    step("Ensure target module does not include 'Magento_Catalog'") {
                        pluginName.click()
                        pluginName.keyboard {
                            enterText("test_plugin")
                        }
                        className.click()
                        className.keyboard {
                            enterText("TestPlugin")
                        }

                        targetModule.click()
                        targetModule.keyboard {
                            hotKey(VK_CONTROL, VK_A) // Select all text
                            key(VK_DELETE) // Delete selected text
                            enterText("Magento_Catalog")
                            button("OK").click()

                            errorDialog {
                                button("OK").click()
                            }

                            button("Cancel").click()
                        }
                    }
                }
            }
        }
    }
}
