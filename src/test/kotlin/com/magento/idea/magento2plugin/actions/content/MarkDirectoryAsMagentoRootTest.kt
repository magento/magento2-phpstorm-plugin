/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.content

import com.automation.remarks.junit5.Video
import org.assertj.swing.core.MouseButton
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.Fixture
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.steps.CommonSteps
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.Keyboard
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.intellij.remoterobot.utils.waitForIgnoringError
import com.intellij.ui.components.dialog
import com.magento.idea.magento2plugin.pages.*
import com.magento.idea.magento2plugin.utils.RemoteRobotExtension
import com.magento.idea.magento2plugin.utils.StepsLogger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.event.KeyEvent.*
import java.io.File
import java.time.Duration.ofMinutes
import kotlin.io.path.createTempDirectory

@ExtendWith(RemoteRobotExtension::class)
class MarkDirectoryAsMagentoRootTest  {
    private lateinit var tempProjectDir: File

    init {
        StepsLogger.init()
    }

    @BeforeEach
    fun setup() {
        // Create a temporary directory
        val sourceDir = File("testData/project/magento2")
        tempProjectDir = createTempDirectory("intellij-test-project").toFile()

        // Copy the test data to the temporary directory
        sourceDir.copyRecursively(
            target = tempProjectDir,
            overwrite = true
        )
    }


    @BeforeEach
    fun waitForIde(remoteRobot: RemoteRobot) {
        waitForIgnoringError(ofMinutes(3)) { remoteRobot.callJs("true") }
    }

    @AfterEach
    fun closeProject(remoteRobot: RemoteRobot) = with(remoteRobot) {
        CommonSteps(remoteRobot).closeProject()
    }

    @Test
    @Video
    fun testMarkDirectoryAsMagentoRoot(remoteRobot: RemoteRobot) = with(remoteRobot) {
        // temporary workaround until we get license for CI
        val startTrial = find<ContainerFixture>(byXpath("//div[@visible_text='Start trial']"))
        startTrial.click()
        val startTrialFree = find<ContainerFixture>(byXpath("//div[@class='s']"))
        startTrialFree.click()
        val dialog = find<DialogFixture>(byXpath("//div[@class='MyDialog']"))
        dialog.button("Close").click()
        Thread.sleep(2_000)
        step("Switch back to PhpStorm IDE window if Firefox overlay detected") {
                remoteRobot.runJs(
                    """
            try {
                const KeyEvent = Java.type("java.awt.event.KeyEvent");
                const Frames = Java.type("java.awt.Frame");
                
              const activeWindow = Array.from(Frames.getFrames()).find(frame => frame.isActive());
                if (activeWindow && activeWindow.getName().includes("Firefox")) {
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_TAB);
                    Thread.sleep(100);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_ALT);
                }
                true;
            } catch (error) {
                false;
            }
            """.trimIndent()
                )

        }
        // end temporary workaround

        welcomeFrame {
            createNewProjectFromExistingFilesLink.click()
            dialog("Open File or Project") {
                // Set the path for the copied test data
                val comboBox = find<ContainerFixture>(byXpath("//div[@class='BorderlessTextField']"))
                comboBox.click() // Focus on the comboBox
                comboBox.keyboard {
                    hotKey(VK_CONTROL, VK_A) // Select all text
                    key(VK_DELETE) // Delete selected text
                    enterText(tempProjectDir.path)
                }

                button("OK").click()
                trustProjectLink.click()
            }
        }
        idea {
            step("Enable Magento Integration") {
                waitFor(ofMinutes(1)) { isDumbMode().not() }
                enableSupportLink.click(java.awt.Point(1, 1))
                waitFor(ofMinutes(1)) { isDumbMode().not() }

                keyboard {
                    hotKey(VK_ALT, VK_1)
                }

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