package com.magento.idea.magento2plugin.pages;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.UtilsKt.hasAnyComponent;


@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
@FixtureName(name = "Welcome Frame")
public class WelcomeFrameFixture extends CommonContainerFixture {
    public WelcomeFrameFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public ComponentFixture createNewProjectLink() {
        return welcomeFrameLink("New Project");
    }

    public ComponentFixture importProjectLink() {
        return welcomeFrameLink("Get from VCS");
    }

    private ComponentFixture welcomeFrameLink(String text) {
        if (hasAnyComponent(this, byXpath("//div[@class='NewRecentProjectPanel']"))) {
            return find(ComponentFixture.class, byXpath("//div[@class='JBOptionButton' and @text='" + text + "']"));
        }
        return find(
                ComponentFixture.class,
                byXpath("//div[(@class='MainButton' and @text='"+ text +"') or (@accessiblename='"+ text +"' and @class='JButton')]")
        );
    }
}
