/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.ExtensionTestUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.indexes.EventIndex;
import java.util.List;

public class EventNameIndexFixtureTest extends BaseProjectTestCase {
    public void testSharedIndexRetainsPhpDispatchFilesAndXmlDeclarations() {
        final PsiFile declaration = myFixture.addFileToProject("app/code/Test/Events/etc/events.xml",
                "<config><event name=\"portability_shared_event\"/></config>");
        final PsiFile dispatch = myFixture.addFileToProject("app/code/Test/Events/Dispatch.php",
                "<?php $events->dispatch('portability_shared_event');");
        final GlobalSearchScope scope = GlobalSearchScope.allScope(getProject());
        final var files = FileBasedIndex.getInstance()
                .getContainingFiles(EventNameIndex.KEY, "portability_shared_event", scope);

        assertContainsElements(files, declaration.getVirtualFile(), dispatch.getVirtualFile());
        final var declarations = new EventIndex(getProject()).getEventElements("portability_shared_event", scope);
        assertSize(1, declarations);
        assertEquals(declaration, declarations.iterator().next().getContainingFile());
    }

    public void testMissingPhpProviderChangesVersionAndRejectsPhpFiles() {
        final EventNameIndex index = new EventNameIndex();
        final int versionWithPhp = index.getVersion();
        final PsiFile php = myFixture.configureByText("Dispatch.php", "<?php");
        assertTrue(index.getInputFilter().acceptInput(php.getVirtualFile()));

        ExtensionTestUtil.maskExtensions(EventNameIndexProvider.EP_NAME, List.of(), getTestRootDisposable());

        assertTrue(versionWithPhp != index.getVersion());
        assertFalse(index.getInputFilter().acceptInput(php.getVirtualFile()));
    }
}
