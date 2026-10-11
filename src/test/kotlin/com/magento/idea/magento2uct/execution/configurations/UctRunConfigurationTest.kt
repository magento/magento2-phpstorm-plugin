/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.execution.configurations

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.jetbrains.php.config.commandLine.PhpCommandLinePathProcessor
import com.jetbrains.php.config.commandLine.PhpCommandSettings
import com.jetbrains.php.config.commandLine.PhpCommandSettingsBuilder
import com.jetbrains.php.config.interpreters.PhpInterpreter
import com.jetbrains.php.run.PhpConfigurationOption
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2uct.packages.SupportedVersion
import org.jdom.Element
import org.junit.Assume.assumeNotNull
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JTextField

class UctRunConfigurationTest : PhysicalMagentoTestCase() {
    private fun configuration(): UctRunConfiguration =
        UctConfigurationFactory(UctRunConfigurationType()).createTemplateConfiguration(project)
            .let { it as UctRunConfiguration }
            .apply {
                scriptName = "${project.basePath}/tools with spaces/uct"
                projectRoot = project.basePath!!
                comingVersion = "2.4.8"
                Settings.getInstance(project).magentoVersion = "2.4.7"
            }

    private fun commandSettings(inheritedLimit: String? = null): PhpCommandSettings =
        PhpCommandSettings("php", PhpCommandLinePathProcessor.LOCAL, null).apply {
            if (inheritedLimit != null) {
                addConfigurationOptions(listOf(PhpConfigurationOption("memory_limit", inheritedLimit)))
            }
        }

    fun testMemoryLimitIsPassedBeforeScriptAndAfterInheritedOptions() {
        val configuration = configuration()
        for (limit in listOf("512M", "2G", "-1", "1024k", "268435456")) {
            configuration.memoryLimit = limit
            configuration.checkConfiguration()
            val arguments = configuration.createCommandLine(commandSettings("128M")).parametersList.list
            val scriptIndex = arguments.indexOf(configuration.scriptName)
            val overrideIndex = arguments.indexOfFirst { it.contains("memory_limit=$limit") }
            val inheritedIndex = arguments.indexOfFirst { it.contains("memory_limit=128M") }
            assertTrue(arguments.toString(), inheritedIndex >= 0 && overrideIndex > inheritedIndex)
            assertTrue(arguments.toString(), overrideIndex < scriptIndex)
            assertEquals(
                listOf("upgrade:check", "--coming-version=2.4.8", "--current-version=2.4.7", "--context=phpstorm", "--ansi"),
                arguments.drop(scriptIndex + 1)
            )
        }
    }

    fun testEmptyMemoryLimitPreservesInterpreterSettings() {
        val configuration = configuration()
        for (empty in listOf("", "   ")) {
            configuration.memoryLimit = empty
            configuration.checkConfiguration()
            val inherited = configuration.createCommandLine(commandSettings("128M")).parametersList.list
            assertEquals(1, inherited.count { it.contains("memory_limit=") })
            assertTrue(inherited.any { it.contains("memory_limit=128M") })
            val defaults = configuration.createCommandLine(commandSettings()).parametersList.list
            assertFalse(defaults.any { it.contains("memory_limit=") })
        }
    }

    fun testInvalidMemoryLimitIsRejectedBeforeCommandCreation() {
        val configuration = configuration()
        for (invalid in listOf("0", "-2", "1.5G", "512MB", "unlimited", "2G -n", "2G\n-d foo=bar")) {
            configuration.memoryLimit = invalid
            val validation = org.junit.Assert.assertThrows(RuntimeConfigurationError::class.java) { configuration.checkConfiguration() }
            assertTrue(validation.message!!.contains("PHP memory limit"))
            org.junit.Assert.assertThrows(ExecutionException::class.java) { configuration.createCommandLine(commandSettings()) }
        }
    }

    fun testMemoryLimitPersistsAndOlderConfigurationsKeepDefault() {
        val configuration = configuration()
        configuration.memoryLimit = " 2G "
        val saved = Element("configuration")
        configuration.writeExternal(saved)
        val restored = configuration()
        restored.readExternal(saved)
        assertEquals("2G", restored.memoryLimit)
        assertEquals("2G", (restored.clone() as UctRunConfiguration).memoryLimit)

        val oldConfiguration = Element("configuration")
        configuration().writeExternal(oldConfiguration)
        restored.readExternal(oldConfiguration)
        assertEquals("", restored.memoryLimit)
        restored.checkConfiguration()
    }

    fun testEditorAppliesResetsAndRejectsInvalidMemoryLimit() {
        // Keep this form test offline; the version selector otherwise queries Packagist.
        val versionsField = SupportedVersion::class.java.getDeclaredField("cachedVersions").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val versions = versionsField.get(null) as AtomicReference<List<SupportedVersion>?>
        val version = SupportedVersion::class.java.getDeclaredConstructor(String::class.java)
            .apply { isAccessible = true }.newInstance("2.4.8")
        val previousVersions = versions.getAndSet(listOf(version))
        try {
            val configuration = configuration().apply { memoryLimit = "512M" }
            val editor = UctSettingsEditor(project)
            Disposer.register(testRootDisposable, editor)
            editor.resetFrom(configuration)
            val input = UctSettingsEditor::class.java.getDeclaredField("memoryLimit")
                .apply { isAccessible = true }.get(editor) as JTextField
            assertEquals("512M", input.text)
            input.text = " -1 "
            editor.applyTo(configuration)
            assertEquals("-1", configuration.memoryLimit)
            input.text = "invalid"
            org.junit.Assert.assertThrows(ConfigurationException::class.java) { editor.applyTo(configuration) }
            assertEquals("-1", configuration.memoryLimit)
            editor.resetFrom(configuration)
            assertEquals("-1", input.text)
            input.text = " "
            editor.applyTo(configuration)
            assertEquals("", configuration.memoryLimit)
        } finally {
            versions.set(previousVersions)
        }
    }

    fun testPhpProcessReceivesMemoryLimitOverride() {
        val php = PathEnvironmentVariableUtil.findInPath(if (SystemInfo.isWindows) "php.exe" else "php")
        assumeNotNull(php)
        VfsRootAccess.allowRootAccess(testRootDisposable, php!!.path)
        val script = myFixture.addFileToProject(
            "tools with spaces/uct",
            """<?php echo ini_get('memory_limit'), "\n", implode("\n", array_slice(${'$'}argv, 1));"""
        ).virtualFile.path
        val ini = myFixture.addFileToProject("php.ini", "memory_limit=128M\n").virtualFile.path
        val interpreter = PhpInterpreter().apply {
            name = "UCT memory limit test"
            homePath = php.path
            customIni = ini
        }
        val configuration = configuration().apply { scriptName = script }
        for ((limit, expected) in listOf("" to "128M", "2G" to "2G", "-1" to "-1")) {
            configuration.memoryLimit = limit
            val settings = PhpCommandSettingsBuilder.create(project, interpreter, false)
            val commandLine: GeneralCommandLine = configuration.createCommandLine(settings)
            val output = CapturingProcessHandler(commandLine).runProcess(10_000)
            assertFalse(output.isTimeout)
            assertEquals(output.stderr, 0, output.exitCode)
            assertEquals(
                listOf(expected, "upgrade:check", "--coming-version=2.4.8", "--current-version=2.4.7", "--context=phpstorm", "--ansi"),
                output.stdout.trim().lines()
            )
        }
    }
}
