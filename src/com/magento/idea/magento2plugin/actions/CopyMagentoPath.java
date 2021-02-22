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
import com.intellij.psi.PsiFile;
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
        "view/base/templates/",
        "templates/"
    };

    @Override
    public void update(@NotNull final AnActionEvent event) {
        final VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && virtualFile.isDirectory()
                || virtualFile != null && !PHTML.equals(virtualFile.getExtension())) {
            event.getPresentation().setVisible(false);
        }
    }

    @Nullable
    @Override
    public String getPathToElement(
            @NotNull final Project project,
            @Nullable final VirtualFile virtualFile,
            @Nullable final Editor editor
    ) {
        if (virtualFile == null) {
            return null;
        }
        final PsiFile file
                = PsiManager.getInstance(project).findFile(virtualFile);
        if (file == null) {
            return null;
        }
        final PsiDirectory directory = file.getContainingDirectory();
        final String moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        if (moduleName == null) {
            return null;
        }
        final StringBuilder fullPath = new StringBuilder(virtualFile.getPath());
        final StringBuilder magentoPath
                = new StringBuilder(moduleName);
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

    private int getIndexOf(final StringBuilder fullPath, final String path) {
        return fullPath.lastIndexOf(path) == -1
                ? getIndexOf(fullPath, templatePaths[++index])
                : fullPath.lastIndexOf(path);
    }
}
