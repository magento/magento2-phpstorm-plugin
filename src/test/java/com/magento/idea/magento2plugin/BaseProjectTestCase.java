/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.project.Settings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Configure test environment with Magento 2 project.
 */
@ExtendWith({IgnoreKnownCrashesExtension.class, MagentoProjectExtension.class})
public abstract class BaseProjectTestCase {
    protected CodeInsightTestFixture myFixture;
    private static final String testDataProjectPath =  "src/test/resources/testData" //NOPMD
            + java.io.File.separator
            + "project";

    private static final String testDataProjectDirectory = "magento2"; //NOPMD

    private String myTestName;

    @BeforeEach
    public void setTestName(TestInfo testInfo) {
        myTestName = testInfo.getTestMethod().map(java.lang.reflect.Method::getName).orElse("");
    }

    public String getTestName(boolean lowercaseFirstLetter) {
        if (myTestName != null && !myTestName.isEmpty()) {
            String name = myTestName;
            if (name.startsWith("test") && name.length() > 4 && Character.isUpperCase(name.charAt(4))) {
                name = name.substring(4);
            }
            return lowercaseFirstLetter ? com.intellij.openapi.util.text.StringUtil.decapitalize(name) : com.intellij.openapi.util.text.StringUtil.capitalize(name);
        }
        return "";
    }

    protected String getTestDataPath() {
        return new java.io.File( "src/test/resources/testData").getAbsolutePath();
    }

    protected void enablePluginAndReindex() {
        final Settings settings = myFixture.getProject().getService(Settings.class);
        if (settings == null) {
            System.err.println("[DEBUG_LOG] myFixture.getProject().getService(Settings.class) returned null");
            return;
        }
        settings.magentoPath = "/src";
        settings.pluginEnabled = true;
        settings.mftfSupportEnabled = true;
        IndexManager.manualReindex();
        com.intellij.testFramework.EdtTestUtil.runInEdtAndWait(() -> {
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        });
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    protected void disablePluginAndReindex() {
        final Settings settings = myFixture.getProject().getService(Settings.class);
        settings.pluginEnabled = false;
        IndexManager.manualReindex();
        com.intellij.testFramework.EdtTestUtil.runInEdtAndWait(PlatformTestUtil::dispatchAllEventsInIdeEventQueue);
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    protected void disableMftfSupportAndReindex() {
        final Settings settings = myFixture.getProject().getService(Settings.class);
        settings.mftfSupportEnabled = false;
        IndexManager.manualReindex();
        com.intellij.testFramework.EdtTestUtil.runInEdtAndWait(PlatformTestUtil::dispatchAllEventsInIdeEventQueue);
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    protected String prepareFixturePath(
            final String fileName,
            final String fixturesFolderPath
    ) {
        String testName = getTestName(true);
        String className = getClass().getSimpleName().replace("Test", "");

        String path = fixturesFolderPath + className + java.io.File.separator + testName + java.io.File.separator + fileName;
        return path.replace(java.io.File.separator + java.io.File.separator, java.io.File.separator);
    }

}
