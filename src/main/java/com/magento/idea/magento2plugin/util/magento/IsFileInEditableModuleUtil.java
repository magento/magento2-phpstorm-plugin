/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;

public final class IsFileInEditableModuleUtil {

    private IsFileInEditableModuleUtil() {}

    /**
     * Module is considered editable if it is declared within `MAGENTO_ROOT/app/code` directory.
     *
     * @param file PsiFile
     * @return boolean
     */
    public static boolean execute(final PsiFile file) {
        final String magentoPath = Settings.getMagentoPath(file.getProject());
        if (magentoPath == null) {
            return false;
        }
        final String editablePath = magentoPath + File.separator + Package.packagesRoot;
        final String filePath = file.getVirtualFile().getPath();

        return filePath.startsWith(editablePath);
    }
}
