/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Package;

public class MagentoBasePathUtil {

    public static boolean isMagentoFolderValid(String path) {
        if (StringUtil.isEmptyOrSpaces(path)) {
            return false;
        } else {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
            if (file != null && file.isDirectory()) {
                return VfsUtil.findRelativeFile(file, Package.APP, Package.MODULE_BASE_AREA_DIR, ModuleDiXml.FILE_NAME) != null &&
                        VfsUtil.findRelativeFile(file, new String[]{Package.VENDOR}) != null;
            } else {
                return false;
            }
        }
    }
}
