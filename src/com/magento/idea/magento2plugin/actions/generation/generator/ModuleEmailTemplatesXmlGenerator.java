/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplatesXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateEmailTemplatesXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.EmailTemplatesXml;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import javax.swing.JOptionPane;

@SuppressWarnings({"PMD.AvoidPrintStackTrace"})
public class ModuleEmailTemplatesXmlGenerator extends FileGenerator {
    private final EmailTemplatesXmlData emailTemplatesData;
    private final Project project;
    private final FindOrCreateEmailTemplatesXml findOrCreateEmailTemplatesXml;
    private final GetCodeTemplate getCodeTemplate;
    private final XmlFilePositionUtil positionUtil;
    private final PsiDocumentManager psiDocumentManager;
    private final CodeStyleManager codeStyleManager;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Module email templates XML generator constructor.
     *
     * @param emailTemplatesData EmailTemplatesData
     * @param project Project
     */
    public ModuleEmailTemplatesXmlGenerator(
            final EmailTemplatesXmlData emailTemplatesData,
            final Project project
    ) {
        super(project);
        this.project = project;
        this.emailTemplatesData = emailTemplatesData;
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
        this.findOrCreateEmailTemplatesXml = new FindOrCreateEmailTemplatesXml(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.codeStyleManager = CodeStyleManager.getInstance(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }


    /**
     * Generate email template XML file.
     *
     * @param actionName Action name
     * @return PsiFile
     */
    public PsiFile generate(final String actionName) {
        final String moduleName = this.emailTemplatesData.getModule();
        final XmlFile emailTemplateXmlFile = (XmlFile) this.findOrCreateEmailTemplatesXml.execute(
                actionName,
                moduleName
        );
        final String groupName = this.emailTemplatesData.getId();
        final XmlTag emailTemplateTag = this.getTemplateTag(emailTemplateXmlFile, groupName);

        if (emailTemplateTag != null) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.alreadyDeclared",
                    String.format("%s Email Template", groupName),
                    moduleName
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    commonBundle.message("common.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final StringBuffer textBuf = new StringBuffer();

            try {
                final String emailTemplateRegistrationTemplate = this.getCodeTemplate.execute(
                        EmailTemplatesXml.EMAIL_TEMPLATE_REGISTRATION,
                        getAttributes()
                );

                textBuf.append(emailTemplateRegistrationTemplate);
            } catch (final IOException exception) {
                exception.printStackTrace();
                return;
            }

            final int insertPos = this.positionUtil.getRootInsertPosition(emailTemplateXmlFile);

            if (textBuf.length() > 0 && insertPos >= 0) {
                final Document document = this.psiDocumentManager.getDocument(emailTemplateXmlFile);
                final String filePath = emailTemplateXmlFile.getVirtualFile().getPath();

                if (document == null) {
                    // practically this should not be possible as we tell to edit XML file
                    final String errorMessage = this.validatorBundle.message(
                            "validator.file.noDocumentAssociations",
                            filePath
                    );
                    JOptionPane.showMessageDialog(
                            null,
                            errorMessage,
                            commonBundle.message("common.error"),
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                if (!document.isWritable()) {
                    final String errorMessage = this.validatorBundle.message(
                            "validator.file.isNotWritable",
                            filePath
                    );
                    JOptionPane.showMessageDialog(
                            null,
                            errorMessage,
                            commonBundle.message("common.error"),
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                document.insertString(insertPos, textBuf);
                final int endPos = insertPos + textBuf.length() + 1;

                this.codeStyleManager.reformatText(emailTemplateXmlFile, insertPos, endPos);
                this.psiDocumentManager.commitDocument(document);
            }
        });

        return emailTemplateXmlFile;
    }

    /**
     * Fill attributes.
     *
     * @param attributes attributes
     */
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("ID", emailTemplatesData.getId());
        attributes.setProperty("LABEL", emailTemplatesData.getLabel());
        attributes.setProperty("FILE_NAME", emailTemplatesData.getTemplateFileName());
        attributes.setProperty("TYPE", emailTemplatesData.getType());
        attributes.setProperty("MODULE", emailTemplatesData.getModule());
        attributes.setProperty("AREA", emailTemplatesData.getArea());
    }

    /**
     * Retrieve template tag with email template id if it registered in email_templates.xml file.
     *
     * @param emailTemplatesXmlFile email templates XML file
     * @param id Email template ID
     * @return XmlTag
     */
    private XmlTag getTemplateTag(final XmlFile emailTemplatesXmlFile, final String id) {
        final Collection<XmlAttributeValue> attributes = XmlPsiTreeUtil.findAttributeValueElements(
                emailTemplatesXmlFile,
                EmailTemplatesXml.TEMPLATE_TAG,
                EmailTemplatesXml.TEMPLATE_ID_ATTRIBUTE
        );

        for (final XmlAttributeValue emailTemplateAttribute: attributes) {
            if (emailTemplateAttribute.getValue().equals(id)) {
                return PsiTreeUtil.getParentOfType(emailTemplateAttribute, XmlTag.class);
            }
        }

        return null;
    }
}
