/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ActionUpdateThreadAware;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAPluginDialog;
import com.magento.idea.magento2plugin.inspections.php.util.PhpClassImplementsNoninterceptableInterfaceUtil;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.magento.plugin.IsPluginAllowedForMethodUtil;
import org.jetbrains.annotations.NotNull;

public class CreateAPluginAction extends DumbAwareAction implements ActionUpdateThreadAware {

    public static final String ACTION_NAME = "Create a new Plugin";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 Plugin";
    private final GetFirstClassOfFile getFirstClassOfFile;
    private Method targetMethod;
    private PhpClass targetClass;

    /**
     * Constructor.
     */
    public CreateAPluginAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
    }

    /**
     * Updates the state of action.
     */
    @Override
    public void update(final AnActionEvent event) {
        targetClass = null;// NOPMD
        targetMethod = null;// NOPMD
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project != null && Settings.isEnabled(project)) {
            final Pair<PsiFile, PhpClass> pair = this.findPhpClass(event);
            final PsiFile psiFile = pair.getFirst();
            final PhpClass phpClass = pair.getSecond();

            if (phpClass == null
                    || !(psiFile instanceof PhpFile)
                    || phpClass.isFinal()
                    || PhpClassImplementsNoninterceptableInterfaceUtil.execute(phpClass)
            ) {
                this.setStatus(event, false);
                return;
            }
            targetClass = phpClass;
            this.setStatus(event, true);
            return;
        }

        this.setStatus(event, false);
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null) {
            return;
        }
        CreateAPluginDialog.open(project, this.targetMethod, this.targetClass);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private Pair<PsiFile, PhpClass> findPhpClass(final @NotNull AnActionEvent event) {
        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        PhpClass phpClass = null;
        if (psiFile instanceof PhpFile) {
            phpClass = getFirstClassOfFile.execute((PhpFile) psiFile);
            fetchTargetMethod(event, psiFile, phpClass);
        }

        return Pair.create(psiFile, phpClass);
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    private void fetchTargetMethod(
            final @NotNull AnActionEvent event,
            final PsiFile psiFile,
            final PhpClass phpClass
    ) {
        final Caret caret = event.getData(PlatformDataKeys.CARET);

        if (caret == null) {
            return;
        }
        final int offset = caret.getOffset();
        final PsiElement element = psiFile.findElementAt(offset);

        if (element == null) {
            return;
        }

        if (element instanceof Method && element.getParent().equals(phpClass)
                && IsPluginAllowedForMethodUtil.check((Method) element)) {
            this.targetMethod = (Method) element;
            return;
        }

        if (element instanceof PhpClass) {
            return;
        }

        final PsiElement parent = element.getParent();

        if (parent instanceof Method && parent.getParent().equals(phpClass)
                && IsPluginAllowedForMethodUtil.check((Method) parent)) {
            this.targetMethod = (Method) parent;
        }
    }
}
