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
import com.intellij.openapi.vfs.VirtualFileManager;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public final class MagentoBasePathUtil {

    private MagentoBasePathUtil() {}

    private static VirtualFile findDirectory(final String path) {
        if (StringUtil.isEmptyOrSpaces(path)) {
            return null;
        }
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file == null) {
            file = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path));
        }
        if (file == null) {
            file = VirtualFileManager.getInstance().findFileByUrl(path);
        }
        if (file == null) {
            file = VirtualFileManager.getInstance().findFileByUrl("temp://" + (path.startsWith("/") ? path : "/" + path));
        }
        return file != null && file.isDirectory() ? file : null;
    }

    /**
     * Method detects Magento Framework Root (check if magento framework exists).
     *
     * @param path String
     * @return boolean
     */
    public static boolean isMagentoFolderValid(final String path) {
        final VirtualFile file = findDirectory(path);

        if (file != null) {
            return VfsUtil.findRelativeFile(
                    file,
                    Package.frameworkRootComposer.split(Package.V_FILE_SEPARATOR)
            ) != null || VfsUtil.findRelativeFile(
                    file,
                    Package.frameworkRootGit.split(Package.V_FILE_SEPARATOR)
            ) != null || VfsUtil.findRelativeFile(
                    file,
                    Package.mageOsFrameworkRootComposer.split(Package.V_FILE_SEPARATOR)
            ) != null;
        }

        return false;
    }

    /**
     * Check if composer.json exists in directory.
     *
     * @param path String
     * @return boolean
     */
    public static Boolean isComposerJsonExists(final String path) {
        final VirtualFile magentoRoot = findDirectory(path);

        if (magentoRoot == null) {
            return false;
        }

        return magentoRoot.findChild(ComposerJson.FILE_NAME) != null;
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
