/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PhpClassLocatorUtil {

    private PhpClassLocatorUtil() {
    }

    /**
     * Locate first class of file.
     *
     * @param file PsiFile
     *
     * @return PhpClass
     */
    public static @Nullable PhpClass locate(final @NotNull PsiFile file) {
        if (!(file instanceof PhpFile)) {
            return null;
        }

        return GetFirstClassOfFile.getInstance().execute((PhpFile) file);
    }
}
