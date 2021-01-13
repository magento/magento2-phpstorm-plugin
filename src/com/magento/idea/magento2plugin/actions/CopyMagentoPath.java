/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions;

import com.intellij.ide.actions.CopyPathProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CopyMagentoPath extends CopyPathProvider {
    public static final String PHTML = "phtml";
    public static final String PHTML_SEPARATOR = "::";
    private int index;
    private final String[] templatePaths = {
        "view/frontend/templates/",
        "view/adminhtml/templates/",
        "view/base/templates/"
    };

    @Override
    public void update(@NotNull AnActionEvent e) {
        final VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && virtualFile.isDirectory()) {
            e.getPresentation().setVisible(false);
        }
    }

    @Nullable
    @Override
    public String getPathToElement(
            @NotNull Project project,
            @Nullable VirtualFile virtualFile,
            @Nullable Editor editor
    ) {
        final PsiDirectory directory
                = PsiManager.getInstance(project).findFile(virtualFile).getContainingDirectory();
        final StringBuilder fullPath = new StringBuilder(virtualFile.getPath());
        final StringBuilder magentoPath
                = new StringBuilder(GetModuleNameByDirectoryUtil.execute(directory, project));
        String path = fullPath.toString();

        if (PHTML.equals(virtualFile.getExtension())) {
            index = -1;
            final int endIndex = getIndexOf(fullPath, templatePaths[++index]);
            final int offset = templatePaths[index].length();

            fullPath.replace(0, endIndex + offset, "");
            magentoPath.append(PHTML_SEPARATOR);
            magentoPath.append(fullPath);
            path = magentoPath.toString();
        }

        return path;
    }

    private int getIndexOf(StringBuilder fullPath, String path) {
        return fullPath.lastIndexOf(path) != -1
                ? fullPath.lastIndexOf(path)
                : getIndexOf(fullPath, templatePaths[++index]);
    }
}
