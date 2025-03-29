/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import java.time.Duration

fun ContainerFixture.dialog(
        title: String,
        timeout: Duration = Duration.ofSeconds(20),
        function: DialogFixture.() -> Unit = {}): DialogFixture = step("Search for dialog with title $title") {
    find<DialogFixture>(DialogFixture.byTitle(title), timeout).apply(function)
}

fun ContainerFixture.errorDialog(
    timeout: Duration = Duration.ofSeconds(20),
    function: DialogFixture.() -> Unit = {}): DialogFixture = step("Search for error dialog") {
    find<DialogFixture>(DialogFixture.getJDialog("Error"), timeout).apply(function)
}

@FixtureName("Dialog")
class DialogFixture(
        remoteRobot: RemoteRobot,
        remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {

    companion object {
        @JvmStatic
        fun byTitle(title: String) = byXpath("title $title", "//div[@title='$title' and @class='MyDialog']")

        @JvmStatic
        fun getJDialog(title: String) = byXpath("title $title", "//div[@title='$title' and @class='JDialog']")
    }
}