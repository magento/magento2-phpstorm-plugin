/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GetTargetElementUtil {

    private GetTargetElementUtil() {}

    /**
     * Get clicked on directory from the action event.
     *
     * @param event AnActionEvent
     *
     * @return PsiDirectory
     */
    public static @Nullable PsiDirectory getDirFromEvent(final @NotNull AnActionEvent event) {
        final PsiElement targetElement = LangDataKeys.PSI_ELEMENT.getData(event.getDataContext());

        if (targetElement == null) {
            return null;
        }
        PsiDirectory targetDirectory = null;
        PsiFile targetFile;

        if (targetElement instanceof PsiDirectory) {
            targetDirectory = (PsiDirectory) targetElement;
        } else if (targetElement instanceof PsiFile) {
            targetFile = (PsiFile) targetElement;
            targetDirectory = targetFile.getContainingDirectory();
        }

        return targetDirectory;
    }

    /**
     * Get clicked on file from the action event.
     *
     * @param event AnActionEvent
     *
     * @return PsiDirectory
     */
    public static @Nullable PsiFile getFileFromEvent(final @NotNull AnActionEvent event) {
        final PsiElement targetElement = LangDataKeys.PSI_ELEMENT.getData(event.getDataContext());

        if (targetElement == null) {
            return null;
        }

        return targetElement instanceof PsiFile ? (PsiFile) targetElement : null;
    }
}
