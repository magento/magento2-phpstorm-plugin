/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JLabelFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.Locators
import com.intellij.remoterobot.utils.RelativeLocators
import com.magento.idea.magento2plugin.ui.FilteredComboBox

@DefaultXpath(by = "FilteredComboBox type", xpath = "//div[@class='FilteredComboBox']")
@FixtureName("FilteredComboBoxFixture")
open class FilteredComboBoxFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    ComponentFixture(remoteRobot, remoteComponent) {
}
