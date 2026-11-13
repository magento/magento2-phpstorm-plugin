/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public final class IsFileInEditableModuleUtil {

    private IsFileInEditableModuleUtil() {}

    /**
     * Module is considered editable if it is declared within `MAGENTO_ROOT/app/code`,
     * `MAGENTO_ROOT/app/design` or a folder marked as a Magento content root.
     *
     * @param file PsiFile
     * @return boolean
     */
    public static boolean execute(final PsiFile file) {
        final Project project = file.getProject();
        final VirtualFile virtualFile = file.getVirtualFile();

        return execute(project, virtualFile);
    }

    /**
     * Validates if a given virtual file is located within editable paths.
     *
     * @param project the current project containing the virtual file
     * @param virtualFile the file to check against editable module directories
     * @return true if the file is in an editable module directory, false otherwise
     */
    public static boolean execute(final Project project, final VirtualFile virtualFile) {
        final String packagesUrl = MagentoPathUrlUtil.execute(project);

        if (packagesUrl == null) {
            return false;
        }
        final List<String> editableUrls = new ArrayList<>();
        final List<String> magentoFolders = Settings.getInstance(project).getMagentoFolders();

        if (magentoFolders != null) {
            editableUrls.addAll(magentoFolders);
        }
        editableUrls.add(packagesUrl);
        editableUrls.add(MagentoPathUrlUtil.getDesignPath(project));

        for (final String editableUrl : editableUrls) {
            if (isUnderRoot(virtualFile, editableUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks a file against a root URL, resolved in the file system of the file.
     *
     * @param virtualFile the file to check
     * @param rootUrl the root URL, can be null when the Magento path is not configured
     * @return true if the file is the root or is located under it
     */
    public static boolean isUnderRoot(
            final VirtualFile virtualFile,
            final @Nullable String rootUrl
    ) {
        if (rootUrl == null) {
            return false;
        }
        final VirtualFile root = virtualFile.getFileSystem()
                .findFileByPath(VirtualFileManager.extractPath(rootUrl));

        return root != null && VfsUtilCore.isAncestor(root, virtualFile, false);
    }
}
