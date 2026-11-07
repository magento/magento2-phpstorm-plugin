/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ActionUpdateThreadAware;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.progress.ProcessCanceledException;
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
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InjectAViewModelAction extends DumbAwareAction implements ActionUpdateThreadAware {

    public static final String ACTION_NAME = "Inject a new View Model for this block";
    public static final String ACTION_DESCRIPTION = "Inject a new Magento 2 View Model";
    private static final Logger LOG = Logger.getInstance(InjectAViewModelAction.class);
    private XmlTag targetXmlTag;

    /**
     * An action constructor.
     */
    public InjectAViewModelAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    public void update(final @NotNull AnActionEvent event) {
        this.setStatus(event, false);
        targetXmlTag = null;
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project == null || !Settings.isEnabled(project)) {
            return;
        }
        final PsiFile psiFile = getPsiFile(event);

        if (psiFile == null || psiFile.getVirtualFile() == null
                || !IsFileInEditableModuleUtil.execute(psiFile)) {
            return;
        }
        final XmlTag element = getElement(event, psiFile);

        if (element != null) {
            targetXmlTag = element;
            this.setStatus(event, true);
        }
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project == null || targetXmlTag == null) {
            return;
        }
        InjectAViewModelDialog.open(project, targetXmlTag);
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private @Nullable PsiFile getPsiFile(final @NotNull AnActionEvent event) {
        try {
            return event.getData(PlatformDataKeys.PSI_FILE);
        } catch (ProcessCanceledException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            // PSI lookup can initialize the IDE's TypeScript libraries even for a layout XML file (#1924).
            if (exception.getCause() instanceof IOException cause
                    && cause.getMessage() != null
                    && (cause.getMessage().equals("Cannot find jsLanguageServicesImpl")
                    || cause.getMessage().startsWith("Cannot find jsLanguageServicesImpl, additional missing path: "))) {
                LOG.debug("View Model action unavailable: bundled JavaScript resources could not be located", exception);
                return null;
            }
            throw exception;
        }
    }

    /**
     * Get focused (target) element for the event.
     *
     * @param event AnActionEvent
     * @param psiFile current file
     *
     * @return XmlTag
     */
    private XmlTag getElement(final @NotNull AnActionEvent event, final @NotNull PsiFile psiFile) {
        final Caret caret = event.getData(PlatformDataKeys.CARET);

        if (caret == null) {
            return null;
        }

        final int offset = caret.getOffset();
        final PsiElement element = psiFile.findElementAt(offset);

        if (element == null) {
            return null;
        }
        final XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);

        if (xmlTag == null) {
            return null;
        }
        final XmlTag resultTag;

        if (CommonXml.ATTRIBUTE_ARGUMENTS.equals(xmlTag.getName())) {
            resultTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);
        } else {
            resultTag = xmlTag;
        }

        if (resultTag == null) {
            return null;
        }

        if (!LayoutXml.BLOCK_ATTRIBUTE_TAG_NAME.equals(resultTag.getName())
                && !LayoutXml.REFERENCE_BLOCK_ATTRIBUTE_TAG_NAME.equals(resultTag.getName())) {
            return null;
        }

        return resultTag;
    }

    /**
     * Set presentation status.
     *
     * @param event AnActionEvent
     * @param status boolean
     */
    private void setStatus(final AnActionEvent event, final boolean status) {
        event.getPresentation().setVisible(status);
        event.getPresentation().setEnabled(status);
    }
}
