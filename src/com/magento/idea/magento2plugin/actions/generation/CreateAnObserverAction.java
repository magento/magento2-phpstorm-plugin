/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ActionUpdateThreadAware;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SlowOperations;
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

public class CreateAnObserverAction extends DumbAwareAction
        implements ActionUpdateThreadAware {

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
    public void update(final @NotNull AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return;
        }

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

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        if (event.getProject() == null) {
            return;
        }
        CreateAnObserverDialog.open(event.getProject(), this.targetEvent);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private PsiElement getElement(final @NotNull AnActionEvent event) {
        final Caret caret = event.getData(PlatformDataKeys.CARET);

        if (caret == null) {
            return null;
        }
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        if (psiFile == null) {
            return null;
        }
        final int offset = caret.getOffset();

        return psiFile.findElementAt(offset);
    }

    private boolean isObserverEventNameClicked(final @NotNull PsiElement element) {
        return checkIsElementStringLiteral(element)
                && checkIsParametersList(element.getParent().getParent())
                && checkIsMethodReference(element.getParent().getParent().getParent())
                && checkIsEventDispatchMethod(
                        (MethodReference) element.getParent().getParent().getParent()
                );
    }

    private boolean checkIsParametersList(final @NotNull PsiElement element) {
        return element instanceof ParameterList;
    }

    private boolean checkIsMethodReference(final @NotNull PsiElement element) {
        return element instanceof MethodReference;
    }

    private boolean checkIsEventDispatchMethod(final MethodReference element) {
        final PsiReference elementReference = element.getReference();
        if (elementReference == null) {
            return false;
        }
        final PsiElement method = SlowOperations.allowSlowOperations(elementReference::resolve);
        if (!(method instanceof Method)) {
            return false;
        }
        if (!Observer.DISPATCH_METHOD.equals(((Method) method).getName())) {
            return false;
        }
        final PsiElement phpClass = method.getParent();
        if (!(phpClass instanceof PhpClass)) {
            return false;
        }
        final String fqn = ((PhpClass) phpClass).getPresentableFQN();

        return Observer.INTERFACE.equals(fqn)
                || Observer.IMPLEMENTATION.equals(fqn)
                || Observer.ENTITY_IMPL.equals(fqn)
                || Observer.STAGING_IMPL.equals(fqn);
    }

    private boolean checkIsElementStringLiteral(final @NotNull PsiElement element) {
        final ASTNode astNode = element.getNode();
        if (astNode == null) {
            return false;
        }
        final IElementType elementType = astNode.getElementType();

        return elementType.equals(PhpTokenTypes.STRING_LITERAL)
                || elementType.equals(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE);
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }
}
