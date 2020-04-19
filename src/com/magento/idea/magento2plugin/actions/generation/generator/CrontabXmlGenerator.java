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
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 *
 */
public class CrontabXmlGenerator extends FileGenerator {
    private Project project;
    private CrontabXmlData crontabXmlData;
    private boolean isCronGroupDeclared;

    private GetCodeTemplate getCodeTemplate;
    private CodeStyleManager codeStyleManager;
    private FindOrCreateCrontabXml findOrCreateCrontabXml;
    private XmlFilePositionUtil positionUtil;
    private PsiDocumentManager psiDocumentManager;

    public CrontabXmlGenerator(Project project, @NotNull CrontabXmlData crontabXmlData) {
        super(project);

        this.project = project;
        this.crontabXmlData = crontabXmlData;

        this.findOrCreateCrontabXml = new FindOrCreateCrontabXml(project);
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.codeStyleManager = CodeStyleManager.getInstance(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    /**
     *
     * @param actionName
     *
     * @return PsiFile
     */
    public PsiFile generate(String actionName) {
        String moduleName = this.crontabXmlData.getModuleName();

        XmlFile crontabXmlFile = (XmlFile) this.findOrCreateCrontabXml.execute(
            actionName,
            moduleName
        );

        String cronjobGroup = this.crontabXmlData.getCronGroup();
        String cronjobName = this.crontabXmlData.getCronjobName();
        XmlTag cronGroupTag = this.getCronGroupTag(crontabXmlFile, cronjobGroup);

        this.isCronGroupDeclared = false;
        boolean isCronjobDeclared = false;

        if (cronGroupTag != null) {
            isCronGroupDeclared = true;
            isCronjobDeclared = this.isCronjobDeclared(cronGroupTag, cronjobName);
        }

        if (isCronjobDeclared) {
            // todo: throw an exception / show validation error
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            StringBuffer textBuf = new StringBuffer();

            try {
                String cronjobRegistrationTemplate = this.getCodeTemplate.execute(
                    CrontabXmlTemplate.TEMPLATE_CRONJOB_REGISTRATION,
                    getAttributes()
                );

                textBuf.append(cronjobRegistrationTemplate);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int insertPos = this.isCronGroupDeclared
                    ? this.positionUtil.getEndPositionOfTag(cronGroupTag)
                    : this.positionUtil.getRootInsertPosition(crontabXmlFile);

            if (textBuf.length() > 0 && insertPos >= 0) {
                Document document = this.psiDocumentManager.getDocument(crontabXmlFile);

                // todo: handle possible null pointers
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;

                this.codeStyleManager.reformatText(crontabXmlFile, insertPos, endPos);
                this.psiDocumentManager.commitDocument(document);
            }
        });

        return crontabXmlFile;
    }

    protected void fillAttributes(Properties attributes) {
        String cronjobName = this.crontabXmlData.getCronjobName();
        String cronjobGroup = this.crontabXmlData.getCronGroup();
        String cronjobInstance = this.crontabXmlData.getCronjobInstance();
        String cronjobSchedule = this.crontabXmlData.getCronjobSchedule();
        String cronjobScheduleConfigPath = this.crontabXmlData.getCronjobScheduleConfigPath();

        if (!this.isCronGroupDeclared) {
            attributes.setProperty("CRON_GROUP", cronjobGroup);
        }

        if (cronjobSchedule != null) {
            attributes.setProperty("CRONJOB_SCHEDULE", cronjobSchedule);
        }

        if (cronjobScheduleConfigPath != null) {
            attributes.setProperty("CRONJOB_SCHEDULE_CONFIG_PATH", cronjobScheduleConfigPath);
        }

        attributes.setProperty("CRONJOB_NAME", cronjobName);
        attributes.setProperty("CRONJOB_INSTANCE", cronjobInstance);
    }

    /**
     * Check whenever cronjob with cronjobName is declared under cronGroupTag
     *
     * @param cronGroupTag
     * @param cronjobName
     *
     * @return boolean
     */
    private boolean isCronjobDeclared(XmlTag cronGroupTag, String cronjobName) {
        XmlTag[] cronjobTags = PsiTreeUtil.getChildrenOfType(cronGroupTag, XmlTag.class);

        if (cronjobTags == null) {
            return false;
        }

        for (XmlTag cronjobTag: cronjobTags) {
            if (!cronjobTag.getName().equals(CrontabXmlTemplate.CRON_JOB_TAG)) {
                continue;
            }

            XmlAttribute[] cronjobAttributes = PsiTreeUtil.getChildrenOfType(cronjobTag, XmlAttribute.class);

            // todo: handle null pointer

            for (XmlAttribute cronjobAttribute: cronjobAttributes) {
                if (!cronjobAttribute.getName().equals(CrontabXmlTemplate.CRON_JOB_NAME_ATTRIBUTE)) {
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
     * Retrieve cronGroup tag with cronjobGroup name if it registered in crontabXmlFile
     *
     * @param crontabXmlFile
     * @param cronjobGroup
     *
     * @return XmlTag
     */
    private XmlTag getCronGroupTag(XmlFile crontabXmlFile, String cronjobGroup) {
        Collection<XmlAttributeValue> cronGroupIdAttributes = XmlPsiTreeUtil.findAttributeValueElements(
            crontabXmlFile,
            CrontabXmlTemplate.CRON_GROUP_TAG,
            CrontabXmlTemplate.CRON_GROUP_NAME_ATTRIBUTE
        );

        for (XmlAttributeValue cronGroupIdAttribute: cronGroupIdAttributes) {
            if (!cronGroupIdAttribute.getValue().equals(cronjobGroup)) {
                continue;
            }

            return PsiTreeUtil.getParentOfType(cronGroupIdAttribute, XmlTag.class);
        }

        return null;
    }
}
