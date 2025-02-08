/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.util;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileSystemItem;
import com.magento.idea.magento2plugin.magento.packages.Package;

public final class GetEditableModuleNameByRootFileUtil {

    private GetEditableModuleNameByRootFileUtil() {}

    /**
     * Method detects Magento Framework Root.
     *
     * @param moduleRootFile PsiFileSystemItem
     * @return boolean
     */
    public static String execute(final PsiFileSystemItem moduleRootFile) {
        final PsiFileSystemItem moduleDirectory = moduleRootFile.getParent();
        if (moduleDirectory == null) {
            return null;
        }
        final PsiDirectory packageDirectory = (PsiDirectory) moduleDirectory.getParent();
        if (packageDirectory == null) {
            return null;
        }

        return packageDirectory.getName()
            + Package.vendorModuleNameSeparator
            + moduleDirectory.getName();
    }
}
