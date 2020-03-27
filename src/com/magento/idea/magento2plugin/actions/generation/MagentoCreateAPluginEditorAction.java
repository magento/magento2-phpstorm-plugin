/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.MagentoCreateAPluginDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewMagentoModuleDialog;
import com.magento.idea.magento2plugin.util.magento.plugin.IsPluginAllowedForMethod;
import org.jetbrains.annotations.NotNull;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

public class MagentoCreateAPluginEditorAction extends DumbAwareAction {
    private final IsPluginAllowedForMethod isPluginAllowed;
    private Method targetMethod;
    private PhpClass targetClass;

    public MagentoCreateAPluginEditorAction() {
        super("Create A Magento 2 Plugin", "Create a new Magento 2 plugin for the class", MagentoIcons.MODULE);
        this.isPluginAllowed = IsPluginAllowedForMethod.getInstance();
    }

    public void update(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if (Settings.isEnabled(project)) {
            Pair<PsiFile, PhpClass> pair = this.findPhpClass(event);
            PsiFile psiFile = pair.getFirst();
            PhpClass phpClass = pair.getSecond();
            if (!(psiFile instanceof PhpFile) || phpClass.isFinal()) {
                this.setStatus(event, false);
                return;
            }
        } else {
            this.setStatus(event, false);
            return;
        }
        this.setStatus(event, true);
    }

    private void setStatus(AnActionEvent event, boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        MagentoCreateAPluginDialog.open(e.getProject());
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    private Pair<PsiFile, PhpClass> findPhpClass(@NotNull AnActionEvent event) {
        PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);

        PhpClass phpClass = null;
        if (psiFile instanceof PhpFile) {
            phpClass = getFirstClassFromFile((PhpFile) psiFile);
            fetchTargetMethod(event, psiFile, phpClass);
        }

        return Pair.create(psiFile, phpClass);
    }

    private void fetchTargetMethod(@NotNull AnActionEvent event, PsiFile psiFile, PhpClass phpClass) {
        Caret caret = event.getData(PlatformDataKeys.CARET);
        if (caret == null) {
            return;
        }
        int offset = caret.getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            return;
        }
        if (element instanceof Method && element.getParent() == phpClass && isPluginAllowed.check((Method)element)) {
            this.targetMethod = (Method)element;
            return;
        }
        PsiElement parent = element.getParent();
        if (parent instanceof Method && parent.getParent() == phpClass && isPluginAllowed.check((Method)parent)) {
            this.targetMethod = (Method)parent;
        }
    }

    @Nullable
    private PhpClass getFirstClassFromFile(PhpFile phpFile) {
        Collection<PhpClass> phpClasses = PsiTreeUtil.collectElementsOfType(phpFile, PhpClass.class);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }
}
