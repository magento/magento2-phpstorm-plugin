/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.util;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class NavigateToCreatedFile {
    private static NavigateToCreatedFile INSTANCE = null;

    public static NavigateToCreatedFile getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new NavigateToCreatedFile();
        }
        return INSTANCE;
    }

    public void navigate(@NotNull Project project, @NotNull PsiFile createdFile) {
        VirtualFile virtualFile = createdFile.getVirtualFile();
        if (virtualFile == null) {
            return;
        }
        PsiNavigationSupport.getInstance().createNavigatable(project, virtualFile, -1).navigate(false);
    }
}
