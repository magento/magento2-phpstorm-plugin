/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.util.GetProjectBasePath;
import java.io.File;

/**
 * Configure test environment with Magento 2 project
 */
abstract public class BaseProjectTestCase extends BasePlatformTestCase {
    private static final String testDataProjectPath = "testData" + File.separator + "project";
    private static final String testDataProjectDirectory = "magento2";

    @Override
    protected void setUp() throws Exception {
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
        Settings settings = Settings.getInstance(myFixture.getProject());
        settings.pluginEnabled = true;
        settings.mftfSupportEnabled = true;
        IndexManager.manualReindex();
    }

    protected void disablePluginAndReindex() {
        Settings settings = Settings.getInstance(myFixture.getProject());
        settings.pluginEnabled = false;
        IndexManager.manualReindex();
    }

    protected void disableMftfSupportAndReindex() {
        Settings settings = Settings.getInstance(myFixture.getProject());
        settings.mftfSupportEnabled = false;
        IndexManager.manualReindex();
    }

    protected String prepareFixturePath(String fileName, String fixturesFolderPath) {
        return fixturesFolderPath + getClass().getSimpleName().replace("Test", "") + File.separator + name() + File.separator + fileName;
    }

    private String name() {
        return StringUtil.trimEnd(getTestName(true), "Test");
    }
}
