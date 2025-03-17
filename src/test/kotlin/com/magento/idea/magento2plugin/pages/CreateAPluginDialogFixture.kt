
package com.magento.idea.magento2plugin.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.createAPluginDialog(function: CreateAPluginDialogFixture.() -> Unit) {
    find<CreateAPluginDialogFixture>(timeout = Duration.ofSeconds(10)).apply(function)
}

@FixtureName("CreateAPluginDialog")
@DefaultXpath("CreateAPluginDialog type", "//div[@class='CreateAPluginDialog']")
class CreateAPluginDialogFixture(
        remoteRobot: RemoteRobot,
        remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {

    val targetModule
        get() = find<FilteredComboBoxFixture>(byXpath("FilteredComboBox", "//div[@class='FilteredComboBox']"))

    val className
        get() = find<JTextFieldFixture>(byXpath("FilteredComboBox", "//div[@name='Class Name']"))

    val pluginName
        get() = find<JTextFieldFixture>(byXpath("FilteredComboBox", "//div[@tooltiptext='Plugin name in di.xml']"))
}