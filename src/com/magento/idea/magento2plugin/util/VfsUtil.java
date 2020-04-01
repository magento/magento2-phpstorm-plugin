/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class VfsUtil {

    @Nullable
    public static VirtualFile findVfUp(VirtualFile item, String searchItemName)
    {
        if (item.getParent() != null) {
            VirtualFile vf = VfsUtilCore.findRelativeFile(searchItemName, item.getParent());
            if (vf != null && !vf.isDirectory()) {
                return vf;
            }
            return findVfUp(item.getParent(), searchItemName);
        }
        return null;
    }

    public static Collection<VirtualFile> getAllSubFiles(VirtualFile virtualFile)
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
