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

import java.util.Arrays;
import java.util.List;

public class CopyMagentoPath extends CopyPathProvider {
    public static final String PHTML = "phtml";
    public static final String JS = "js";
    public static final String CSS = "css";
    private final List<String> acceptedTypes = Arrays.asList(PHTML, JS, CSS);
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

    private boolean isNotValidFile(VirtualFile virtualFile) {
        return virtualFile != null && virtualFile.isDirectory()
                || virtualFile != null && !acceptedTypes.contains(virtualFile.getExtension());
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

        index = -1;
        String[] paths;

        if (PHTML.equals(virtualFile.getExtension())) {
            paths = templatePaths;
        } else if (JS.equals(virtualFile.getExtension()) || CSS.equals(virtualFile.getExtension())) {
            paths = webPaths;
        } else {
            return fullPath.toString();
        }

        final int endIndex = getIndexOf(fullPath, paths[++index], paths);
        final int offset = paths[index].length();

        fullPath.replace(0, endIndex + offset, "");

        return magentoPath.append(SEPARATOR).append(fullPath).toString();
    }

    private int getIndexOf(final StringBuilder fullPath, final String path, final String[] paths) {
        return fullPath.lastIndexOf(path) == -1
                ? getIndexOf(fullPath, paths[++index], paths)
                : fullPath.lastIndexOf(path);
    }
}
