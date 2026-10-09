/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php;

import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shows the path of the file that contains a navigation target next to the target
 * in a gutter icon popup.
 *
 * <p>The platform renderer takes the container text from the element presentation,
 * which is empty for XML tags, so targets such as several {@code <preference>} nodes
 * cannot be told apart without this renderer.</p>
 */
public class FilePathTargetPresentationRenderer extends PsiTargetPresentationRenderer<PsiElement> {

    @Override
    public @Nullable String getContainerText(final @NotNull PsiElement element) {
        final PsiFile psiFile = element.getContainingFile();

        if (psiFile == null) {
            return super.getContainerText(element);
        }
        final VirtualFile virtualFile = psiFile.getOriginalFile().getVirtualFile();

        if (virtualFile == null) {
            return super.getContainerText(element);
        }
        final VirtualFile contentRoot = ProjectFileIndex.getInstance(element.getProject())
                .getContentRootForFile(virtualFile);
        final String relativePath = contentRoot == null
                ? null
                : VfsUtilCore.getRelativePath(virtualFile, contentRoot);

        return relativePath == null ? virtualFile.getPath() : relativePath;
    }
}
