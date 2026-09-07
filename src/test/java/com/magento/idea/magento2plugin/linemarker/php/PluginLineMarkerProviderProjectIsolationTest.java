/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.EmptyModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex;
import com.magento.idea.magento2plugin.stubs.indexes.data.PluginData;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PluginLineMarkerProviderProjectIsolationTest extends BaseProjectTestCase {

    private static final String TARGET_CLASS = "Magento\\Theme\\Block\\PluginClass";
    private static final String TARGET_CLASS_SOURCE = """
            <?php

            namespace Magento\\Theme\\Block;

            class PluginClass
            {
                public function someMethod()
                {
                }
            }
            """;

    private Disposable secondProjectDisposable;
    private Project secondProject;
    private Path secondProjectPath;

    /**
     * Reproduces the project-specific PSI leak from a shared file-index value.
     */
    public void testPluginLineMarkersMustRemainProjectIsolated() throws Exception {
        myFixture.addFileToProject("PluginClass.php", TARGET_CLASS_SOURCE);
        secondProject = createSecondProject();

        enablePlugin(secondProject);
        IndexManager.manualReindex();
        waitForIndexes(getProject());
        waitForIndexes(secondProject);

        final PhpClass firstTargetClass = getTargetClass(getProject());
        final PhpClass secondTargetClass = getTargetClass(secondProject);
        final PluginData firstProjectData = getPluginData(getProject());
        final PluginData secondProjectData = getPluginData(secondProject);

        assertSame(
                "FileBasedIndex should expose the shared value involved in the reported leak",
                firstProjectData,
                secondProjectData
        );
        assertPluginDataIsImmutable(firstProjectData);

        assertCanCreatePluginLineMarker(firstTargetClass);
        assertCanCreatePluginLineMarker(secondTargetClass);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (secondProjectDisposable != null) {
                Disposer.dispose(secondProjectDisposable);
                secondProjectDisposable = null;
                secondProject = null;
            }
            if (secondProjectPath != null) {
                FileUtil.delete(secondProjectPath.toFile());
                secondProjectPath = null;
            }
        } finally {
            super.tearDown();
        }
    }

    private Project createSecondProject() throws IOException {
        final File sourceProject = new File(getAbsoluteProjectPath(
                TEST_DATA_ROOT + "/project/magento2"
        ));

        secondProjectPath = Files.createTempDirectory("second-magento-project-");
        FileUtil.copyDir(sourceProject, secondProjectPath.toFile());
        Files.writeString(secondProjectPath.resolve("PluginClass.php"), TARGET_CLASS_SOURCE);
        secondProjectDisposable = Disposer.newDisposable("second Magento test project");

        final Project project = PlatformTestUtil.loadAndOpenProject(
                secondProjectPath,
                secondProjectDisposable
        );
        final VirtualFile contentRoot = LocalFileSystem.getInstance()
                .refreshAndFindFileByNioFile(secondProjectPath);

        assertNotNull("Unable to resolve second project content root", contentRoot);
        WriteAction.runAndWait(() -> {
            final Module module = ModuleManager.getInstance(project).newModule(
                    secondProjectPath.resolve("secondMagentoProject.iml"),
                    EmptyModuleType.EMPTY_MODULE
            );
            ModuleRootModificationUtil.addContentRoot(module, contentRoot);
        });

        return project;
    }

    private void enablePlugin(final Project project) {
        final Settings settings = Settings.getInstance(project);

        settings.magentoPath = "/src";
        settings.pluginEnabled = true;
    }

    private void waitForIndexes(final Project project) {
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        IndexingTestUtil.waitUntilIndexesAreReady(project);
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
    }

    private PhpClass getTargetClass(final Project project) {
        return assertOneElement(
                PhpIndex.getInstance(project).getClassesByFQN(TARGET_CLASS)
        );
    }

    private PluginData getPluginData(final Project project) {
        final List<Set<PluginData>> values = FileBasedIndex.getInstance().getValues(
                PluginIndex.KEY,
                TARGET_CLASS,
                GlobalSearchScope.allScope(project)
        );

        return assertOneElement(assertOneElement(values));
    }

    private void assertPluginDataIsImmutable(final PluginData pluginData) {
        assertTrue(
                "Indexed PluginData must be final",
                Modifier.isFinal(pluginData.getClass().getModifiers())
        );

        for (final Field field : pluginData.getClass().getDeclaredFields()) {
            assertTrue(
                    "Indexed PluginData must not retain mutable project-specific state: "
                            + field.getName(),
                    Modifier.isFinal(field.getModifiers())
            );
        }
    }

    private void assertCanCreatePluginLineMarker(final PhpClass targetClass) {
        final List<LineMarkerInfo<?>> lineMarkers = new ArrayList<>();

        new PluginLineMarkerProvider().collectSlowLineMarkers(
                List.of(targetClass),
                lineMarkers
        );

        assertFalse("Expected a plugin line marker", lineMarkers.isEmpty());
    }
}
