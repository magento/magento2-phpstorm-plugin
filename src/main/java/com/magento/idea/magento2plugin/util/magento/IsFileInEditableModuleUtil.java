/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
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
        final Project project = file.getProject();
        final VirtualFile virtualFile = file.getVirtualFile();

        return execute(project, virtualFile);
    }

    /**
     * Validates if a given virtual file is located within editable paths defined by Magento project structure.
     *
     * @param project the current project containing the virtual file
     * @param virtualFile the file to check against editable module directories
     * @return true if the file is in an editable module directory, false otherwise
     */
    public static boolean execute(final Project project, final VirtualFile virtualFile) {
        final Settings settings = Settings.getInstance(project);
        List<String> editablePaths = settings.getMagentoFolders();
        final String magentoRootPath = MagentoPathUrlUtil.execute(project);
        final String magentoDesignPath = MagentoPathUrlUtil.getDesignPath(project);

        if (magentoRootPath == null) {
            return false;
        }

        if (editablePaths == null) {
            editablePaths = new ArrayList<>();
        }

        editablePaths.add(magentoRootPath);
        if (magentoDesignPath != null) {
            editablePaths.add(magentoDesignPath);
        }

        final String currentFilePath = virtualFile.getUrl();
        for (final String editablePath : editablePaths) {
            if (normalizeUrl(currentFilePath).startsWith(normalizeUrl(editablePath))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normalizes a URL by removing the scheme (e.g., temp://, file://) to allow proper comparisons.
     *
     * @param url the URL to normalize
     * @return the normalized URL as a String
     */
    private static String normalizeUrl(final String url) {
        final int schemeSeparatorIndex = url.indexOf("://");
        if (schemeSeparatorIndex != -1) {
            return url.substring(schemeSeparatorIndex + 3);
        }
        return url;
    }
}
