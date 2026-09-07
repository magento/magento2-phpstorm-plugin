/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.lang.roots;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.lang.PhpFileType;
import org.jetbrains.annotations.NotNull;

public class MagentoTestSourceFilter extends TestSourcesFilter {

    private static final String TEST_CLASS_SUFFIX = "Test";

    @Override
    public boolean isTestSource(final @NotNull VirtualFile file, final @NotNull Project project) {
        final String fileName = file.getNameWithoutExtension();
        final int suffixIndex = fileName.length() - TEST_CLASS_SUFFIX.length();

        if (suffixIndex < 0) {
            return false;
        }

        return file.getFileType().equals(PhpFileType.INSTANCE)
                && "Test".equals(fileName.substring(suffixIndex));
    }
}
