/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.module;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class UctModulePathValidatorUtil {

    private UctModulePathValidatorUtil() {
    }

    /**
     * Check if uct module path candidate is valid.
     *
     * @param modulePathCandidate String
     *
     * @return boolean
     */
    public static boolean validate(final @NotNull String modulePathCandidate) {
        final VirtualFile vfs = VfsUtil.findFile(Path.of(modulePathCandidate), false);

        if (vfs == null) {
            return false;
        }

        return vfs.isDirectory();
    }
}
