/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.comparator.util;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.actions.impl.MutableDiffRequestChain;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DiffRequestChainUtil {

    private DiffRequestChainUtil() {}

    /**
     * Create mutable chain for files comparing.
     *
     * @param project Project
     * @param targetFile VirtualFile
     * @param baseFile VirtualFile
     *
     * @return MutableDiffRequestChain
     */
    public static @Nullable MutableDiffRequestChain createMutableChain(
            final @NotNull Project project,
            final @NotNull VirtualFile targetFile,
            final @NotNull VirtualFile baseFile
    ) {
        final DiffContentFactory contentFactory = DiffContentFactory.getInstance();
        final DiffContent targetContent = contentFactory.create(project, targetFile);
        final DiffContent baseContent = contentFactory.create(project, baseFile);

        if (!(targetContent instanceof DocumentContent)
                || !(baseContent instanceof DocumentContent)) {
            return null;
        }

        final MutableDiffRequestChain chain = new MutableDiffRequestChain(
                (DocumentContent) targetContent,
                (DocumentContent) baseContent,
                project
        );
        chain.setWindowTitle(DiffRequestFactory.getInstance().getTitleForComparison(targetFile, baseFile));

        return chain;
    }
}
