/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class UctExecutableValidatorUtil {

    private static final String UCT_EXECUTABLE_FILENAME = "uct";

    private UctExecutableValidatorUtil() {
    }

    /**
     * Check if uct executable candidate is valid.
     *
     * @param executablePathCandidate String
     *
     * @return boolean
     */
    public static boolean validate(final @NotNull String executablePathCandidate) {
        final VirtualFile vfs = VfsUtil.findFile(Path.of(executablePathCandidate), false);

        if (vfs == null) {
            return false;
        }

        return UCT_EXECUTABLE_FILENAME.equals(vfs.getName());
    }
}
