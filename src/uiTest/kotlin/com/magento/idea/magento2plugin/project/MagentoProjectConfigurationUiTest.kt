/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project

import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.button
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.waitFor
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class MagentoProjectConfigurationUiTest {

    @Test
    fun mcpToUiRoundTripAndInspectionInWebStorm() = runWebStormUiTest(
        "file-generators",
        enableSupport = false,
        prepareProject = { root ->
            // Start with clean project without pre-configured Magento support
            Files.createDirectories(root.resolve(".idea"))
            root.resolve(".idea/workspace.xml").writeText("""
                <project version="4">
                    <component name="Magento2PluginSettings">
                        <option name="pluginEnabled" value="false"/>
                        <option name="magentoPath" value=""/>
                    </component>
                </project>
            """.trimIndent())
        }
    ) { root ->
        check(!isPluginLoaded("com.jetbrains.php")) { "WebStorm must run without PHP plugin" }
        check(isPluginLoaded("com.intellij.mcpServer")) { "MCP plugin must be loaded" }

        val project = singleProject()
        val commands = utility<ProjectConfigCommands>()
        val inspections = utility<ProjectInspectCommands>()
        val settings = utility<ProjectSettingsRemote>()

        // 1. Initial state: integration is disabled
        val initialStatus = commands.getStatus(project)
        check(initialStatus.contains("Integration enabled: no")) { initialStatus }
        check(!settings.isEnabled(project)) { "Plugin should be initially disabled" }

        // 2. Configure and enable through MCP endpoint
        val configureResult = commands.configure(
            project = project,
            action = "configure",
            magentoPath = ".",
            enabled = true,
            excludeGeneratedFiles = null,
            projectPath = root.toString()
        )
        check(configureResult.contains("Result: success")) { configureResult }
        check(configureResult.contains("Integration enabled: yes")) { configureResult }
        check(configureResult.contains("Indexing exclusions: enabled")) { configureResult }
        check(settings.isEnabled(project)) { "Plugin should be enabled after MCP configuration" }
        check(settings.isExcludeGeneratedFiles(project)) { "Exclusions should be enabled by default" }

        // 3. Open Magento settings dialog and verify MCP-configured state
        ideFrame {
            openSettingsDialog()
            settingsDialog {
                searchTextField.text = "Magento"
                waitFor("Magento settings in tree", 30.seconds) {
                    settingsTree.collectExpandedPaths().any { it.path.last() == "Magento" }
                }
                settingsTree.clickRow { it == "Magento" }

                // Settings dialog shows updated values from Settings service
                check(settings.isEnabled(project)) { "Settings dialog reflects enabled state" }
                check(settings.getMagentoPath(project) == root.toString()) { "Settings dialog reflects configured path" }
                check(settings.isExcludeGeneratedFiles(project)) { "Settings dialog reflects exclusion checkbox" }

                button("OK").click()
            }
            waitForIndicators(5.minutes)
        }

        // 4. Repeated configuration is idempotent
        val repeatResult = commands.configure(
            project = project,
            action = "configure",
            magentoPath = ".",
            enabled = true,
            excludeGeneratedFiles = true,
            projectPath = root.toString()
        )
        check(repeatResult.contains("no changes required")) { repeatResult }

        // 5. Query fixture module through magento_inspect to verify setup has a usable effect
        val inspectResult = withReadAction {
            inspections.query(project, "module", """{"moduleName":"Acme_Shipping"}""")
        }
        check(inspectResult.contains("app/code/Acme/Shipping")) { inspectResult }
    }

    @Test
    fun nestedRootAndPartialCheckoutAndInvalidPathInWebStorm() = runWebStormUiTest(
        "file-generators",
        enableSupport = false,
        prepareProject = { root ->
            // Create a nested root under src/
            val nested = root.resolve("src")
            Files.createDirectories(nested.resolve("vendor/magento/framework"))
            nested.resolve("composer.json").writeText("""{"name":"magento/project-community-edition","version":"2.4.8"}""")
            nested.resolve("vendor/magento/framework/composer.json").writeText("""{"name":"magento/framework"}""")

            // Create a supported partial checkout directory under partial/
            val partial = root.resolve("partial")
            Files.createDirectories(partial)
            partial.resolve("composer.json").writeText("""{"name":"magento/project-partial"}""")
        }
    ) { root ->
        check(!isPluginLoaded("com.jetbrains.php"))
        val project = singleProject()
        val commands = utility<ProjectConfigCommands>()
        val settings = utility<ProjectSettingsRemote>()

        // 1. Invalid path rejected and leaves prior settings unchanged
        val invalidResult = commands.configure(
            project = project,
            action = "configure",
            magentoPath = "nonexistent_dir",
            enabled = true,
            excludeGeneratedFiles = null,
            projectPath = root.toString()
        )
        check(invalidResult.contains("Result: error")) { invalidResult }
        check(invalidResult.contains("Path does not exist")) { invalidResult }
        check(!settings.isEnabled(project)) { "Settings must remain unchanged after error" }

        // 2. Nested root under src/ with explicit exclusion opt-out
        val nestedResult = commands.configure(
            project = project,
            action = "configure",
            magentoPath = "src",
            enabled = true,
            excludeGeneratedFiles = false,
            projectPath = root.toString()
        )
        check(nestedResult.contains("Result: success")) { nestedResult }
        check(nestedResult.contains("${root}/src")) { nestedResult }
        check(nestedResult.contains("Indexing exclusions: disabled (opt-out)")) { nestedResult }
        check(!settings.isExcludeGeneratedFiles(project)) { "Opt-out must be persisted" }

        // 3. Supported partial checkout under partial/ reports missing dependencies explicitly
        val partialResult = commands.configure(
            project = project,
            action = "configure",
            magentoPath = "partial",
            enabled = true,
            excludeGeneratedFiles = true,
            projectPath = root.toString()
        )
        check(partialResult.contains("Result: success")) { partialResult }
        check(partialResult.contains("Supported partial checkout")) { partialResult }
        check(partialResult.contains("vendor/magento/framework")) { partialResult }
        check(partialResult.contains("composer install")) { partialResult }

        // 4. MCP status inspection reflects partial checkout
        val status = commands.getStatus(project)
        check(status.contains("Supported partial checkout")) { status }
        check(status.contains("missing: vendor/magento/framework") || status.contains("Missing dependencies")) { status }
    }
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoProjectConfigurationCommands", plugin = "com.magento.idea.magento2plugin")
private interface ProjectConfigCommands {
    fun getStatus(project: Project): String
    fun configure(
        project: Project,
        action: String,
        magentoPath: String,
        enabled: Boolean?,
        excludeGeneratedFiles: Boolean?,
        projectPath: String
    ): String
    fun help(): String
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoInspectionCommands", plugin = "com.magento.idea.magento2plugin")
private interface ProjectInspectCommands {
    fun query(project: Project, queryType: String, parametersJson: String): String
}

@Remote("com.magento.idea.magento2plugin.project.Settings", plugin = "com.magento.idea.magento2plugin")
private interface ProjectSettingsRemote {
    fun isEnabled(project: Project): Boolean
    fun getMagentoPath(project: Project): String?
    fun isExcludeGeneratedFiles(project: Project): Boolean
}
