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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.InjectAViewModelDialog;
import com.magento.idea.magento2plugin.magento.files.CommonXml;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

public class InjectAViewModelAction extends DumbAwareAction {
    public static String actionName = "Inject a new View Model for this block";
    public static String actionDescription = "Inject a new Magento 2 View Model";
    private XmlTag targetXmlTag;

    public InjectAViewModelAction() {
        super(actionName, actionDescription, MagentoIcons.MODULE);
    }

    @Override
    public void update(final AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        if (Settings.isEnabled(project)) {
            final XmlTag element = getElement(event);
            if (element == null) {
                this.setStatus(event, false);
            } else  {
                targetXmlTag = element;
                this.setStatus(event, true);
            }
        } else {
            this.setStatus(event, false);
        }
    }

    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        InjectAViewModelDialog.open(event.getProject(), this.targetXmlTag);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    private XmlTag getElement(final @NotNull AnActionEvent event) {
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

        final XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);

        if (xmlTag == null) {
            return null;
        }
        XmlTag resultTag;
        if (xmlTag.getName().equals(CommonXml.ATTRIBUTE_ARGUMENTS)) {
            resultTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);
        } else {
            resultTag = xmlTag;
        }
        if (resultTag == null) {
            return null;
        }

        if (!resultTag.getName().equals(LayoutXml.BLOCK_ATTRIBUTE_TAG_NAME)
                && !resultTag.getName().equals(LayoutXml.REFERENCE_BLOCK_ATTRIBUTE_TAG_NAME)) {
            return null;
        }

        return resultTag;
    }
}
