/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.project.util.GetProjectBasePath;
import org.jetbrains.annotations.NotNull;

public class IsClickedDirectoryInsideProject {

    private static IsClickedDirectoryInsideProject INSTANCE = null;

    public static IsClickedDirectoryInsideProject getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new IsClickedDirectoryInsideProject();
        }
        return INSTANCE;
    }

    public boolean execute(@NotNull Project project, PsiDirectory directory) {
        return VfsUtilCore.isAncestor(GetProjectBasePath.execute(project), directory.getVirtualFile(), false);
    }
}
