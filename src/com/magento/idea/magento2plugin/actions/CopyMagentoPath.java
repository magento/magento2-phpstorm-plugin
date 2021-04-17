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
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CopyMagentoPath extends CopyPathProvider {
    public static final String PHTML_EXTENSION = "phtml";
    public static final List<String> WEB_EXTENSIONS
            = Arrays.asList("js", "css", "png", "jpg", "jpeg", "gif");
    public static final String SEPARATOR = "::";
    private int index;

    private final String[] templatePaths = {
        "view/frontend/templates/",
        "view/adminhtml/templates/",
        "view/base/templates/",
        "templates/"
    };

    private final String[] webPaths = {
        "view/frontend/web/",
        "view/adminhtml/web/",
        "view/base/web/",
        "web/"
    };

    @Override
    public void update(@NotNull final AnActionEvent event) {
        final VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (isNotValidFile(virtualFile)) {
            event.getPresentation().setVisible(false);
        }
    }

    private boolean isNotValidFile(final VirtualFile virtualFile) {
        return virtualFile != null && virtualFile.isDirectory()
                || virtualFile != null && !WEB_EXTENSIONS.contains(virtualFile.getExtension())
                    && !PHTML_EXTENSION.equals(virtualFile.getExtension());
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

        index = -1;
        String[] paths;

        if (PHTML_EXTENSION.equals(virtualFile.getExtension())) {
            paths = templatePaths;
        } else if (WEB_EXTENSIONS.contains(virtualFile.getExtension())) {
            paths = webPaths;
        } else {
            return fullPath.toString();
        }

        try {
            final int endIndex = getIndexOf(paths, fullPath, paths[++index]);
            final int offset = paths[index].length();

            fullPath.replace(0, endIndex + offset, "");

            return moduleName + SEPARATOR + fullPath;
        } catch (ArrayIndexOutOfBoundsException exception) {
            return fullPath.toString();
        }
    }

    private int getIndexOf(final String[] paths, final StringBuilder fullPath, final String path) {
        return fullPath.lastIndexOf(path) == -1
                ? getIndexOf(paths, fullPath, paths[++index])
                : fullPath.lastIndexOf(path);
    }
}
