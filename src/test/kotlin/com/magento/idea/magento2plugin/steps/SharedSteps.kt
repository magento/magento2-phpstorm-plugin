/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.steps

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.steps.CommonSteps
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.magento.idea.magento2plugin.pages.*
import java.awt.Point
import java.awt.event.KeyEvent.*
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.time.Duration.ofMinutes
import java.util.*

class SharedSteps(private val remoteRobot: RemoteRobot) {
    private lateinit var tempProjectDir: File

    fun createOrOpenTestProject(): File {
        setupTemporaryMagentoProject()

        step("Create Or Open Test Project", Runnable {
            try {
                remoteRobot.welcomeFrame {
                    val newProjectButton = remoteRobot.find(
                        ContainerFixture::class.java,
                        byXpath("//div[@visible_text='New Project']")
                    )
                    newProjectButton.click()
                    Thread.sleep(2_000)

                    val jTextFieldFixture = find<JTextFieldFixture>(byXpath("//div[@class='TextFieldWithBrowseButton']"))
                    jTextFieldFixture.click()
                    jTextFieldFixture.keyboard {
                        hotKey(VK_CONTROL, VK_A)
                        key(VK_DELETE)
                        enterText(tempProjectDir.absolutePath.toString().replace("\\", "\\\\"))
                    }
                    keyboard { key(VK_ENTER) }

                    dialog("Directory Is Not Empty") {
                        button("Create from Existing Sources").click()
                    }

                    enableMagentoSupport()
                }
            } catch (exception: Exception) {
                // temporary workaround until we get license for CI
                activateIde()
                // end temporary workaround
                try {
                    val launchedFromScript = remoteRobot.find(
                        ContainerFixture::class.java,
                        byXpath("//div[@class='LinkLabel']")
                    )
                    launchedFromScript.click()
                } catch (e: Exception) {
                    // Element does not exist, continue without failing the test
                }

                createProjectFromExistingFiles()
                enableMagentoSupport()
            }
        })

        return tempProjectDir;
    }

    fun closeProject() {
        CommonSteps(remoteRobot).closeProject()
    }

    private fun setupTemporaryMagentoProject() {
        // Create a parent directory and a random child directory inside it
        val parentDir = Paths.get("intellij-test-project").toFile()
        if (parentDir.exists()) {
            parentDir.deleteRecursively()
        }
        parentDir.mkdirs()

        // Create a randomly named child directory inside the parent directory
        tempProjectDir = File(parentDir, UUID.randomUUID().toString()).apply {
            mkdirs()
        }

        // Define the source directory for the test data
        val sourceDir = File("testData/project/magento2")

        // Copy the test data to the temporary directory
        sourceDir.copyRecursively(
            target = tempProjectDir,
            overwrite = true
        )
    }

    private fun activateIde() {
        if ("true" == System.getenv("GITHUB_ACTIONS")) {
            val startTrial =
                remoteRobot.find(ContainerFixture::class.java, byXpath("//div[@visible_text='Start trial']"))
            startTrial.click()

            val startTrialFree = remoteRobot.find(ContainerFixture::class.java, byXpath("//div[@class='s']"))
            startTrialFree.click()

            val dialog = remoteRobot.find(
                DialogFixture::class.java, byXpath("//div[@class='MyDialog']")
            )
            dialog.button("Close").click()
            closeBrowser()

            try {
                Thread.sleep(10000)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw RuntimeException(e)
            }
        } else {
            closeBrowser()
            val dialog = remoteRobot.find(
                DialogFixture::class.java, byXpath("//div[@class='MyDialog']")
            )
            dialog.button("Activate").click()
            dialog.button("Close").click()
        }
    }

    private fun enableMagentoSupport() {
        remoteRobot.idea {
            step("Enable Magento Integration") {
                waitFor(ofMinutes(1)) { isDumbMode().not() }
                Thread.sleep(5_000)
                enableSupportLink.click(Point(1, 1))
                waitFor(ofMinutes(1)) { isDumbMode().not() }

                if (!isProjectViewVisible()) {
                    keyboard {
                        hotKey(VK_ALT, VK_1)
                    }
                }
            }
        }
    }

    private fun createProjectFromExistingFiles() {
        remoteRobot.welcomeFrame {
            try {
                val launchedFromScript = find<ContainerFixture>(byXpath("//div[@class='LinkLabel']"))
                launchedFromScript.click()
            } catch (e: Exception) {
                // Element does not exist, continue without failing the test
            }

            createNewProjectFromExistingFilesLink.click()
            selectProjectPath()
        }
    }

    private fun WelcomeFrame.selectProjectPath() {
        dialog("Open File or Project") {
            // Set the path for the copied test data
            val comboBox = find<ContainerFixture>(byXpath("//div[@class='BorderlessTextField']"))
            comboBox.click() // Focus on the comboBox
            comboBox.keyboard {
                hotKey(VK_CONTROL, VK_A) // Select all text
                key(VK_DELETE) // Delete selected text
                enterText(tempProjectDir.absolutePath.toString().replace("\\", "\\\\"))
            }

            button("OK").click()
            trustProjectLink.click()
        }
    }

    /**
     * Closes the browser by terminating its process based on the operating system.
     */
    fun closeBrowser() {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())

        try {
            if (os.contains("win")) {
                // For Windows: Close common browsers like Chrome, Firefox, etc.
                Runtime.getRuntime().exec("taskkill /F /IM edge.exe")
            } else if (os.contains("mac")) {
                // For macOS: Kill browsers using `pkill`
                Runtime.getRuntime().exec("killall -9 safari")
            } else if (os.contains("nix") || os.contains("nux")) {
                // For Linux-based systems: Kill typical browser processes
                Runtime.getRuntime().exec("killall -9 firefox")
                Runtime.getRuntime().exec("killall -9 chrome")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}