/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.MagentoLibJsIndex;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public class LibJsIndex {

    private static LibJsIndex INSTANCE;

    private LibJsIndex() {
    }

    public static LibJsIndex getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new LibJsIndex();
        }
        return INSTANCE;
    }

    public Collection<VirtualFile> getLibJsFiles(@NotNull String filePath, @NotNull Project project) {
        return FileBasedIndex.getInstance()
                .getContainingFiles(MagentoLibJsIndex.KEY,
                        filePath.replace("'", ""),
                        GlobalSearchScope.allScope(project)
                );
    }
}
