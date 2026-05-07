/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin;

import com.intellij.testFramework.LoggedErrorProcessor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configure test environment with Magento 2 project.
 */
public abstract class BaseProjectTestCase extends BasePlatformTestCase {
    protected static final String TEST_DATA_ROOT = "src" + File.separator
            + "test" + File.separator
            + "testData";

    private Thread.UncaughtExceptionHandler previousUncaughtHandler;
    private static final String testDataProjectPath = TEST_DATA_ROOT //NOPMD
            + File.separator
            + "project";

    private static final String testDataProjectDirectory = "magento2"; //NOPMD

    @Override
    public void setUp() throws Exception {
        VfsRootAccess.allowRootAccess(
                getTestRootDisposable(),
                getAbsoluteProjectPath(".intellijPlatform")
        );

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
            BaseProjectTestCase.super.setUp();
            copyMagento2ToTestProject();
            waitForIndexes();
            enablePluginAndReindex();
        });
    }

    private void copyMagento2ToTestProject() {
        setFixtureTestDataPath(testDataProjectPath);
        myFixture.copyDirectoryToProject(
                testDataProjectDirectory,
                ""
        );
    }

    @Override
    protected String getTestDataPath() {
        //configure specific test data in your test.
        return getAbsoluteTestDataPath(TEST_DATA_ROOT);
    }

    protected void enablePluginAndReindex() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.magentoPath = "/src";
        settings.pluginEnabled = true;
        settings.mftfSupportEnabled = true;
        IndexManager.manualReindex();
        waitForIndexes();
    }

    protected void disablePluginAndReindex() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.pluginEnabled = false;
        IndexManager.manualReindex();
        waitForIndexes();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            // Restore previous default handler
            Thread.setDefaultUncaughtExceptionHandler(previousUncaughtHandler);
        }
    }

    protected void disableMftfSupportAndReindex() {
        final Settings settings = Settings.getInstance(myFixture.getProject());
        settings.mftfSupportEnabled = false;
        IndexManager.manualReindex();
        waitForIndexes();
    }

    private void waitForIndexes() {
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
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

    protected void setFixtureTestDataPath(final String testDataPath) {
        final String absoluteTestDataPath = getAbsoluteTestDataPath(testDataPath);
        VfsRootAccess.allowRootAccess(getTestRootDisposable(), absoluteTestDataPath);
        myFixture.setTestDataPath(absoluteTestDataPath);
    }

    protected String getAbsoluteTestDataPath(final String testDataPath) {
        return getAbsoluteProjectPath(testDataPath);
    }

    protected String getAbsoluteProjectPath(final String path) {
        return FileUtil.toSystemIndependentName(
                Paths.get(path).toAbsolutePath().normalize().toString()
        );
    }

}
