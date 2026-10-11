/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.intellij.openapi.options.ConfigurationException
import com.intellij.util.xmlb.XmlSerializer
import com.magento.idea.magento2plugin.PhysicalMagentoTestCase
import javax.swing.JCheckBox
import javax.swing.JTextField

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
            assertTrue(error.message!!.contains("Published releases URL"))
            assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, settings.getPublishedReleasesUrl())
        }
    }

    fun testSettingsWithoutPublishedReleasesUrlUseDefault() {
        val settings = Settings()
        settings.publishedReleasesUrl = "https://previous.example.test/releases"
        settings.loadState(Settings.State())
        assertEquals(Settings.DEFAULT_PUBLISHED_RELEASES_URL, settings.getPublishedReleasesUrl())
    }
}
