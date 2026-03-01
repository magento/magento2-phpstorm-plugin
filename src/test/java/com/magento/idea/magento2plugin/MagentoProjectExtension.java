/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin;

import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.project.Settings;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;

/**
 * JUnit 5 Extension to manage Magento 2 project lifecycle in tests.
 */
public class MagentoProjectExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(MagentoProjectExtension.class);
    private static final String FIXTURE = "myFixture";

    private static final String testDataProjectPath = "testData/project";
    private static final String testDataProjectDirectory = "magento2";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        final IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        final TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                factory.createLightFixtureBuilder(null, "");
        final IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        CodeInsightTestFixture myFixture = factory.createCodeInsightFixture(fixture, new LightTempDirTestFixtureImpl(true));

        com.intellij.testFramework.EdtTestUtil.runInEdtAndWait(() -> {
            try {
                myFixture.setUp();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Register Settings service if missing
        if (myFixture.getProject().getService(Settings.class) == null) {
            ServiceContainerUtil.registerServiceInstance(myFixture.getProject(), Settings.class, new Settings());
        }

        com.intellij.testFramework.EdtTestUtil.runInEdtAndWait(() -> {
            myFixture.setTestDataPath(testDataProjectPath);
            myFixture.copyDirectoryToProject(testDataProjectDirectory, "");

            final Settings settings = myFixture.getProject().getService(Settings.class);
            if (settings != null) {
                settings.magentoPath = "/src";
                settings.pluginEnabled = true;
                settings.mftfSupportEnabled = true;
                IndexManager.manualReindex();
                PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
                try {
                    IndexingTestUtil.waitUntilIndexesAreReady(myFixture.getProject());
                } catch (Exception e) {
                    // Ignore for now
                }
            }
        });

        context.getStore(NAMESPACE).put(FIXTURE, myFixture);

        // Inject into the test instance if it has a myFixture field
        Object testInstance = context.getRequiredTestInstance();
        Class<?> clazz = testInstance.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField("myFixture");
                field.setAccessible(true);
                field.set(testInstance, myFixture);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        CodeInsightTestFixture myFixture = context.getStore(NAMESPACE).get(FIXTURE, CodeInsightTestFixture.class);
        if (myFixture != null) {
            com.intellij.testFramework.EdtTestUtil.runInEdtAndWait(() -> {
                try {
                    myFixture.tearDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == CodeInsightTestFixture.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(FIXTURE, CodeInsightTestFixture.class);
    }
}
