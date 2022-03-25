/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.scanner;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ModuleFilesScanner implements Iterable<PsiFile> {

    private final ComponentData componentData;
    private final List<PsiFile> files;

    /**
     * Module files scanner constructor.
     *
     * @param componentData ComponentData
     */
    public ModuleFilesScanner(final @NotNull ComponentData componentData) {
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
            if (SupportedIssue.getSupportedFileTypes().stream().noneMatch(
                    clazz -> clazz.isInstance(file))
            ) {
                continue;
            }
            files.add(file);
        }

        for (final PsiDirectory subDirectory : directory.getSubdirectories()) {
            collectFilesInDirectoryRecursively(subDirectory);
        }
    }
}
