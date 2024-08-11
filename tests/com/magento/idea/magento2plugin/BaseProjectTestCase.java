/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.project.Settings;

/**
 * Configure test environment with Magento 2 project.
 */
public abstract class BaseProjectTestCase extends BasePlatformTestCase {
    private static final String testDataProjectPath = "testData" //NOPMD
            + File.separator
            + "project";

    private static final String testDataProjectDirectory = "magento2"; //NOPMD

    @Override
    public void setUp() throws Exception {
        super.setUp();
        copyMagento2ToTestProject();
        enablePluginAndReindex();
    }

    private void copyMagento2ToTestProject() {
        myFixture.setTestDataPath(testDataProjectPath);
        myFixture.copyDirectoryToProject(
                testDataProjectDirectory,
                ""
        );
    }

    @Override
    protected String getTestDataPath() {
        //configure specific test data in your test.
        return "testData";
    }

    protected void enablePluginAndReindex() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath = "/src";
        settings.pluginEnabled = true;
        settings.mftfSupportEnabled = true;
        IndexManager.manualReindex();
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    protected void disablePluginAndReindex() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.pluginEnabled = false;
        IndexManager.manualReindex();
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    protected void disableMftfSupportAndReindex() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.mftfSupportEnabled = false;
        IndexManager.manualReindex();
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    protected String prepareFixturePath(
            final String fileName,
            final String fixturesFolderPath
    ) {
        return fixturesFolderPath + getClass().getSimpleName().replace("Test", "")
                + File.separator
                + name()
                + File.separator
                + fileName;
    }

    private String name() {
        return StringUtil.trimEnd(getTestName(true), "Test");
    }

}
