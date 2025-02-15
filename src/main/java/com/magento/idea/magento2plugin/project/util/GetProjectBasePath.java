/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class GetProjectBasePath {

    public static VirtualFile execute(@NotNull Project project) {
        String basePath =  project.getBasePath();
        if (basePath == null) {
            return null;
        }
        return LocalFileSystem.getInstance().findFileByPath(basePath);
    }
}
