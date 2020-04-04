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
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAnObserverDialog;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

public class CreateAnObserverAction extends DumbAwareAction {
    public static final String ACTION_NAME = "Create a Magento Observer...";
    static final String ACTION_DESCRIPTION = "Create a new Magento 2 Observer for the event";
    static final String SIGNATURE = "#M#C\\Magento\\Framework\\Event\\ManagerInterface.dispatch|#M#M#C\\Magento\\Framework\\App\\Action\\Context.getEventManager.dispatch";
    public String targetEvent;

    public CreateAnObserverAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    public void update(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if (!Settings.isEnabled(project)) {
            this.setStatus(event, false);
            return;
        }
        PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);
        if (!(psiFile instanceof PhpFile)) {
            this.setStatus(event, false);
            return;
        }

        PsiElement element = getElement(event);
        if (element == null) {
            this.setStatus(event, false);
            return;
        }

        if (isObserverEventNameClicked(element)) {
            this.setStatus(event, true);
            targetEvent = element.getText();
            return;
        }

        if (isDispatchMethodClicked(element)) {
            //@TODO implement fetching from the dispatch method
            //this.setStatus(event, true);
            //targetEvent = fetchTargetEventName(element);
            //return;
        }

        this.setStatus(event, false);
    }

    private PsiElement getElement(@NotNull AnActionEvent event) {
        Caret caret = event.getData(PlatformDataKeys.CARET);
        PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);
        if (caret == null) {
            return null;
        }
        int offset = caret.getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            return null;
        }
        return element;
    }

    private boolean isObserverEventNameClicked(@NotNull PsiElement element) {
        if (checkIsElementStringLiteral(element)) {
            //@TODO check if quotes are clicked

            if (checkIsParametersList(element.getParent().getParent()) &&
                    checkIsMethodReference(element.getParent().getParent().getParent()) &&
                    checkIsEventDispatchMethod((MethodReference) element.getParent().getParent().getParent()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkIsParametersList(@NotNull PsiElement element) {
        if (element instanceof ParameterList) {
            return true;
        }
        return false;
    }

    private boolean checkIsMethodReference(@NotNull PsiElement element) {
        if (element instanceof MethodReference) {
            return true;
        }
        return false;
    }

    private boolean checkIsEventDispatchMethod(MethodReference element) {
        return element.getSignature().equals(SIGNATURE);
    }

    private boolean checkIsElementStringLiteral(@NotNull PsiElement element) {
        ASTNode astNode = element.getNode();
        if (astNode == null) {
            return false;
        }
        IElementType elementType = astNode.getElementType();

        if (elementType != PhpTokenTypes.STRING_LITERAL && elementType != PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE) {
            return false;
        }

        return true;
    }

    private boolean checkIsElementMethodReference(@NotNull PsiElement element) {
        ASTNode astNode = element.getNode();
        if (astNode == null) {
            return false;
        }
        IElementType elementType = astNode.getElementType();
        if (elementType != PhpTokenTypes.IDENTIFIER) {
            return false;
        }

        return true;
    }

    private boolean isDispatchMethodClicked(@NotNull PsiElement element) {
        if (checkIsElementMethodReference(element) &&
                checkIsMethodReference(element.getParent()) &&
                checkIsEventDispatchMethod((MethodReference) element.getParent())) {
            return true;
        }

        return false;
    }

    private String fetchTargetEventName(@NotNull PsiElement element) {
        //@TODO implement fetching event name from the dispatch method
        return null;
    }

    private void setStatus(AnActionEvent event, boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CreateAnObserverDialog.open(e.getProject(), this.targetEvent);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
