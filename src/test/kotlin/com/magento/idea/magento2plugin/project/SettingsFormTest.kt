/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.intellij.openapi.options.ConfigurationException
import com.intellij.util.xmlb.XmlSerializer
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JTextField
import com.magento.idea.magento2plugin.updates.MagentoUpdateSettings
import com.magento.idea.magento2plugin.updates.MagentoUpdatesPanel
import com.magento.idea.magento2plugin.updates.UpdateCheckWindow

class SettingsFormTest : PhysicalMagentoTestCase() {
    private fun createForm(): SettingsForm {
        myFixture.addFileToProject("composer.json", """{"name":"magento/project-community-edition","version":"2.4.8"}""")
        myFixture.addFileToProject("vendor/magento/framework/composer.json", """{"name":"magento/framework"}""")
        val settings = Settings.getInstance(project)
        settings.pluginEnabled = true
        settings.magentoPath = project.basePath
        settings.defaultLicense = Settings.DEFAULT_LICENSE
        return object : SettingsForm(project) {
            override fun reindex() = Unit
        }.apply {
            createComponent()
            reset()
        }
    }

    private inline fun <reified T> field(form: SettingsForm, name: String): T =
        SettingsForm::class.java.getDeclaredField(name).apply { isAccessible = true }.get(form) as T

    fun testPublishedReleasesUrlCanBeEditedAppliedResetAndPersisted() {
        val form = createForm()
        val settings = Settings.getInstance(project)
        val input = field<JTextField>(form, "publishedReleasesUrl")
        assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, input.text)
        assertTrue(input.isEnabled)
        assertFalse(form.isModified)

        val originalState = settings.state!!
        val mirror = "https://releases.example.test/magento?channel=stable"
        input.text = "  $mirror  "
        assertTrue(form.isModified)
        form.apply()
        assertEquals(mirror, settings.getPublishedReleasesUrl())
        assertFalse(form.isModified)
        assertFalse(originalState == settings.state)

        val saved = XmlSerializer.serialize(settings.state!!)
        val reloaded = Settings()
        reloaded.loadState(XmlSerializer.deserialize(saved, Settings.State::class.java))
        assertEquals(mirror, reloaded.getPublishedReleasesUrl())

        input.text = "https://unsaved.example.test/releases"
        form.reset()
        assertEquals(mirror, input.text)
        assertFalse(form.isModified)

        input.text = " "
        form.apply()
        assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, settings.getPublishedReleasesUrl())
        form.reset()
        assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, input.text)
        assertFalse(form.isModified)

        field<JCheckBox>(form, "pluginEnabled").doClick()
        assertFalse(input.isEnabled)
        field<JCheckBox>(form, "pluginEnabled").doClick()
        assertTrue(input.isEnabled)
    }

    fun testInvalidPublishedReleasesUrlCannotBeSaved() {
        val form = createForm()
        val settings = Settings.getInstance(project)
        val input = field<JTextField>(form, "publishedReleasesUrl")
        for (invalid in listOf("relative/path", "file:///tmp/releases.json", "https://", "https://example.test/#page", "https://bad host/releases")) {
            input.text = invalid
            val error = org.junit.Assert.assertThrows(ConfigurationException::class.java) { form.apply() }
            assertTrue(error.messageHtml.toString().contains("Published releases URL"))
            assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, settings.getPublishedReleasesUrl())
        }
    }

    fun testSettingsWithoutPublishedReleasesUrlUseDefault() {
        val settings = Settings()
        settings.publishedReleasesUrl = "https://previous.example.test/releases"
        settings.loadState(Settings.State())
        assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, settings.getPublishedReleasesUrl())
    }

    fun testUpdatePreferencesApplyResetAndPersistIndependentlyOfUctReleaseCatalog() {
        val form = createForm()
        val panel = field<MagentoUpdatesPanel>(form, "updatesPanel")
        val automatic = MagentoUpdatesPanel::class.java.getDeclaredField("automatic").apply { isAccessible = true }.get(panel) as JCheckBox
        val url = MagentoUpdatesPanel::class.java.getDeclaredField("url").apply { isAccessible = true }.get(panel) as JTextField
        val checkWindow = MagentoUpdatesPanel::class.java.getDeclaredField("checkWindow").apply { isAccessible = true }.get(panel) as JComboBox<*>
        val settings = project.getService(MagentoUpdateSettings::class.java)
        assertEquals(UpdateCheckWindow.MONTH, checkWindow.selectedItem)
        assertEquals(listOf("Month", "Week", "2 days", "12 hours"), (0 until checkWindow.itemCount).map { checkWindow.getItemAt(it).toString() })
        assertTrue(checkWindow.toolTipText.contains("does not change how often checks run"))
        assertTrue(checkWindow.toolTipText.contains("UTC"))
        checkWindow.selectedItem = UpdateCheckWindow.TWELVE_HOURS
        assertTrue(form.isModified)
        automatic.isSelected = false
        url.text = "https://updates.example.test/api/v1/"
        assertTrue(form.isModified)
        form.apply()
        assertFalse(settings.state.enabled)
        assertEquals(UpdateCheckWindow.TWELVE_HOURS, settings.state.checkWindow)
        assertEquals("https://updates.example.test/api/v1", settings.state.feedUrl)
        assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, Settings.getInstance(project).getPublishedReleasesUrl())
        assertFalse(form.isModified)
        val saved = XmlSerializer.serialize(settings.state)
        val reloaded = MagentoUpdateSettings()
        reloaded.loadState(XmlSerializer.deserialize(saved, MagentoUpdateSettings.Options::class.java))
        assertFalse(reloaded.state.enabled)
        assertEquals(UpdateCheckWindow.TWELVE_HOURS, reloaded.state.checkWindow)
        assertEquals(settings.state.feedUrl, reloaded.state.feedUrl)
        automatic.isSelected = true
        url.text = "https://unsaved.example.test"
        checkWindow.selectedItem = UpdateCheckWindow.WEEK
        form.reset()
        assertFalse(automatic.isSelected)
        assertEquals(settings.state.feedUrl, url.text)
        assertEquals(UpdateCheckWindow.TWELVE_HOURS, checkWindow.selectedItem)
        assertFalse(form.isModified)
        url.text = "http://insecure.example.test"
        org.junit.Assert.assertThrows(ConfigurationException::class.java) { form.apply() }
        assertEquals("https://updates.example.test/api/v1", settings.state.feedUrl)
        field<JCheckBox>(form, "pluginEnabled").doClick()
        assertFalse(checkWindow.isEnabled)
        field<JCheckBox>(form, "pluginEnabled").doClick()
        assertTrue(checkWindow.isEnabled)
    }

    fun testExistingUpdatePreferencesDefaultToMonthWhenPublicationWindowIsMissing() {
        val settings = MagentoUpdateSettings()
        settings.state.checkWindow = UpdateCheckWindow.TWELVE_HOURS
        settings.loadState(XmlSerializer.deserialize(org.jdom.Element("Options"), MagentoUpdateSettings.Options::class.java))
        assertEquals(UpdateCheckWindow.MONTH, settings.state.checkWindow)
    }
}
