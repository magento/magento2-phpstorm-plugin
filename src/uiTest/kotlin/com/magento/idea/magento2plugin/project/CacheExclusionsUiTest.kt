/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.time.Duration.Companion.minutes

class CacheExclusionsUiTest {
    @Test
    fun nestedCacheExclusionsWorkWithoutPhp() = runWebStormUiTest(
        "file-generators",
        prepareProject = { root ->
            for (path in listOf(".cache/data.json", "vendor/magento/framework/.cache/data.json")) {
                Files.createDirectories(root.resolve(path).parent)
                Files.writeString(root.resolve(path), "{}")
            }
        }
    ) { root ->
        check(!isPluginLoaded("com.jetbrains.php"))
        val project = singleProject()
        val index = utility<CacheFileIndex>().getInstance(project)
        val vfs = utility<CacheVfs>().getInstance()
        val commands = utility<CacheCommands>()
        val status = commands.getStatus(project)
        check(status.contains("vendor/magento/framework/.cache")) { status }
        fun assertMembership(excluded: Boolean) = withReadAction {
            for (path in listOf(".cache/data.json", "vendor/magento/framework/.cache/data.json")) {
                check(index.isExcluded(requireNotNull(vfs.findFileByUrl("file://$root/$path"))) == excluded) { path }
            }
            check(!index.isExcluded(requireNotNull(vfs.findFileByUrl("file://$root/vendor/magento/framework/composer.json"))))
        }
        assertMembership(true)
        commands.configure(project, "apply", false, emptyList())
        ideFrame { waitForIndicators(5.minutes) }
        assertMembership(false)
    }
}

@Remote("com.magento.idea.magento2plugin.mcp.MagentoIndexingExclusionCommands", plugin = "com.magento.idea.magento2plugin")
private interface CacheCommands {
    fun getStatus(project: Project): String
    fun configure(project: Project, action: String, enabled: Boolean?, additionalPaths: List<String>?): String
}

@Remote("com.intellij.openapi.vfs.VirtualFile")
private interface CacheFile

@Remote("com.intellij.openapi.vfs.VirtualFileManager")
private interface CacheVfs {
    fun getInstance(): CacheVfs
    fun findFileByUrl(url: String): CacheFile?
}

@Remote("com.intellij.openapi.roots.ProjectFileIndex")
private interface CacheFileIndex {
    fun getInstance(project: Project): CacheFileIndex
    fun isExcluded(file: CacheFile): Boolean
}
