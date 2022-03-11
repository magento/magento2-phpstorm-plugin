/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.php;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import org.jetbrains.annotations.NotNull;

public final class PhpPsiElementsUtil {

    private PhpPsiElementsUtil() {}

    /**
     * Get PHP class from event.
     *
     * @param event AnActionEvent
     *
     * @return PhpClass
     */
    public static PhpClass getPhpClass(final @NotNull AnActionEvent event) {
        final PhpFile phpFile = getPhpFile(event);

        return phpFile == null ? null : GetFirstClassOfFile.getInstance().execute(phpFile);
    }

    /**
     * Get PHP method from event.
     *
     * @param event AnActionEvent
     *
     * @return Method
     */
    public static Method getPhpMethod(final @NotNull AnActionEvent event) {
        PsiElement element = getElementUnderCursor(event);

        if (element instanceof LeafPsiElement && element.getParent() instanceof Method) {
            element = element.getParent();
        }

        return element instanceof Method ? (Method) element : null;
    }

    /**
     * Get method argument from event.
     *
     * @param event AnActionEvent
     *
     * @return Method
     */
    public static Parameter getMethodArgument(final @NotNull AnActionEvent event) {
        PsiElement element = getElementUnderCursor(event);

        if (element instanceof LeafPsiElement && element.getParent() instanceof Parameter) {
            element = element.getParent();
        }

        return element instanceof Parameter ? (Parameter) element : null;
    }

    /**
     * Get php file for event.
     *
     * @param event AnActionEvent
     *
     * @return PhpFile
     */
    private static PhpFile getPhpFile(final @NotNull AnActionEvent event) {
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        if (!(psiFile instanceof PhpFile)) {
            return null;
        }

        return (PhpFile) psiFile;
    }

    /**
     * Get element under cursor.
     *
     * @param event AnActionEvent
     *
     * @return PsiElement
     */
    private static PsiElement getElementUnderCursor(final @NotNull AnActionEvent event) {
        final PhpFile phpFile = getPhpFile(event);

        if (phpFile == null) {
            return null;
        }

        final Caret caret = event.getData(PlatformDataKeys.CARET);

        if (caret == null) {
            return null;
        }

        final int offset = caret.getOffset();

        return phpFile.findElementAt(offset);
    }
}
