/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAnObserverDialog;
import com.magento.idea.magento2plugin.magento.files.Observer;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

public class CreateAnObserverAction extends DumbAwareAction {
    public static final String ACTION_NAME = "Create a new Observer for this event";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 Observer";
    public String targetEvent;

    public CreateAnObserverAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    /**
     * Updates the state of action.
     */
    @Override
    public void update(final AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        if (!Settings.isEnabled(project)) {
            this.setStatus(event, false);
            return;
        }
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);
        if (!(psiFile instanceof PhpFile)) {
            this.setStatus(event, false);
            return;
        }

        final PsiElement element = getElement(event);
        if (element == null) {
            this.setStatus(event, false);
            return;
        }

        if (isObserverEventNameClicked(element)) {
            this.setStatus(event, true);
            targetEvent = element.getText();
            return;
        }

        this.setStatus(event, false);
    }

    private PsiElement getElement(@NotNull final AnActionEvent event) {
        final Caret caret = event.getData(PlatformDataKeys.CARET);
        if (caret == null) {
            return null;
        }
        final int offset = caret.getOffset();
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);
        final PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            return null;
        }
        return element;
    }

    private boolean isObserverEventNameClicked(@NotNull final PsiElement element) {
        return checkIsElementStringLiteral(element)
                && checkIsParametersList(element.getParent().getParent())
                && checkIsMethodReference(element.getParent().getParent().getParent())
                && checkIsEventDispatchMethod(
                        (MethodReference) element.getParent().getParent().getParent()
                );
    }

    private boolean checkIsParametersList(@NotNull final PsiElement element) {
        return element instanceof ParameterList;
    }

    private boolean checkIsMethodReference(@NotNull final PsiElement element) {
        return element instanceof MethodReference;
    }

    private boolean checkIsEventDispatchMethod(final MethodReference element) {
        final PsiReference elementReference = element.getReference();
        if (elementReference == null) {
            return false;
        }
        final PsiElement method = elementReference.resolve();
        if (!(method instanceof Method)) {
            return false;
        }
        if (!((Method) method).getName().equals(Observer.DISPATCH_METHOD)) {
            return false;
        }
        final PsiElement phpClass = method.getParent();
        if (!(phpClass instanceof PhpClass)) {
            return false;
        }
        final String fqn = ((PhpClass) phpClass).getPresentableFQN();
        return fqn.equals(Observer.INTERFACE);
    }

    private boolean checkIsElementStringLiteral(@NotNull final PsiElement element) {
        final ASTNode astNode = element.getNode();
        if (astNode == null) {
            return false;
        }
        final IElementType elementType = astNode.getElementType();

        return elementType == PhpTokenTypes.STRING_LITERAL
                || elementType == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE;
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        CreateAnObserverDialog.open(event.getProject(), this.targetEvent);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
