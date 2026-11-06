/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.List;

public class IsFileInEditableModuleUtilTest extends BaseProjectTestCase {

    private Settings settings;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        settings = Settings.getInstance(getProject());
        settings.pluginEnabled = true;
        settings.magentoPath = "/src";
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

    public void testAcceptsPackagesAndDesignRoots() {
        assertTrue(isEditable(file("app/code/Foo/Editable/etc/module.xml")));
        assertTrue(isEditable(file("app/design/frontend/Foo/bar/theme.xml")));
    }

    public void testRejectsOtherFolders() {
        assertFalse(isEditable(file("vendor/foo/editable/etc/module.xml")));
        assertFalse(isEditable(file("lib/web/foo.xml")));
    }

    public void testRejectsSiblingWithTheSamePrefix() {
        assertFalse(isEditable(file("app/codeBackup/Foo/Editable/etc/module.xml")));
        assertFalse(isEditable(file("app/designBackup/Foo/bar/theme.xml")));
    }

    public void testResolvesAConfiguredPathWithADotSegment() {
        settings.magentoPath = "/src/.";

        assertTrue(isEditable(file("app/code/Foo/Dot/etc/module.xml")));
    }

    public void testKeepsMagentoFoldersUnchanged() {
        final VirtualFile file = file("packages/foo/etc/module.xml");
        final String folderUrl = file.getParent().getParent().getUrl();
        final List<String> magentoFolders = new ArrayList<>(List.of(folderUrl));

        settings.myMagentoFolders = magentoFolders;

        assertTrue(isEditable(file));
        assertEquals(List.of(folderUrl), magentoFolders);
    }

    public void testLoadStateRemovesDuplicatedMagentoFolders() {
        final Settings.State state = settings.getState();

        state.addMagentoFolder("file:///src/packages");
        state.addMagentoFolder("file:///src/packages");
        settings.loadState(state);

        assertEquals(List.of("file:///src/packages"), settings.getMagentoFolders());
    }

    public void testLoadStateRemovesDefaultRootsFromMagentoFolders() {
        final Settings.State state = settings.getState();

        state.addMagentoFolder("file:///src/app/code");
        state.addMagentoFolder("file:///src/app/design");
        state.addMagentoFolder("file:///src/packages");
        settings.loadState(state);

        assertEquals(List.of("file:///src/packages"), settings.getMagentoFolders());
    }

    public void testRootUrlsAreCanonical() {
        settings.magentoPath = "/src/.";

        assertEquals("file:///src/app/code", MagentoPathUrlUtil.execute(getProject()));
        assertTrue(
                MagentoPathUrlUtil.isDefaultEditableRoot(getProject(), "file:///src/app/design")
        );
    }

    public void testLoadStateRemovesDefaultRootsWithADotSegmentPath() {
        settings.magentoPath = "/src/.";
        final Settings.State state = settings.getState();

        state.addMagentoFolder("file:///src/app/code");
        settings.loadState(state);

        assertEmpty(settings.getMagentoFolders());
    }

    public void testRejectsMarkedFolderWithoutMagentoPath() {
        final VirtualFile file = file("packages/noPath/etc/module.xml");

        settings.magentoPath = null;
        settings.myMagentoFolders = List.of(file.getParent().getParent().getUrl());

        assertFalse(isEditable(file));
    }

    private VirtualFile file(final String path) {
        return myFixture.addFileToProject(path, "<config/>").getVirtualFile();
    }

    private boolean isEditable(final VirtualFile file) {
        return IsFileInEditableModuleUtil.execute(getProject(), file);
    }
}
