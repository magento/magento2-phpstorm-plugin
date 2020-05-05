/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;

public final class MagentoBasePathUtil {

    private MagentoBasePathUtil() {}

    /**
     * Method detects Magento Framework Root.
     *
     * @param path String
     * @return boolean
     */
    public static boolean isMagentoFolderValid(final String path) {
        if (StringUtil.isEmptyOrSpaces(path)) {
            return false;
        }
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file != null && file.isDirectory()) {
            return VfsUtil.findRelativeFile(file, Package.frameworkRootComposer.split(File.separator)) != null
                || VfsUtil.findRelativeFile(file, Package.frameworkRootGit.split(File.separator)) != null;
        }
        return false;
    }
}
