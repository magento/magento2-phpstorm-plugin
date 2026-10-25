/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.stubs.indexes.data.PluginData;
import java.util.Map;
import java.util.Set;

public class PluginIndexTest extends BaseProjectTestCase {

    public void testLeadingNamespaceSeparatorKeepsPresentableIndexKey() {
        final PsiFile file = myFixture.addFileToProject(
                "app/code/Portable/Module/etc/di.xml",
                "<config><type name=\"\\Portable\\Target\">"
                        + "<plugin name=\"portable_plugin\" type=\"\\Portable\\Plugin\" sortOrder=\"25\"/>"
                        + "</type></config>"
        );
        IndexingTestUtil.waitUntilIndexesAreReady(getProject());
        final Map<String, Set<PluginData>> data = FileBasedIndex.getInstance().getFileData(
                PluginIndex.KEY, file.getVirtualFile(), getProject()
        );

        assertSameElements(data.keySet(), "Portable\\Target");
        final Set<PluginData> plugins = data.get("Portable\\Target");
        assertSize(1, plugins);
        final PluginData plugin = plugins.iterator().next();
        assertEquals("\\Portable\\Plugin", plugin.getType());
        assertEquals(25, plugin.getSortOrder());
    }
}
