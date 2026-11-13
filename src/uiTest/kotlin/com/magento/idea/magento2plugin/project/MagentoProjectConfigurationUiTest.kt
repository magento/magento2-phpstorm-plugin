/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project

import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.getOpenProjects
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.button
import com.intellij.driver.sdk.ui.components.elements.waitForNoOpenedDialogs
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.waitFor
import com.magento.idea.magento2plugin.mcp.McpHttpDriverClient
import com.magento.idea.magento2plugin.ui.runPhpStormUiTest
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.json.JSONObject
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
        val settings = utility<ProjectSettingsRemote>()

        // 1. Establish real MCP HTTP session in running WebStorm
        val bridge = utility<RemoteMcpSessionBridge>()
        val sessionStr = bridge.startSession()
        val (portStr, tokenName, token) = sessionStr.split(":")
        McpHttpDriverClient(portStr.toInt(), tokenName, token, root.toString()).use { mcp ->
            mcp.initialize()

            // 2. Initial state: integration is disabled
            val initialStatus = mcp.callText("configure_magento_project", JSONObject().put("action", "status"))
            check(initialStatus.contains("Integration enabled: no")) { initialStatus }
            check(!settings.isEnabled(project)) { "Plugin should be initially disabled" }

            // 3. Configure and enable through real MCP HTTP endpoint
            val configureResult = mcp.callText("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", ".")
                .put("enabled", true)
                .put("projectPath", root.toString())
            )
            check(configureResult.contains("Result: success")) { configureResult }
            check(configureResult.contains("Integration enabled: yes")) { configureResult }
            check(configureResult.contains("Indexing exclusions: enabled")) { configureResult }
            check(settings.isEnabled(project)) { "Plugin should be enabled after MCP configuration" }
            check(settings.isExcludeGeneratedFiles(project)) { "Exclusions should be enabled by default" }

            // 4. Open Magento settings dialog and verify MCP-configured state in visible UI components
            ideFrame {
                openSettingsDialog()
                settingsDialog {
                    openTreeSettingsSection("Tools", "Magento")
                    val magentoRow = settingsTree.collectExpandedPaths().first { it.path.last() == "Magento" }.row
                    withContext(OnDispatcher.EDT) {
                        val tree = driver.cast(settingsTree.component, SwingTree::class)
                        tree.scrollRowToVisible(magentoRow)
                        tree.setSelectionRow(magentoRow)
                    }

                    waitFor("Magento settings loaded", 10.seconds) {
                        xx("//div[@class='JCheckBox' and @text='Enable Magento integration']").list().isNotEmpty()
                    }

                    content {
                        val pluginEnabledBox = x("//div[@class='JCheckBox' and @text='Enable Magento integration']")
                        val excludeBox = x("//div[@class='JCheckBox' and @text='Exclude generated and runtime files from indexing']")
                        val pathField = xx("//div[@class='TextFieldWithBrowseButton']//div[contains(@class, 'TextField')]", com.intellij.driver.sdk.ui.components.elements.JTextFieldUI::class.java).list().firstOrNull()
                            ?: xx("//div[@class='JTextField']", com.intellij.driver.sdk.ui.components.elements.JTextFieldUI::class.java).list().first { it.text.replace('\\', '/') == root.toString().replace('\\', '/') }

                        val isPluginChecked = withContext(OnDispatcher.EDT) {
                            driver.cast(pluginEnabledBox.component, SwingCheckBox::class).isSelected()
                        }
                        check(isPluginChecked) { "UI checkbox 'Enable Magento integration' should be selected" }

                        val isExcludeChecked = withContext(OnDispatcher.EDT) {
                            driver.cast(excludeBox.component, SwingCheckBox::class).isSelected()
                        }
                        check(isExcludeChecked) { "UI checkbox 'Exclude generated...' should be selected" }

                        check(pathField.text.replace('\\', '/') == root.toString().replace('\\', '/')) {
                            "UI path text field expected ${root.toString()}, found ${pathField.text}"
                        }

                        // UI edit -> save in UI -> MCP status check round trip:
                        // Toggle exclusion checkbox in the UI
                        withContext(OnDispatcher.EDT) {
                            val cb = driver.cast(excludeBox.component, SwingCheckBox::class)
                            cb.doClick(0)
                        }
                        val isExcludeCheckedAfterClick = withContext(OnDispatcher.EDT) {
                            driver.cast(excludeBox.component, SwingCheckBox::class).isSelected()
                        }
                        check(!isExcludeCheckedAfterClick) { "UI checkbox should be unchecked after click" }
                    }

                    withContext(OnDispatcher.EDT) {
                        val apply = driver.cast(applyButton.component, ProjectConfigButton::class)
                        if (apply.isEnabled()) {
                            apply.doClick()
                        }
                        val okComponent = runCatching { okButton.component }.getOrNull()
                            ?: button("OK").component
                        driver.cast(okComponent, ProjectConfigButton::class).doClick()
                    }
                }
                waitForNoOpenedDialogs()
                waitForIndicators(5.minutes)
            }

            // 5. Verify MCP status reflects the updated UI setting over HTTP
            val statusAfterUiEdit = mcp.callText("configure_magento_project", JSONObject().put("action", "status"))
            check(statusAfterUiEdit.contains("Indexing exclusions: disabled (opt-out)")) { statusAfterUiEdit }
        }

        // 6. Close/reopen persistence: close project, reopen without recreating .idea, establish new MCP session
            val initialProject = singleProject()
            bridge.stopSession()
            val closed = bridge.closeAndDispose(initialProject)
            check(closed) { "Initial project should be closed and disposed" }
            waitFor("Project closed", 20.seconds) {
                getOpenProjects().isEmpty()
            }

            bridge.openProject(root.toString())
            waitFor("Project reopened", 45.seconds) {
                getOpenProjects().isNotEmpty()
            }
            val reopenedProject = singleProject()
            ideFrame { waitForIndicators(5.minutes) }

            // Establish a new MCP session for the reopened project
            val reopenedSessionStr = bridge.startSession()
            val (rPort, rName, rToken) = reopenedSessionStr.split(":")
            McpHttpDriverClient(rPort.toInt(), rName, rToken, root.toString()).use { reopenedMcp ->
                reopenedMcp.initialize()

                // Verify saved path, enabled state, and exclusion opt-out persist across project reload
                val statusAfterReopen = reopenedMcp.callText("configure_magento_project", JSONObject().put("action", "status"))
                check(statusAfterReopen.contains("Integration enabled: yes")) { statusAfterReopen }
                check(statusAfterReopen.contains("Resolved Magento path: ${root.toString().replace('\\', '/')}")) { statusAfterReopen }
                check(statusAfterReopen.contains("Indexing exclusions: disabled (opt-out)")) { statusAfterReopen }

                // Verify Settings dialog in reopened project also reflects the persisted state
                ideFrame {
                    val showSettings = service(RemoteShowSettingsUtil::class)
                    withContext(OnDispatcher.EDT) {
                        showSettings.showSettingsDialog(reopenedProject, "Magento")
                    }
                    settingsDialog {
                        waitFor("Magento settings reloaded", 15.seconds) {
                            xx("//div[@class='JCheckBox' and @text='Enable Magento integration']").list().isNotEmpty()
                        }

                        content {
                            val pluginEnabledBox = x("//div[@class='JCheckBox' and @text='Enable Magento integration']")
                            val excludeBox = x("//div[@class='JCheckBox' and @text='Exclude generated and runtime files from indexing']")
                            val pathField = xx("//div[@class='TextFieldWithBrowseButton']//div[contains(@class, 'TextField')]", com.intellij.driver.sdk.ui.components.elements.JTextFieldUI::class.java).list().firstOrNull()
                                ?: xx("//div[@class='JTextField']", com.intellij.driver.sdk.ui.components.elements.JTextFieldUI::class.java).list().first { it.text.replace('\\', '/') == root.toString().replace('\\', '/') }

                            val isPluginChecked = withContext(OnDispatcher.EDT) {
                                driver.cast(pluginEnabledBox.component, SwingCheckBox::class).isSelected()
                            }
                            check(isPluginChecked) { "Reopened: 'Enable Magento integration' should remain selected" }

                            val isExcludeChecked = withContext(OnDispatcher.EDT) {
                                driver.cast(excludeBox.component, SwingCheckBox::class).isSelected()
                            }
                            check(!isExcludeChecked) { "Reopened: 'Exclude generated...' should remain unselected (opt-out)" }

                            check(pathField.text.replace('\\', '/') == root.toString().replace('\\', '/')) {
                                "Reopened: UI path text field expected ${root.toString()}, found ${pathField.text}"
                            }
                        }

                        withContext(OnDispatcher.EDT) {
                            val okComponent = runCatching { okButton.component }.getOrNull()
                                ?: button("OK").component
                            driver.cast(okComponent, ProjectConfigButton::class).doClick()
                        }
                    }
                    waitForNoOpenedDialogs()
                }

                // 7. Repeated configuration over HTTP is idempotent
                val repeatResult = reopenedMcp.callText("configure_magento_project", JSONObject()
                    .put("action", "configure")
                    .put("magentoPath", ".")
                    .put("enabled", true)
                    .put("excludeGeneratedFiles", false)
                    .put("projectPath", root.toString())
                )
                check(repeatResult.contains("no changes required")) { repeatResult }

                // 8. Query fixture module through magento_inspect over HTTP
                val inspectResult = reopenedMcp.callText("magento_inspect", JSONObject()
                    .put("mode", "query")
                    .put("queryType", "module")
                    .put("parametersJson", """{"moduleName":"Acme_Shipping"}""")
                )
                check(inspectResult.contains("app/code/Acme/Shipping")) { inspectResult }
            }
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

            // Create an empty unrecognized directory
            Files.createDirectories(root.resolve("unrecognized"))
        }
    ) { root ->
        check(!isPluginLoaded("com.jetbrains.php"))
        val project = singleProject()
        val settings = utility<ProjectSettingsRemote>()

        val bridge = utility<RemoteMcpSessionBridge>()
        val sessionStr = bridge.startSession()
        val (portStr, tokenName, token) = sessionStr.split(":")
        McpHttpDriverClient(portStr.toInt(), tokenName, token, root.toString()).use { mcp ->
            mcp.initialize()

            // 1. Invalid nonexistent path rejected over HTTP
            val invalidResult = mcp.callText("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", "nonexistent_dir")
                .put("enabled", true)
                .put("projectPath", root.toString())
            )
            check(invalidResult.contains("Result: error")) { invalidResult }
            check(invalidResult.contains("Path does not exist")) { invalidResult }
            check(!settings.isEnabled(project)) { "Settings must remain unchanged after error" }

            // 2. Unrecognized directory rejected over HTTP
            val unrecognizedResult = mcp.callText("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", "unrecognized")
                .put("enabled", true)
                .put("projectPath", root.toString())
            )
            check(unrecognizedResult.contains("Result: error")) { unrecognizedResult }
            check(unrecognizedResult.contains("no recognized Magento structure")) { unrecognizedResult }
            check(!settings.isEnabled(project)) { "Settings must remain unchanged after unrecognized directory" }

            // 3. Nested root under src/ with explicit exclusion opt-out over HTTP
            val nestedResult = mcp.callText("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", "src")
                .put("enabled", true)
                .put("excludeGeneratedFiles", false)
                .put("projectPath", root.toString())
            )
            check(nestedResult.contains("Result: success")) { nestedResult }
            check(nestedResult.contains("${root}/src")) { nestedResult }
            check(nestedResult.contains("Indexing exclusions: disabled (opt-out)")) { nestedResult }
            check(!settings.isExcludeGeneratedFiles(project)) { "Opt-out must be persisted" }

            // 4. Supported partial checkout under partial/ reports missing dependencies explicitly
            val partialResult = mcp.callText("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", "partial")
                .put("enabled", true)
                .put("excludeGeneratedFiles", true)
                .put("projectPath", root.toString())
            )
            check(partialResult.contains("Result: success")) { partialResult }
            check(partialResult.contains("Supported partial checkout")) { partialResult }
            check(partialResult.contains("vendor/magento/framework")) { partialResult }
            check(partialResult.contains("composer install")) { partialResult }

            // 5. MCP status inspection reflects partial checkout over HTTP
            val status = mcp.callText("configure_magento_project", JSONObject().put("action", "status"))
            check(status.contains("Supported partial checkout")) { status }
            check(status.contains("missing: vendor/magento/framework") || status.contains("Missing dependencies")) { status }
        }
    }

    @Test
    fun mcpProjectConfigurationAndInspectionInPhpStorm() = runPhpStormUiTest(
        "file-generators",
        enableSupport = false,
        prepareProject = { root ->
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
        check(isPluginLoaded("com.jetbrains.php")) { "PhpStorm must run with PHP plugin" }
        check(isPluginLoaded("com.intellij.mcpServer")) { "MCP plugin must be loaded" }

        val bridge = utility<RemoteMcpSessionBridge>()
        val sessionStr = bridge.startSession()
        val (portStr, tokenName, token) = sessionStr.split(":")
        McpHttpDriverClient(portStr.toInt(), tokenName, token, root.toString()).use { mcp ->
            mcp.initialize()

            // Configure via real MCP HTTP endpoint in PhpStorm
            val configureResult = mcp.callText("configure_magento_project", JSONObject()
                .put("action", "configure")
                .put("magentoPath", ".")
                .put("enabled", true)
                .put("excludeGeneratedFiles", true)
                .put("projectPath", root.toString())
            )
            check(configureResult.contains("Result: success")) { configureResult }
            check(configureResult.contains("Integration enabled: yes")) { configureResult }

            // Verify status via HTTP
            val status = mcp.callText("configure_magento_project", JSONObject().put("action", "status"))
            check(status.contains("Integration enabled: yes")) { status }

            // Verify PhpStorm UI reflects the MCP configuration
            ideFrame {
                val showSettings = service(RemoteShowSettingsUtil::class)
                withContext(OnDispatcher.EDT) {
                    showSettings.showSettingsDialog(project, "Frameworks")
                }
                settingsDialog {
                    waitFor("PhpStorm Magento framework settings loaded", 15.seconds) {
                        xx("//div[@class='JCheckBox' and @text='Enable Magento integration']").list().isNotEmpty()
                    }

                    content {
                        val pluginEnabledBox = x("//div[@class='JCheckBox' and @text='Enable Magento integration']")
                        val isPluginChecked = withContext(OnDispatcher.EDT) {
                            driver.cast(pluginEnabledBox.component, SwingCheckBox::class).isSelected()
                        }
                        check(isPluginChecked) { "UI checkbox 'Enable Magento integration' should be selected in PhpStorm" }
                    }

                    withContext(OnDispatcher.EDT) {
                        val okComponent = runCatching { okButton.component }.getOrNull()
                            ?: button("OK").component
                        driver.cast(okComponent, ProjectConfigButton::class).doClick()
                    }
                }
                waitForNoOpenedDialogs()
                waitForIndicators(5.minutes)
            }

            // Inspect module via MCP HTTP in PhpStorm
            val inspectResult = mcp.callText("magento_inspect", JSONObject()
                .put("mode", "query")
                .put("queryType", "module")
                .put("parametersJson", """{"moduleName":"Acme_Shipping"}""")
            )
            check(inspectResult.contains("app/code/Acme/Shipping")) { inspectResult }
        }
    }
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoMcpSessionBridge", plugin = "com.magento.idea.magento2plugin")
private interface RemoteMcpSessionBridge {
    fun startSession(): String
    fun stopSession()
    fun closeAndDispose(project: Project): Boolean
    fun openProject(path: String): Project?
}

@Remote("com.magento.idea.magento2plugin.project.Settings", plugin = "com.magento.idea.magento2plugin")
private interface ProjectSettingsRemote {
    fun isEnabled(project: Project): Boolean
    fun getMagentoPath(project: Project): String?
    fun isExcludeGeneratedFiles(project: Project): Boolean
}

@Remote("javax.swing.JCheckBox")
private interface SwingCheckBox {
    fun isSelected(): Boolean
    fun setSelected(selected: Boolean)
    fun doClick(pressTime: Int)
}

@Remote("javax.swing.AbstractButton")
private interface ProjectConfigButton {
    fun isEnabled(): Boolean
    fun doClick()
}

@Remote("javax.swing.JTree")
private interface SwingTree {
    fun setSelectionRow(row: Int)
    fun scrollRowToVisible(row: Int)
}

@Remote("com.intellij.openapi.options.ShowSettingsUtil")
private interface RemoteShowSettingsUtil {
    fun showSettingsDialog(project: Project?, toSelect: String)
}

