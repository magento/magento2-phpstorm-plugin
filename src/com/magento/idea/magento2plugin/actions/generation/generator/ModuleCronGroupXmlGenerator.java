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
import com.magento.idea.magento2plugin.actions.generation.data.CronGroupXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateCronGroupXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.CronGroupXmlTemplate;
import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import javax.swing.JOptionPane;

@SuppressWarnings({"PMD.AvoidPrintStackTrace"})
public class ModuleCronGroupXmlGenerator extends FileGenerator {
    private final CronGroupXmlData cronGroupXmlData;
    private final Project project;
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final XmlFilePositionUtil positionUtil;
    private final PsiDocumentManager psiDocumentManager;
    private final FindOrCreateCronGroupXml findOrCreateCronGroupsXml;
    private final CodeStyleManager codeStyleManager;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Module CRON group XML generator constructor.
     *
     * @param cronGroupXmlData CRON group XML data
     * @param project Project
     */
    public ModuleCronGroupXmlGenerator(
            final CronGroupXmlData cronGroupXmlData,
            final Project project
    ) {
        super(project);
        this.project = project;
        this.cronGroupXmlData = cronGroupXmlData;
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        this.findOrCreateCronGroupsXml = new FindOrCreateCronGroupXml(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.codeStyleManager = CodeStyleManager.getInstance(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Generate CRON group XML file.
     *
     * @param actionName Action name
     * @return PsiFile
     */
    public PsiFile generate(final String actionName) {
        final String moduleName = this.cronGroupXmlData.getModule();
        final XmlFile cronGroupXmlFile = (XmlFile) this.findOrCreateCronGroupsXml.execute(
                actionName,
                moduleName
        );
        final String groupName = this.cronGroupXmlData.getGroupName();
        final XmlTag cronGroupTag = this.getCronGroupTag(cronGroupXmlFile, groupName);

        if (cronGroupTag != null) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.alreadyDeclared",
                    String.format("%s CRON Group", groupName),
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
                final String cronjobRegistrationTemplate = this.getCodeTemplateUtil.execute(
                        CronGroupXmlTemplate.TEMPLATE_CRON_GROUP_REGISTRATION,
                        getAttributes()
                );

                textBuf.append(cronjobRegistrationTemplate);
            } catch (final IOException exception) {
                exception.printStackTrace();
                return;
            }

            final int insertPos = this.positionUtil.getRootInsertPosition(cronGroupXmlFile);

            if (textBuf.length() > 0 && insertPos >= 0) {
                final Document document = this.psiDocumentManager.getDocument(cronGroupXmlFile);
                final String filePath = cronGroupXmlFile.getVirtualFile().getPath();

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

                this.codeStyleManager.reformatText(cronGroupXmlFile, insertPos, endPos);
                this.psiDocumentManager.commitDocument(document);
            }
        });

        return cronGroupXmlFile;
    }

    /**
     * Fill attributes.
     *
     * @param attributes attributes
     */
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("GROUP_NAME", cronGroupXmlData.getGroupName());

        if (cronGroupXmlData.getScheduleGenerateEvery() != null) {
            attributes.setProperty(
                    "SCHEDULE_GENERATE_EVERY",
                    cronGroupXmlData.getScheduleGenerateEvery().toString()
            );
        }

        if (cronGroupXmlData.getScheduleAheadFor() != null) {
            attributes.setProperty(
                    "SCHEDULE_AHEAD_FOR",
                    cronGroupXmlData.getScheduleAheadFor().toString()
            );
        }

        if (cronGroupXmlData.getScheduleLifetime() != null) {
            attributes.setProperty(
                    "SCHEDULE_LIFETIME",
                    cronGroupXmlData.getScheduleLifetime().toString()
            );
        }

        if (cronGroupXmlData.getHistoryCleanupEvery() != null) {
            attributes.setProperty(
                    "HISTORY_CLEANUP_EVERY",
                    cronGroupXmlData.getHistoryCleanupEvery().toString()
            );
        }

        if (cronGroupXmlData.getHistorySuccessLifetime() != null) {
            attributes.setProperty(
                    "HISTORY_SUCCESS_LIFETIME",
                    cronGroupXmlData.getHistorySuccessLifetime().toString()
            );
        }

        if (cronGroupXmlData.getHistoryFailureLifetime() != null) {
            attributes.setProperty(
                    "HISTORY_FAILURE_LIFETIME",
                    cronGroupXmlData.getHistoryFailureLifetime().toString()
            );
        }

        if (cronGroupXmlData.getUseSeparateProcess() != null) {
            attributes.setProperty(
                    "USE_SEPARATE_PROCESS",
                    cronGroupXmlData.getUseSeparateProcess().toString()
            );
        }
    }

    /**
     * Retrieve cronGroup tag with cronjobGroup name if it registered in cron_groups.xml file.
     *
     * @param cronGroupXmlFile CRON group XML file
     * @param cronGroupName CRON group name
     * @return XmlTag
     */
    private XmlTag getCronGroupTag(final XmlFile cronGroupXmlFile, final String cronGroupName) {
        final Collection<XmlAttributeValue> attributes = XmlPsiTreeUtil.findAttributeValueElements(
                cronGroupXmlFile,
                CrontabXmlTemplate.CRON_GROUP_TAG,
                CrontabXmlTemplate.CRON_GROUP_NAME_ATTRIBUTE
        );

        for (final XmlAttributeValue cronGroupIdAttribute: attributes) {
            if (cronGroupIdAttribute.getValue().equals(cronGroupName)) {
                return PsiTreeUtil.getParentOfType(cronGroupIdAttribute, XmlTag.class);
            }
        }

        return null;
    }
}
