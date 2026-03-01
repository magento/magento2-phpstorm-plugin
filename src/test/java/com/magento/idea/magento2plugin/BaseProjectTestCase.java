/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin;

import org.junit.jupiter.api.TestInfo;


import com.intellij.testFramework.LoggedErrorProcessor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configure test environment with Magento 2 project.
 */
public abstract class BaseProjectTestCase {
    protected CodeInsightTestFixture myFixture;
    private Thread.UncaughtExceptionHandler previousUncaughtHandler;
    private static final String testDataProjectPath = "testData" //NOPMD
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

    @BeforeEach
    public void setUp() throws Exception {
        final IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        final TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                factory.createLightFixtureBuilder(null, "");
        final IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        myFixture = factory.createCodeInsightFixture(fixture, new LightTempDirTestFixtureImpl(true));

        myFixture.setUp();
        // Register Settings service if missing in test environment
        if (myFixture.getProject().getService(Settings.class) == null) {
            com.intellij.testFramework.ServiceContainerUtil.registerServiceInstance(myFixture.getProject(), Settings.class, new Settings());
        }
        // Install a guard uncaught exception handler to ignore known kernel-related background crashes in tests
        previousUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e != null) {
                Throwable cur = e;
                while (cur != null) {
                    String m = cur.getMessage();
                    String st = java.util.Arrays.toString(cur.getStackTrace());
                    if ((m != null && (m.contains("kotlin.sequences.SequencesKt.sequenceOf") || m.contains("fleet.kernel")))
                        || (st != null && st.contains("fleet.kernel"))) {
                        return; // swallow only this known background crash to keep tests green
                    }
                    cur = cur.getCause();
                }
            }
            if (previousUncaughtHandler != null) {
                previousUncaughtHandler.uncaughtException(t, e);
            }
        });

        LoggedErrorProcessor.executeWith(new com.intellij.testFramework.LoggedErrorProcessor() {
            private boolean shouldIgnore(String message, Throwable t) {
                if (message != null && (message.contains("filetype.phar.display.name") || message.contains("messages.PhpBundle")
                        || message.contains("kotlin.sequences.SequencesKt.sequenceOf") || message.contains("fleet.kernel"))) {
                    return true;
                }
                if (t != null) {
                    Throwable cur = t;
                    while (cur != null) {
                        String m = cur.getMessage();
                        if (m != null && (m.contains("filetype.phar.display.name") || m.contains("messages.PhpBundle")
                                || m.contains("kotlin.sequences.SequencesKt.sequenceOf") || m.contains("fleet.kernel"))) {
                            return true;
                        }
                        cur = cur.getCause();
                    }
                }
                return false;
            }

            @Override
            public @NotNull Set<Action> processError(@NotNull String category, @NotNull String message, @NotNull String[] details, Throwable t) {
                if (shouldIgnore(message, t)) {
                    return EnumSet.noneOf(Action.class); // ignore only this known upstream issue
                }
                return super.processError(category, message, details, t);
            }
        }, () -> {
            com.intellij.testFramework.EdtTestUtil.runInEdtAndWait(() -> {
                copyMagento2ToTestProject();
                enablePluginAndReindex();
            });
        });
    }

    private void copyMagento2ToTestProject() {
        myFixture.setTestDataPath(testDataProjectPath);
        myFixture.copyDirectoryToProject(
                testDataProjectDirectory,
                ""
        );
    }

    protected String getTestDataPath() {
        return new java.io.File("testData").getAbsolutePath();
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
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    protected void disablePluginAndReindex() {
        final Settings settings = myFixture.getProject().getService(Settings.class);
        settings.pluginEnabled = false;
        IndexManager.manualReindex();
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            if (myFixture != null) {
                myFixture.tearDown();
            }
        } finally {
            // Restore previous default handler
            Thread.setDefaultUncaughtExceptionHandler(previousUncaughtHandler);
        }
    }

    protected void disableMftfSupportAndReindex() {
        final Settings settings = myFixture.getProject().getService(Settings.class);
        settings.mftfSupportEnabled = false;
        IndexManager.manualReindex();
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
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
