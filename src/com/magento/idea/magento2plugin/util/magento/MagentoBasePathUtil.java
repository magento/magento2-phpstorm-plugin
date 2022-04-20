/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

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
            return VfsUtil.findRelativeFile(
                    file,
                    Package.frameworkRootComposer.split(VfsUtilCore.VFS_SEPARATOR)
            ) != null
                || VfsUtil.findRelativeFile(
                        file,
                    Package.frameworkRootGit.split(VfsUtilCore.VFS_SEPARATOR)) != null;
        }
        return false;
    }

    /**
     * Check if specified path belongs to the correct vendor name.
     *
     * @param path String
     *
     * @return boolean
     */
    public static boolean isCustomVendorDirValid(final @NotNull String path) {
        final String[] pathParts = path.split(Package.V_FILE_SEPARATOR);

        if (pathParts.length < 3) { //NOPMD
            return false;
        }

        final String[] sourceCandidateParts = Arrays.copyOfRange(
                pathParts,
                pathParts.length - 3,
                pathParts.length - 1
        );

        return Package.packagesRoot.equals(
                String.join(Package.V_FILE_SEPARATOR, sourceCandidateParts)
        );
    }

    /**
     * Check if specified path belongs to the correct packages folder.
     *
     * @param path String
     *
     * @return boolean
     */
    public static boolean isCustomCodeSourceDirValid(final @NotNull String path) {
        final String[] pathParts = path.split(Package.V_FILE_SEPARATOR);

        if (pathParts.length < 2) { //NOPMD
            return false;
        }

        final String[] sourceCandidateParts = Arrays.copyOfRange(
                pathParts,
                pathParts.length - 2,
                pathParts.length
        );

        return Package.packagesRoot.equals(
                String.join(Package.V_FILE_SEPARATOR, sourceCandidateParts)
        );
    }
}
