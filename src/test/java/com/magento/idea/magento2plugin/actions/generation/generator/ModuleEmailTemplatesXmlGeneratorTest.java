/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplatesXmlData;
import com.magento.idea.magento2plugin.magento.files.EmailTemplateHtml;
import com.magento.idea.magento2plugin.magento.files.EmailTemplatesXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;

public class ModuleEmailTemplatesXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EMAIL_TEMPLATE_ID_HTML = "custom_email_template_html";
    private static final String EMAIL_TEMPLATE_LABEL_HTML = "Custom Email Template HTML";
    private static final String EMAIL_TEMPLATE_FILE_NAME_HTML = "custom_email_template_html";
    private static final String EMAIL_TEMPLATE_ID_TEXT = "custom_email_template_text";
    private static final String EMAIL_TEMPLATE_LABEL_TEXT = "Custom Email Template TEXT";
    private static final String EMAIL_TEMPLATE_FILE_NAME_TEXT = "custom_email_template_text";

    /**
     * Test generating email template configuration with HTML type.
     */
    public void testGenerateEmailTemplateHtmlXml() {
        final String filePath = this.getFixturePath(EmailTemplatesXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile emailTemplateXmlFile = addEmailTemplateXml(
                EMAIL_TEMPLATE_ID_HTML,
                EMAIL_TEMPLATE_LABEL_HTML,
                EMAIL_TEMPLATE_FILE_NAME_HTML,
                EmailTemplateHtml.HTML_TYPE,
                Areas.adminhtml.toString()
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, emailTemplateXmlFile);
    }

    /**
     * Test generating email template configuration with TEXT type.
     */
    public void testGenerateEmailTemplateTextXml() {
        final String filePath = this.getFixturePath(EmailTemplatesXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile emailTemplateXmlFile = addEmailTemplateXml(
                EMAIL_TEMPLATE_ID_TEXT,
                EMAIL_TEMPLATE_LABEL_TEXT,
                EMAIL_TEMPLATE_FILE_NAME_TEXT,
                EmailTemplateHtml.TEXT_TYPE,
                Areas.frontend.toString()
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, emailTemplateXmlFile);
    }

    /**
     * Test adding two email templates to email_templates.xml.
     */
    public void testAddTwoEmailTemplatesToEmailTemplatesXmlFile() {
        final String filePath = this.getFixturePath(EmailTemplatesXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        addEmailTemplateXml(
                EMAIL_TEMPLATE_ID_HTML,
                EMAIL_TEMPLATE_LABEL_HTML,
                EMAIL_TEMPLATE_FILE_NAME_HTML,
                EmailTemplateHtml.HTML_TYPE,
                Areas.adminhtml.toString()
        );
        final PsiFile emailTemplateXmlFile = addEmailTemplateXml(
                EMAIL_TEMPLATE_ID_TEXT,
                EMAIL_TEMPLATE_LABEL_TEXT,
                EMAIL_TEMPLATE_FILE_NAME_TEXT,
                EmailTemplateHtml.TEXT_TYPE,
                Areas.frontend.toString()
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, emailTemplateXmlFile);
    }

    /**
     * Add email template XML.
     *
     * @param emailTemplateId Email Template ID
     * @param label Email Template Label
     * @param filename Email Template Filename
     * @param type Email Type
     * @param area Area
     * @return PsiFile
     */
    private PsiFile addEmailTemplateXml(
            final String emailTemplateId,
            final String label,
            final String filename,
            final String type,
            final String area
    ) {
        final Project project = myFixture.getProject();
        final EmailTemplatesXmlData emailTemplatesData = new EmailTemplatesXmlData(
                MODULE_NAME,
                emailTemplateId,
                label,
                filename,
                type,
                area
        );
        final ModuleEmailTemplatesXmlGenerator generator = new ModuleEmailTemplatesXmlGenerator(
                emailTemplatesData,
                project
        );

        return generator.generate("test");
    }
}
