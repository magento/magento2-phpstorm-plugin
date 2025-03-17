package com.magento.idea.magento2plugin.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor

fun RemoteRobot.contextMenuItem(text: String): ContextMenuItemFixture {
    val xpath = byXpath("text '$text'", "//div[@class='ActionMenuItem' and @text='$text']")
    waitFor {
        findAll<ContextMenuItemFixture>(xpath).isNotEmpty()
    }
    return findAll<ContextMenuItemFixture>(xpath).first()
}

fun RemoteRobot.contextMenu(text: String): ContextMenuItemFixture {
    val xpath = byXpath("text '$text'", "//div[@class='ActionMenu' and @text='$text']")
    waitFor {
        findAll<ContextMenuItemFixture>(xpath).isNotEmpty()
    }
    return findAll<ContextMenuItemFixture>(xpath).first()
}

@FixtureName("ContextMenuItem")
class ContextMenuItemFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ComponentFixture(remoteRobot, remoteComponent)