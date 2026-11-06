/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.startup;

import com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightingSettingsPerFile;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.PlatformTestUtil;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReaderModeHighlightingResetTest extends BaseProjectTestCase {

    private static final FileHighlightingSetting SKIP = FileHighlightingSetting.SKIP_INSPECTION;
    private static final FileHighlightingSetting ON = FileHighlightingSetting.FORCE_HIGHLIGHTING;

    private File magentoRoot;
    private String magentoPath;
    private Settings settings;
    private HighlightingSettingsPerFile highlighting;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        settings = Settings.getInstance(getProject());
        settings.pluginEnabled = true;
        magentoRoot = FileUtil.createTempDirectory("magento", "", true);
        VfsRootAccess.allowRootAccess(getTestRootDisposable(), magentoRoot.getPath());
        assertTrue(new File(magentoRoot, "vendor/magento/framework").mkdirs());
        magentoPath = FileUtil.toSystemIndependentName(magentoRoot.getPath());
        settings.magentoPath = magentoPath;
        settings.myMagentoFolders = null;
        highlighting = HighlightingSettingsPerFile.getInstance(getProject());
        PropertiesComponent.getInstance(getProject())
                .unsetValue(ReaderModeHighlightingReset.DONE_KEY);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            settings.myMagentoFolders = null;
            PropertiesComponent.getInstance(getProject())
                    .unsetValue(ReaderModeHighlightingReset.DONE_KEY);
        } finally {
            super.tearDown();
        }
    }

    public void testEnablesInspectionsInEditableRootsOnly() throws Exception {
        final PsiFile packages = skipInspection("app/code/Foo/Reset/etc/di.xml");
        final PsiFile design = skipInspection("app/design/frontend/Foo/reset/theme.xml");
        final PsiFile libWeb = skipInspection("lib/web/reset/config.xml");
        final PsiFile vendor = skipInspection("vendor/foo/reset/etc/di.xml");
        final PsiFile other = skipInspection("dev/reset/config.xml");

        runReset();

        assertEquals(ON, setting(packages));
        assertEquals(ON, setting(design));
        assertEquals(ON, setting(libWeb));
        assertEquals(SKIP, setting(vendor));
        assertEquals(SKIP, setting(other));
    }

    public void testEnablesInspectionsInMarkedFolder() throws Exception {
        final PsiFile file = skipInspection("packages/reset/etc/di.xml");

        settings.myMagentoFolders = List.of(
                file.getVirtualFile().getParent().getParent().getUrl()
        );
        runReset();

        assertEquals(ON, setting(file));
    }

    public void testKeepsReadOnlyFile() throws Exception {
        final PsiFile file = skipInspection("app/code/Foo/ReadOnly/etc/di.xml");

        WriteAction.runAndWait(() -> file.getVirtualFile().setWritable(false));
        runReset();

        assertEquals(SKIP, setting(file));
    }

    public void testKeepsEntriesWithAnotherLevel() throws Exception {
        final PsiFile file = file("app/code/Foo/Level/etc/di.xml");

        highlighting.setHighlightingSettingForRoot(
                file,
                FileHighlightingSetting.SKIP_HIGHLIGHTING
        );
        runReset();

        assertEquals(FileHighlightingSetting.SKIP_HIGHLIGHTING, setting(file));
    }

    public void testSkipsEntryOfDeletedFile() throws Exception {
        final PsiFile deleted = skipInspection("app/code/Foo/Deleted/etc/di.xml");
        final PsiFile kept = skipInspection("app/code/Foo/Kept/etc/di.xml");

        WriteAction.runAndWait(() -> deleted.getVirtualFile().delete(this));
        runReset();

        assertEquals(ON, setting(kept));
    }

    public void testRunsOncePerProject() throws Exception {
        runReset();
        final PsiFile chosenByUser = skipInspection("app/code/Foo/Later/etc/di.xml");

        runReset();

        assertEquals(SKIP, setting(chosenByUser));
    }

    public void testWaitsUntilThePluginIsEnabled() throws Exception {
        final PsiFile file = skipInspection("app/code/Foo/Disabled/etc/di.xml");

        settings.pluginEnabled = false;
        runReset();
        assertEquals(SKIP, setting(file));

        settings.pluginEnabled = true;
        runReset();
        assertEquals(ON, setting(file));
    }

    public void testWaitsUntilTheMagentoPathIsSet() throws Exception {
        final PsiFile file = skipInspection("app/code/Foo/NoPath/etc/di.xml");

        settings.magentoPath = null;
        runReset();
        assertEquals(SKIP, setting(file));

        settings.magentoPath = magentoPath;
        runReset();
        assertEquals(ON, setting(file));
    }

    public void testWaitsUntilTheMagentoPathIsValid() throws Exception {
        final PsiFile file = skipInspection("app/code/Foo/WrongPath/etc/di.xml");

        settings.magentoPath = magentoPath + "/wrong";
        runReset();
        assertEquals(SKIP, setting(file));

        settings.magentoPath = magentoPath;
        runReset();
        assertEquals(ON, setting(file));
    }

    public void testEnablesInspectionsInEveryRootOfAFile() throws Exception {
        final PsiFile file = file("app/code/Foo/Roots/view/template.phtml", "<?php echo 1; ?><b>x</b>");
        final List<PsiFile> roots = file.getViewProvider().getAllFiles();
        final PsiFile lastRoot = roots.get(roots.size() - 1);

        assertTrue(roots.size() > 1);
        highlighting.setHighlightingSettingForRoot(lastRoot, SKIP);
        runReset();

        assertEquals(ON, setting(lastRoot));
    }

    public void testStartupActivityRunsTheReset() {
        final PsiFile file = skipInspection("app/code/Foo/Startup/etc/di.xml");

        new CheckIfMagentoPathIsValidActivity().runActivity(getProject());
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

        assertEquals(ON, setting(file));
    }

    public void testMarkingAFolderEnablesInspectionsUnderIt() throws Exception {
        runReset();
        final PsiFile marked = skipInspection("vendor/foo/marked/etc/di.xml");
        final PsiFile other = skipInspection("vendor/foo/other/etc/di.xml");
        final PsiFile chosenByUser = skipInspection("app/code/Foo/Chosen/etc/di.xml");
        final VirtualFile folder = marked.getVirtualFile().getParent().getParent();

        settings.myMagentoFolders = new ArrayList<>(List.of(folder.getUrl()));
        ReaderModeHighlightingReset.runForFolder(getProject(), folder);
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

        assertEquals(ON, setting(marked));
        assertEquals(SKIP, setting(other));
        assertEquals(SKIP, setting(chosenByUser));
    }

    private void runReset()
            throws ExecutionException, InterruptedException, TimeoutException, IOException {
        final Future<?> future = ApplicationManager.getApplication().executeOnPooledThread(
                () -> ReaderModeHighlightingReset.run(getProject())
        );
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);

        while (!future.isDone() && System.nanoTime() < deadline) {
            PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
        }
        future.get(1, TimeUnit.SECONDS);
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
    }

    private PsiFile skipInspection(final String path) {
        final PsiFile file = file(path);

        highlighting.setHighlightingSettingForRoot(file, SKIP);

        return file;
    }

    private PsiFile file(final String path) {
        return file(path, "<config/>");
    }

    private PsiFile file(final String path, final String content) {
        final File file = new File(magentoRoot, path);

        try {
            FileUtil.writeToFile(file, content);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        final VirtualFile virtualFile = LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(file);

        assertNotNull(virtualFile);

        return PsiManager.getInstance(getProject()).findFile(virtualFile);
    }

    private FileHighlightingSetting setting(final PsiFile file) {
        return highlighting.getHighlightingSettingForRoot(file);
    }
}
