/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.singleProject
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.writeText

class MagentoDdevUiTest {
    @Test
    fun discoversDdevWithoutPhpSupportOrRunningContainers() = runWebStormUiTest(
        "file-generators",
        prepareProject = { root ->
            Files.createDirectories(root.resolve(".ddev"))
            root.resolve(".ddev/config.yaml").writeText("name: commerce-ui\ntype: magento2\ndocroot: pub\nphp_version: '8.3'\nworking_dir: {web: /var/www/html}\n")
        }
    ) { root ->
        check(!isPluginLoaded("com.jetbrains.php"))
        check(isPluginLoaded("com.intellij.mcpServer"))
        val description = utility<CliToolQueries>().describeCliEnvironment(singleProject())
        check(description.contains("DDEV configuration: ${root.resolve(".ddev/config.yaml")}")) { description }
        check(description.contains("commerce-ui") && description.contains("8.3")) { description }
        check(description.contains(root.toString())) { description }
        check(description.contains("ddev exec --raw --service=web --project=commerce-ui --dir=/var/www/html -- php -v")) { description }
        check(description.contains("PhpStorm's Terminal")) { description }
    }
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoCliToolQueries", plugin = "com.magento.idea.magento2plugin")
private interface CliToolQueries {
    fun describeCliEnvironment(project: Project): String
}
