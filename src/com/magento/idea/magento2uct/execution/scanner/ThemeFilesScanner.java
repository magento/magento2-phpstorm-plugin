/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.scanner;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThemeFilesScanner implements Iterable<PsiFile> {

    private final ComponentData componentData;
    private final List<PsiFile> files;

    /**
     * Module files scanner constructor.
     *
     * @param componentData ComponentData
     */
    public ThemeFilesScanner(final @NotNull ComponentData componentData) {
        this.componentData = componentData;
        files = new ArrayList<>();
    }

    @Override
    public @NotNull Iterator<PsiFile> iterator() {
        return run().iterator();
    }

    /**
     * Collect module files.
     *
     * @return List[PsiFile]
     */
    private List<PsiFile> run() {
        files.clear();
        collectFilesInDirectoryRecursively(componentData.getDirectory());

        return files;
    }

    /**
     * Collect all files in directory recursively.
     *
     * @param directory PsiDirectory
     */
    private void collectFilesInDirectoryRecursively(final @NotNull PsiDirectory directory) {
        for (final PsiFile file : directory.getFiles()) {
            if (!(file instanceof PhpFile)) {
                files.add(file);
            }
        }

        for (final PsiDirectory subDirectory : directory.getSubdirectories()) {
            collectFilesInDirectoryRecursively(subDirectory);
        }
    }
}
