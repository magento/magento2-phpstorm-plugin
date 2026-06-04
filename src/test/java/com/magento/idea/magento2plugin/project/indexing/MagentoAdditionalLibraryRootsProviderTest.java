/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.indexing;

import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class MagentoAdditionalLibraryRootsProviderTest extends BaseProjectTestCase {

    public void testProvidesMagentoIndexRootsIncludingVendor() {
        final Settings settings = Settings.getInstance(getProject());
        final String magentoPath = getAbsoluteProjectPath("src/test/testData/project/magento2");
        settings.pluginEnabled = true;
        settings.magentoPath = magentoPath;

        final MagentoAdditionalLibraryRootsProvider provider = new MagentoAdditionalLibraryRootsProvider();
        final Collection<SyntheticLibrary> libraries = provider.getAdditionalProjectLibraries(getProject());

        assertEquals(1, libraries.size());

        final Set<String> rootPaths = libraries.iterator().next().getSourceRoots().stream()
                .map(VirtualFile::getPath)
                .collect(Collectors.toSet());

        assertTrue(rootPaths.contains(magentoPath + "/app/code"));
        assertTrue(rootPaths.contains(magentoPath + "/vendor"));
        assertTrue(rootPaths.contains(magentoPath + "/lib/web"));
    }

    public void testDoesNotProvideRootsWhenPluginDisabled() {
        disablePluginAndReindex();

        final MagentoAdditionalLibraryRootsProvider provider = new MagentoAdditionalLibraryRootsProvider();

        assertTrue(provider.getAdditionalProjectLibraries(getProject()).isEmpty());
        assertTrue(provider.getRootsToWatch(getProject()).isEmpty());
    }
}
