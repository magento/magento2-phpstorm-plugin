/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.indexing;

import com.intellij.codeInsight.actions.ReaderModeMatcher;
import com.intellij.codeInsight.actions.ReaderModeProvider;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.io.IOException;
import java.util.List;

public class MagentoReaderModeMatcherTest extends BaseProjectTestCase {

    private static final String DI_XML = "/etc/di.xml";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Settings settings = Settings.getInstance(getProject());
        settings.pluginEnabled = true;
        settings.magentoPath = "/src";
    }

    public void testIsRegistered() {
        final ExtensionPointName<ReaderModeMatcher> extensionPoint =
                new ExtensionPointName<>("com.intellij.readerModeMatcher");

        assertTrue(
                extensionPoint.getExtensionList().stream()
                        .anyMatch(MagentoReaderModeMatcher.class::isInstance)
        );
    }

    public void testExcludesWritableFilesInEditableRoots() {
        assertEquals(Boolean.FALSE, matches(file("app/code/Foo/Bar")));
        assertEquals(Boolean.FALSE, matches(file("app/design/frontend/Foo/bar")));
        assertEquals(Boolean.FALSE, matches(file("lib/web/foo")));
    }

    public void testExcludesWritableFilesInMarkedMagentoFolders() {
        final VirtualFile file = file("packages/foo");
        final String folderUrl = file.getParent().getParent().getUrl();

        final Settings settings = Settings.getInstance(getProject());
        settings.myMagentoFolders = List.of(folderUrl);

        try {
            assertEquals(Boolean.FALSE, matches(file));
        } finally {
            settings.myMagentoFolders = null;
        }
    }

    public void testLeavesOtherFilesToThePlatform() {
        assertNull(matches(file("vendor/foo/bar")));
        assertNull(matches(file("app/codeBackup/Foo/Bar")));
    }

    public void testLeavesReadOnlyFilesToThePlatform() throws IOException {
        final VirtualFile file = file("app/code/Foo/ReadOnly");

        WriteAction.runAndWait(() -> file.setWritable(false));

        assertNull(matches(file));
    }

    public void testLeavesFilesToThePlatformWhenPluginDisabled() {
        final VirtualFile file = file("app/code/Foo/Disabled");

        Settings.getInstance(getProject()).pluginEnabled = false;

        assertNull(matches(file));
    }

    private VirtualFile file(final String module) {
        return myFixture.addFileToProject(module + DI_XML, "<config/>").getVirtualFile();
    }

    private Boolean matches(final VirtualFile file) {
        return new MagentoReaderModeMatcher().matches(
                getProject(),
                file,
                null,
                ReaderModeProvider.ReaderMode.LIBRARIES_AND_READ_ONLY
        );
    }
}
