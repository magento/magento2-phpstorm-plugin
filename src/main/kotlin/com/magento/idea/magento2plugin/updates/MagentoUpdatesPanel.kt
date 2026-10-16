/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

/** Shared by the PHP and Tools settings pages. Uses no PHP or UCT classes. */
class MagentoUpdatesPanel(private val project: Project) : JPanel(BorderLayout(0, 6)) {
    private val automatic = JBCheckBox("Check for releases and security advisories automatically")
    private val url = JBTextField()
    private val checkWindow = ComboBox(UpdateCheckWindow.entries.toTypedArray())
    private val check = JButton("Check Magento updates")

    init {
        add(automatic, BorderLayout.NORTH)
        checkWindow.toolTipText = "<html>Show releases and security advisories published within the selected period.<br>" +
            "Choose a shorter period if you work on this project often, or a longer one to catch up after time away.<br>" +
            "Applies to automatic and manual checks; it does not change how often checks run.<br>" +
            "When only a publication date is available, include that date if it overlaps the period (UTC).</html>"
        add(JPanel(BorderLayout(0, 6)).apply {
            add(JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
                add(JLabel("Published within: ").apply {
                    labelFor = checkWindow
                    toolTipText = checkWindow.toolTipText
                })
                add(checkWindow)
            }, BorderLayout.NORTH)
            add(JPanel(BorderLayout(6, 0)).apply {
                add(JLabel("Updates feed URL:").apply { labelFor = url }, BorderLayout.WEST)
                add(url, BorderLayout.CENTER)
            }, BorderLayout.CENTER)
        }, BorderLayout.CENTER)
        add(JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply { add(check) }, BorderLayout.SOUTH)
        url.toolTipText = "magento.watch API or a compatible mirror. Only the detected distribution and version are sent."
        check.toolTipText = "Check using the saved Magento root, updates feed and publication window settings."
        check.addActionListener { project.getService(MagentoUpdateService::class.java).checkForUpdates(true) }
        reset()
    }

    private fun options() = project.getService(MagentoUpdateSettings::class.java).state
    fun isModified(): Boolean = automatic.isSelected != options().enabled ||
        checkWindow.selectedItem != options().checkWindow ||
        MagentoUpdateSettings.normalizeFeedUrl(url.text) != MagentoUpdateSettings.normalizeFeedUrl(options().feedUrl)

    @Throws(ConfigurationException::class)
    fun validateSettings() {
        if (!MagentoUpdateSettings.isValidFeedUrl(url.text)) throw ConfigurationException("Updates feed URL must use HTTPS, without credentials, query or fragment. HTTP is allowed on localhost.")
    }

    fun apply() {
        options().enabled = automatic.isSelected
        options().feedUrl = MagentoUpdateSettings.normalizeFeedUrl(url.text)
        options().checkWindow = checkWindow.selectedItem as UpdateCheckWindow
        project.getService(MagentoUpdateService::class.java).checkForUpdates()
    }

    fun reset() {
        automatic.isSelected = options().enabled
        url.text = MagentoUpdateSettings.normalizeFeedUrl(options().feedUrl)
        checkWindow.selectedItem = options().checkWindow
    }

    fun setIntegrationEnabled(enabled: Boolean) {
        automatic.isEnabled = enabled
        url.isEnabled = enabled
        checkWindow.isEnabled = enabled
        check.isEnabled = enabled
    }
}
