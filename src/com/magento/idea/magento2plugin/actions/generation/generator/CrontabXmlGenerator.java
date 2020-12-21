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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.CrontabXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateCrontabXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class CrontabXmlGenerator extends FileGenerator {
    private final Project project;
    private final CrontabXmlData crontabXmlData;
    private boolean isCronGroupDeclared;

    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final CodeStyleManager codeStyleManager;
    private final FindOrCreateCrontabXml findOrCreateCrontabXml;
    private final XmlFilePositionUtil positionUtil;
    private final PsiDocumentManager psiDocumentManager;

    /**
     * Generator for crontab.xml.
     *
     * @param project Project
     * @param crontabXmlData CrontabXmlData
     */
    public CrontabXmlGenerator(
            final Project project,
            final @NotNull CrontabXmlData crontabXmlData
    ) {
        super(project);

        this.project = project;
        this.crontabXmlData = crontabXmlData;

        this.findOrCreateCrontabXml = new FindOrCreateCrontabXml(project);
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.codeStyleManager = CodeStyleManager.getInstance(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    /**
     * Register newly created cronjob in the crontab.xml.
     * If there is not crontab.xml, it will create one.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final String moduleName = this.crontabXmlData.getModuleName();

        final XmlFile crontabXmlFile = (XmlFile) this.findOrCreateCrontabXml.execute(
                actionName,
                moduleName
        );

        final String cronjobGroup = this.crontabXmlData.getCronGroup();
        final String cronjobName = this.crontabXmlData.getCronjobName();
        final XmlTag cronGroupTag = this.getCronGroupTag(crontabXmlFile, cronjobGroup);

        this.isCronGroupDeclared = false;
        boolean isCronjobDeclared = false;

        if (cronGroupTag != null) {
            isCronGroupDeclared = true;
            isCronjobDeclared = this.isCronjobDeclared(cronGroupTag, cronjobName);
        }

        if (isCronjobDeclared) {
            throw new RuntimeException(cronjobName//NOPMD
                + " cronjob is already declared in the " + moduleName + " module");
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final StringBuffer textBuf = new StringBuffer();

            try {
                final String cronjobRegistrationTemplate = this.getCodeTemplateUtil.execute(
                        CrontabXmlTemplate.TEMPLATE_CRONJOB_REGISTRATION,
                        getAttributes()
                );

                textBuf.append(cronjobRegistrationTemplate);
            } catch (IOException e) {
                e.printStackTrace();//NOPMD
                return;
            }

            final int insertPos = this.isCronGroupDeclared
                    ? this.positionUtil.getEndPositionOfTag(cronGroupTag)
                    : this.positionUtil.getRootInsertPosition(crontabXmlFile);

            if (textBuf.length() > 0 && insertPos >= 0) {
                final Document document = this.psiDocumentManager.getDocument(crontabXmlFile);

                if (document == null) {
                    // practically this should not be possible as we tell to edit XML file
                    throw new RuntimeException(//NOPMD
                            crontabXmlFile.getVirtualFile().getPath()
                                + " file is binary or has no document associations"
                    );
                }

                if (!document.isWritable()) {
                    throw new RuntimeException(//NOPMD
                            crontabXmlFile.getVirtualFile().getPath()
                                + " file is not writable. Please check file permission"
                    );
                }

                document.insertString(insertPos, textBuf);
                final int endPos = insertPos + textBuf.length() + 1;

                this.codeStyleManager.reformatText(crontabXmlFile, insertPos, endPos);
                this.psiDocumentManager.commitDocument(document);
            }
        });

        return crontabXmlFile;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        final String cronjobGroup = this.crontabXmlData.getCronGroup();
        final String cronjobSchedule = this.crontabXmlData.getCronjobSchedule();
        final String cronjobScheduleConfigPath = this.crontabXmlData.getCronjobScheduleConfigPath();

        if (!this.isCronGroupDeclared) {
            attributes.setProperty("CRON_GROUP", cronjobGroup);
        }

        if (cronjobSchedule != null) {
            attributes.setProperty("CRONJOB_SCHEDULE", cronjobSchedule);
        }

        if (cronjobScheduleConfigPath != null) {
            attributes.setProperty("CRONJOB_SCHEDULE_CONFIG_PATH", cronjobScheduleConfigPath);
        }

        final String cronjobName = this.crontabXmlData.getCronjobName();
        attributes.setProperty("CRONJOB_NAME", cronjobName);

        final String cronjobInstance = this.crontabXmlData.getCronjobInstance();
        attributes.setProperty("CRONJOB_INSTANCE", cronjobInstance);
    }

    /**
     * Check whenever cronjob with cronjobName is declared under cronGroupTag.
     *
     * @param cronGroupTag XmlTag
     * @param cronjobName String
     *
     * @return boolean
     */
    private boolean isCronjobDeclared(final XmlTag cronGroupTag, final String cronjobName) {
        final XmlTag[] cronjobTags = PsiTreeUtil.getChildrenOfType(cronGroupTag, XmlTag.class);

        if (cronjobTags == null) {
            return false;
        }

        for (final XmlTag cronjobTag: cronjobTags) {
            if (!cronjobTag.getName().equals(CrontabXmlTemplate.CRON_JOB_TAG)) {
                continue;
            }

            final XmlAttribute[] cronjobAttributes = PsiTreeUtil.getChildrenOfType(
                    cronjobTag,
                    XmlAttribute.class
            );

            // todo: handle null pointer

            for (final XmlAttribute cronjobAttribute: cronjobAttributes) {
                if (!cronjobAttribute.getName()
                        .equals(CrontabXmlTemplate.CRON_JOB_NAME_ATTRIBUTE)) {
                    continue;
                }

                if (cronjobName.equals(cronjobAttribute.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Retrieve cronGroup tag with cronjobGroup name if it registered in crontabXmlFile.
     *
     * @param crontabXmlFile XmlFile
     * @param cronjobGroup String
     *
     * @return XmlTag
     */
    private XmlTag getCronGroupTag(final XmlFile crontabXmlFile, final String cronjobGroup) {
        final Collection<XmlAttributeValue> cronGroupIdAttributes
                = XmlPsiTreeUtil.findAttributeValueElements(
                    crontabXmlFile,
                    CrontabXmlTemplate.CRON_GROUP_TAG,
                    CrontabXmlTemplate.CRON_GROUP_NAME_ATTRIBUTE
        );

        for (final XmlAttributeValue cronGroupIdAttribute: cronGroupIdAttributes) {
            if (!cronGroupIdAttribute.getValue().equals(cronjobGroup)) {
                continue;
            }

            return PsiTreeUtil.getParentOfType(cronGroupIdAttribute, XmlTag.class);//NOPMD
        }

        return null;
    }
}
