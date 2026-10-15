/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.magento.idea.magento2plugin.project.Settings

class CheckMagentoUpdatesAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project?.let { Settings.isEnabled(it) } == true
    }
    override fun actionPerformed(event: AnActionEvent) {
        event.project?.getService(MagentoUpdateService::class.java)?.checkForUpdates(true)
    }
}
