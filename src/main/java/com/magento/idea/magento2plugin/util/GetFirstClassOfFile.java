/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

public class GetFirstClassOfFile {
    private static GetFirstClassOfFile INSTANCE = null;

    public static GetFirstClassOfFile getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new GetFirstClassOfFile();
        }
        return INSTANCE;
    }

    @Nullable
    public PhpClass execute(PhpFile phpFile) {
        Collection<PhpClass> phpClasses = PsiTreeUtil.collectElementsOfType(phpFile, PhpClass.class);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }

    /**
     * Get the containing file of the first PHP class declaration.
     *
     * @param file file to inspect
     * @return the declaration's file, or null for a non-PHP file or a file without a class
     */
    @Nullable
    public PsiFile getContainingFile(final @NotNull PsiFile file) {
        if (!(file instanceof PhpFile phpFile)) {
            return null;
        }
        final PhpClass phpClass = execute(phpFile);
        return phpClass == null ? null : phpClass.getContainingFile();
    }
}
