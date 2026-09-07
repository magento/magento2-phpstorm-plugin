/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui

import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.toolwindows.projectView
import com.intellij.driver.sdk.ui.components.elements.DialogUiComponent
import com.intellij.driver.sdk.waitFor
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.minutes

internal fun Driver.invokeProjectViewAction(
    targetPath: List<String>,
    actionId: String,
    waitForAction: Boolean = true,
) {
    ideFrame {
        projectView {
            val targetName = targetPath.last()
            val matchesTarget: (String) -> Boolean = { nodeName ->
                nodeName == targetName ||
                    nodeName.endsWith("/$targetName") ||
                    nodeName.endsWith("\\$targetName")
            }
            projectViewTree.expandAll(2.minutes)
            projectViewTree.selectRows { nodeNames ->
                nodeNames.filter(matchesTarget)
            }
            waitFor("selected Project view target $targetName") {
                projectViewTree.collectSelectedPaths().any { selectedPath ->
                    matchesTarget(selectedPath.path.last())
                }
            }
            driver.invokeAction(
                actionId,
                now = waitForAction,
                component = projectViewTree.component,
                place = "ProjectViewPopup",
            )
        }
    }
}

internal fun Driver.assertGeneratedFile(
    projectPath: Path,
    relativePath: String,
    vararg expectedContent: String,
) {
    val generatedFile = projectPath.resolve(relativePath)
    invokeAction("SaveAll")
    waitFor("generated file $relativePath with expected content", 2.minutes) {
        generatedFile.exists() && runCatching {
            val content = generatedFile.readText()
            expectedContent.all(content::contains)
        }.getOrDefault(false)
    }

    val content = generatedFile.readText()
    expectedContent.forEach { expected ->
        check(content.contains(expected)) {
            "$relativePath does not contain expected text: $expected\n$content"
        }
    }
}

internal fun cleanupGeneratedFiles(
    projectPath: Path,
    vararg relativePaths: String,
) {
    val normalizedProjectPath = projectPath.toAbsolutePath().normalize()

    relativePaths.forEach { relativePath ->
        val generatedFile = normalizedProjectPath.resolve(relativePath).normalize()
        check(generatedFile.startsWith(normalizedProjectPath)) {
            "Generated file must stay inside the test project: $relativePath"
        }
        generatedFile.deleteIfExists()
    }
}

internal fun DialogUiComponent.clickOkButton() {
    driver.withContext(OnDispatcher.EDT) {
        driver.cast(okButton.component, SwingButton::class).doClick()
    }
}

@Remote("javax.swing.JButton")
private interface SwingButton {
    fun doClick()
}
