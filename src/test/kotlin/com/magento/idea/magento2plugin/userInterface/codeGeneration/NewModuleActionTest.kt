/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.userInterface.codeGeneration

import com.automation.remarks.junit5.Video
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitForIgnoringError
import com.magento.idea.magento2plugin.magento.files.ComposerJson
import com.magento.idea.magento2plugin.magento.files.ModuleXml
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp
import com.magento.idea.magento2plugin.magento.packages.File
import com.magento.idea.magento2plugin.magento.packages.Package
import com.magento.idea.magento2plugin.pages.*
import com.magento.idea.magento2plugin.steps.SharedSteps
import com.magento.idea.magento2plugin.utils.RemoteRobotExtension
import com.magento.idea.magento2plugin.utils.StepsLogger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration.ofMinutes

@ExtendWith(RemoteRobotExtension::class)
class NewModuleActionTest  {
    private lateinit var tempProjectDir: java.io.File

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
    fun testNewModuleAction(remoteRobot: RemoteRobot) = with(remoteRobot) {
        tempProjectDir = SharedSteps(remoteRobot).createOrOpenTestProject()

        idea {
            step("Create A new Module") {
                with(projectViewTree) {
                    findText("app").doubleClick()
                    findText("code").rightClick()
                }

                contextMenu("New").click()
                contextMenuItem("Magento 2 Module").click()

                createAModuleDialog {
                    step("Ensure target module includes 'Magento_Catalog'") {
                        packageName.click()
                        packageName.keyboard {
                            enterText("MyTestVendor")
                        }

                        moduleName.click()
                        moduleName.keyboard {
                            enterText("MyTestModule")
                            button("OK").click()
                        }
                    }
                }
            }

            step("Check Generated Files") {
                checkRegistrationPhp()
                checkModuleXml()
                checkComposerJson()
            }
        }
    }

    private fun checkRegistrationPhp() {
        val registrationPhp = java.io.File(
            getModulePath() +
                    File.separator +
                    RegistrationPhp.FILE_NAME
        )

        val expected = "<?php\n" +
                "\n" +
                "use Magento\\Framework\\Component\\ComponentRegistrar;\n" +
                "\n" +
                "ComponentRegistrar::register(ComponentRegistrar::MODULE, 'MyTestVendor_MyTestModule', __DIR__);\n"
        val actual = registrationPhp.readText()
        if (actual != expected) {
            throw AssertionError("The content of registration.php does not match the expected content.\nExpected:\n$expected\nActual:\n$actual")
        }
    }

    private fun checkModuleXml() {
        val xmlFile = java.io.File(
            getModulePath() +
                    File.separator +
                    Package.moduleBaseAreaDir +
                    File.separator +
                    ModuleXml.FILE_NAME
        )

        val expected = "<?xml version=\"1.0\"?>\n" +
                "<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:noNamespaceSchemaLocation=\"urn:magento:framework:Module/etc/module.xsd\">\n" +
                "    <module name=\"MyTestVendor_MyTestModule\"/>\n" +
                "</config>\n"
        val actual = xmlFile.readText()
        if (actual != expected) {
            throw AssertionError("The content of registration.php does not match the expected content.\nExpected:\n$expected\nActual:\n$actual")
        }
    }

    private fun checkComposerJson() {
        val composerJsonFile = java.io.File(
            getModulePath() +
                    File.separator +
                    ComposerJson.FILE_NAME
        )

        val expected = "{\n" +
                "  \"name\": \"my-test-vendor/module-my-test-module\",\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"description\": \"N/A\",\n" +
                "  \"type\": \"magento2-module\",\n" +
                "  \"require\": {\n" +
                "    \"magento/framework\": \"*\"\n" +
                "  },\n" +
                "  \"license\": [\n" +
                "    \"\"\n" +
                "  ],\n" +
                "  \"autoload\": {\n" +
                "    \"files\": [\n" +
                "      \"registration.php\"\n" +
                "    ],\n" +
                "    \"psr-4\": {\n" +
                "      \"MyTestVendor\\\\MyTestModule\\\\\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
        val actual = composerJsonFile.readText()
        if (actual != expected) {
            throw AssertionError("The content of registration.php does not match the expected content.\nExpected:\n$expected\nActual:\n$actual")
        }
    }

    private fun getModulePath() = tempProjectDir.path + "/app/code/MyTestVendor/MyTestModule"
}
