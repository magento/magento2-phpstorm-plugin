/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public class GetModuleFileUtil {

    private static GetModuleFileUtil INSTANCE;

    public static GetModuleFileUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new GetModuleFileUtil();
        }
        return INSTANCE;
    }

    public Collection<VirtualFile> execute(@NotNull String fileName, Project project)
    {
        String moduleName = GetModuleNameUtil.getInstance().execute(fileName);
        if (null == moduleName || moduleName.isEmpty()) {
            return null;
        }
        return FileBasedIndex.getInstance()
                .getContainingFiles(ModuleNameIndex.KEY, moduleName,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(project),
                                PhpFileType.INSTANCE
                        )
                );
    }
}
