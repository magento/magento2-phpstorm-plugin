/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;

public class GetModuleSourceFilesUtil {

    private static GetModuleSourceFilesUtil INSTANCE;

    public static GetModuleSourceFilesUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new GetModuleSourceFilesUtil();
        }
        return INSTANCE;
    }

    public Collection<VirtualFile> execute(@NotNull String fileName, Project project)
    {
        Collection<VirtualFile> virtualFiles = GetModuleFileUtil.getInstance().execute(
                fileName,
                project
        );
        if (null == virtualFiles) {
            return null;
        }
        virtualFiles.removeIf(vf -> !(vf != null && vf.getParent() != null));
        Collection<VirtualFile> sourceVfs = new ArrayList<>();
        for (VirtualFile vf : virtualFiles) {
            sourceVfs.add(vf.getParent());
        }
        return sourceVfs;
    }
}
