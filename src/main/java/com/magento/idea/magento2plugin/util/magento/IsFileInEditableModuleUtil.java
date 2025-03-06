/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.List;

public final class IsFileInEditableModuleUtil {

    private IsFileInEditableModuleUtil() {}

    /**
     * Module is considered editable if it is declared within `MAGENTO_ROOT/app/code` directory.
     *
     * @param file PsiFile
     * @return boolean
     */
    public static boolean execute(final PsiFile file) {
        Project project = file.getProject();
        VirtualFile virtualFile = file.getVirtualFile();

        return execute(project, virtualFile);
    }

    /**
     * Validates if a given virtual file is located within editable paths defined by Magento project structure.
     *
     * @param project the current project containing the virtual file
     * @param virtualFile the file to check against editable module directories
     * @return true if the file is in an editable module directory, false otherwise
     */
    public static boolean execute(Project project, VirtualFile virtualFile) {
        Settings settings = Settings.getInstance(project);
        List<String> magentoToFolders = settings.getMagentoFolders();
        String magentoPathUrl = MagentoPathUrlUtil.execute(project);
        if (magentoPathUrl != null) {
            if (magentoToFolders == null) {
                magentoToFolders = List.of(
                        magentoPathUrl
                );
            } else {
                magentoToFolders.add(
                       magentoPathUrl
                );
            }
        }


        final String filePath = virtualFile.getUrl();

        if (magentoToFolders == null) {
            return false;
        }

        for (String editablePath : magentoToFolders) {
            if (filePath.startsWith(editablePath)) {
                return true;
            }
        }
        return false;
    }
}
