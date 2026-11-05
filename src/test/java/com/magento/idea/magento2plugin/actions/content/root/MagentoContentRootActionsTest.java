/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.content.root;

import com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightingSettingsPerFile;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.TestActionEvent;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.decorator.MagentoFolderDecorator;
import com.magento.idea.magento2plugin.project.Settings;
import java.io.File;
import javax.swing.Icon;

public class MagentoContentRootActionsTest extends BaseProjectTestCase {

    private File magentoRoot;
    private Settings settings;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        magentoRoot = FileUtil.createTempDirectory("magento", "", true);
        VfsRootAccess.allowRootAccess(getTestRootDisposable(), magentoRoot.getPath());
        settings = Settings.getInstance(getProject());
        settings.pluginEnabled = true;
        settings.magentoPath = FileUtil.toSystemIndependentName(magentoRoot.getPath());
        settings.myMagentoFolders = null;
    }

    @Override
    public void tearDown() throws Exception {
        try {
            settings.myMagentoFolders = null;
        } finally {
            super.tearDown();
        }
    }

    public void testDefaultRootsOfferNeitherAction() {
        final VirtualFile packages = directory("app/code");
        final VirtualFile design = directory("app/design");

        assertFalse(isVisible(new MarkDirectoryAsMagentoContentRot(), packages));
        assertFalse(isVisible(new MarkDirectoryAsMagentoContentRot(), design));

        settings.addMagentoFolder(packages.getUrl());
        settings.addMagentoFolder(design.getUrl());

        assertFalse(isVisible(new UnmarkDirectoryAsMagentoContentRot(), packages));
        assertFalse(isVisible(new UnmarkDirectoryAsMagentoContentRot(), design));
    }

    public void testDecoratorMarksDefaultRootsAndMarkedFolders() {
        final VirtualFile packages = directory("app/code");
        final VirtualFile design = directory("app/design");
        final VirtualFile other = directory("packages/foo");

        assertEquals(MagentoIcons.MARK_AS, decoratedIcon(packages));
        assertEquals(MagentoIcons.MARK_AS, decoratedIcon(design));
        assertNull(decoratedIcon(other));

        settings.addMagentoFolder(other.getUrl());

        assertEquals(MagentoIcons.MARK_AS, decoratedIcon(other));
    }

    public void testOtherFolderOffersMarkUntilItIsMarked() {
        final VirtualFile folder = directory("packages/foo");

        assertTrue(isVisible(new MarkDirectoryAsMagentoContentRot(), folder));
        assertFalse(isVisible(new UnmarkDirectoryAsMagentoContentRot(), folder));

        settings.addMagentoFolder(folder.getUrl());

        assertFalse(isVisible(new MarkDirectoryAsMagentoContentRot(), folder));
        assertTrue(isVisible(new UnmarkDirectoryAsMagentoContentRot(), folder));
    }

    public void testMarkingAFolderEnablesItsInspections() throws Exception {
        final File file = new File(magentoRoot, "vendor/foo/marked/etc/di.xml");

        FileUtil.writeToFile(file, "<config/>");
        final VirtualFile virtualFile = LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(file);
        final PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);
        final HighlightingSettingsPerFile highlighting =
                HighlightingSettingsPerFile.getInstance(getProject());
        final VirtualFile folder = virtualFile.getParent().getParent();
        final MarkDirectoryAsMagentoContentRot action = new MarkDirectoryAsMagentoContentRot();

        highlighting.setHighlightingSettingForRoot(psiFile, FileHighlightingSetting.SKIP_INSPECTION);
        isVisible(action, folder);
        action.modifyRoots(folder, null);
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

        assertEquals(
                FileHighlightingSetting.FORCE_HIGHLIGHTING,
                highlighting.getHighlightingSettingForRoot(psiFile)
        );
    }

    private Icon decoratedIcon(final VirtualFile folder) {
        final PresentationData presentation = new PresentationData();

        new MagentoFolderDecorator().decorate(
                new PsiDirectoryNode(
                        getProject(),
                        PsiManager.getInstance(getProject()).findDirectory(folder),
                        new ViewSettings() { }
                ),
                presentation
        );

        return presentation.getIcon(false);
    }

    private boolean isVisible(final AnAction action, final VirtualFile folder) {
        final AnActionEvent event = TestActionEvent.createTestEvent(
                action,
                SimpleDataContext.builder()
                        .add(
                                LangDataKeys.PSI_ELEMENT,
                                PsiManager.getInstance(getProject()).findDirectory(folder)
                        )
                        .add(
                                PlatformCoreDataKeys.MODULE,
                                ModuleManager.getInstance(getProject()).getModules()[0]
                        )
                        .build()
        );

        action.update(event);

        return event.getPresentation().isEnabledAndVisible();
    }

    private VirtualFile directory(final String path) {
        final File directory = new File(magentoRoot, path);

        assertTrue(directory.mkdirs());

        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory);
    }
}
