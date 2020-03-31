/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.util;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;

public class GetAllSubFilesOfVirtualFileUtil {

    private static GetAllSubFilesOfVirtualFileUtil INSTANCE;

    public static GetAllSubFilesOfVirtualFileUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new GetAllSubFilesOfVirtualFileUtil();
        }
        return INSTANCE;
    }

    public Collection<VirtualFile> execute(VirtualFile virtualFile)
    {
        Collection<VirtualFile> list = new ArrayList<>();

        VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.isDirectory()) {
                    list.add(file);
                }
                return super.visitFile(file);
            }
        });
        return list;
    }
}
