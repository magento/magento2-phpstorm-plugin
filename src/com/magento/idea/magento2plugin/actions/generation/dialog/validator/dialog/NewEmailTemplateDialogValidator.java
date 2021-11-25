/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplateHtmlData;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplatesXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewEmailTemplateDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.EmailTemplatesXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.Collection;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class NewEmailTemplateDialogValidator {

    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final Project project;

    /**
     * Constructor.
     */
    public NewEmailTemplateDialogValidator(final Project project) {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.project = project;
    }

    /**
     * Validate dialog.
     *
     * @param dialog NewEmailTemplateDialog
     * @return boolean
     */
    public boolean validate(final @NotNull NewEmailTemplateDialog dialog) {
        final EmailTemplatesXmlData emailTemplatesXmlData = dialog.getEmailTemplateData();
        final String errorTitle = commonBundle.message("common.error");
        final EmailTemplateHtmlData emailTemplateHtmlData = dialog.getEmailTemplateHtmlData();

        if (isTemplateFileAlreadyExists(emailTemplateHtmlData)) {
            final String templateFileName = emailTemplatesXmlData.getTemplateFileName();
            final String errorMessage = this.validatorBundle.message(
                    "validator.alreadyDeclared",
                    String.format("%s Email Template File", templateFileName),
                    emailTemplatesXmlData.getModule()
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (isTemplateTagAlreadyExists(emailTemplatesXmlData)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.alreadyDeclared",
                    String.format("%s Email Template", emailTemplatesXmlData.getId()),
                    emailTemplatesXmlData.getModule()
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return true;
    }

    /**
     * Is template tag already exists.
     *
     * @param emailTemplatesXmlData email templates XML data
     *
     * @return bool
     */
    private boolean isTemplateTagAlreadyExists(
            final @NotNull EmailTemplatesXmlData emailTemplatesXmlData
    ) {
        final EmailTemplatesXml emailTemplatesXml = new EmailTemplatesXml();
        final XmlFile emailTemplatesFile = (XmlFile) FileBasedIndexUtil.findModuleConfigFile(
                emailTemplatesXml.getFileName(),
                Areas.base,
                emailTemplatesXmlData.getModule(),
                project
        );

        if (emailTemplatesFile == null) {
            return false;
        }

        final Collection<XmlAttributeValue> attributes = XmlPsiTreeUtil.findAttributeValueElements(
                emailTemplatesFile,
                EmailTemplatesXml.TEMPLATE_TAG,
                EmailTemplatesXml.TEMPLATE_ID_ATTRIBUTE,
                emailTemplatesXmlData.getId()
        );

        for (final XmlAttributeValue emailTemplateAttribute: attributes) {
            final XmlTag templateXmlTag = PsiTreeUtil.getParentOfType(
                    emailTemplateAttribute,
                    XmlTag.class
            );
            final String existingTemplateArea = templateXmlTag.getAttribute("area").getValue();

            if (existingTemplateArea.equals(emailTemplatesXmlData.getArea())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Is template file already exists.
     *
     * @param emailTemplateData EmailTemplateHtmlData
     * @return boolean
     */
    private boolean isTemplateFileAlreadyExists(
            final @NotNull EmailTemplateHtmlData emailTemplateData
    ) {
        final PsiFile templateFile = FileBasedIndexUtil.findModuleViewFile(
                emailTemplateData.getFileName(),
                getArea(emailTemplateData.getArea()),
                emailTemplateData.getModule(),
                project,
                Package.moduleViewEmailDir
        );

        return templateFile != null;
    }

    private Areas getArea(final String area) {
        return Areas.getAreaByString(area);
    }
}
