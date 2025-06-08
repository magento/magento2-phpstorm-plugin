/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.createAModuleDialog(function: CreateAModuleDialogFixture.() -> Unit) {
    find<CreateAModuleDialogFixture>(timeout = Duration.ofSeconds(10)).apply(function)
}

@FixtureName("CreateAModuleDialog")
@DefaultXpath("CreateAModuleDialog type", "//div[@class='MyDialog']")
class CreateAModuleDialogFixture(
        remoteRobot: RemoteRobot,
        remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {

    val packageName
        get() = find<JTextFieldFixture>(byXpath("FilteredComboBox", "//div[@name='Package Name']"))

    val moduleName
        get() = find<JTextFieldFixture>(byXpath("FilteredComboBox", "//div[@name='Module Name']"))
}