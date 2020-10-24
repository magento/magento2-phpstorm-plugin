/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplateHtmlData;
import com.magento.idea.magento2plugin.magento.files.EmailTemplateHtml;
import com.magento.idea.magento2plugin.magento.packages.Areas;

public class ModuleEmailTemplateHtmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String EMAIL_TEMPLATE_DIR = "src/app/code/Foo/Bar/view/%s/email";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EMAIL_TEMPLATE_FILE_NAME_HTML = "custom_email_template_html";
    private static final String EMAIL_TEMPLATE_FILE_NAME_TEXT = "custom_email_template_text";
    private static final String EMAIL_TEMPLATE_SUBJECT_HTML = "HTML Email Template Subject";
    private static final String EMAIL_TEMPLATE_SUBJECT_TEXT = "TEXT Email Template Subject";

    /**
     * Test generating email template with HTML type.
     */
    public void testGenerateEmailTemplateHtml() {
        final String filePath = this.getFixturePath(
                String.format(
                        "%s.%s",
                        EMAIL_TEMPLATE_FILE_NAME_HTML,
                        EmailTemplateHtml.HTML_FILE_EXTENSION
                )
        );
        final String area = Areas.adminhtml.toString();
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile emailTemplateXmlFile = generateEmailTemplateHtml(
                EMAIL_TEMPLATE_FILE_NAME_HTML,
                EMAIL_TEMPLATE_SUBJECT_HTML,
                EmailTemplateHtml.HTML_TYPE,
                area
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                emailTemplateXmlFile
        );
    }

    /**
     * Test generating email template with TEXT type.
     */
    public void testGenerateEmailTemplateText() {
        final String filePath = this.getFixturePath(
                String.format(
                        "%s.%s",
                        EMAIL_TEMPLATE_FILE_NAME_TEXT,
                        EmailTemplateHtml.HTML_FILE_EXTENSION
                )
        );
        final String area = Areas.frontend.toString();
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile emailTemplateXmlFile = generateEmailTemplateHtml(
                EMAIL_TEMPLATE_FILE_NAME_TEXT,
                EMAIL_TEMPLATE_SUBJECT_TEXT,
                EmailTemplateHtml.TEXT_TYPE,
                area
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                emailTemplateXmlFile
        );
    }

    /**
     * Generate email template Html file.
     *
     * @param filename Email Template Filename
     * @param subject Email Template Filename
     * @param type Email Type
     * @param area Area
     * @return PsiFile
     */
    private PsiFile generateEmailTemplateHtml(
            final String filename,
            final String subject,
            final String type,
            final String area
    ) {
        final Project project = myFixture.getProject();
        final EmailTemplateHtmlData emailTemplatesData = new EmailTemplateHtmlData(
                MODULE_NAME,
                filename,
                area,
                subject,
                type
        );
        final ModuleEmailTemplateHtmlGenerator generator = new ModuleEmailTemplateHtmlGenerator(
                emailTemplatesData,
                project
        );

        return generator.generate("test");
    }

    private String getExpectedDirectory(final String area) {
        return String.format(EMAIL_TEMPLATE_DIR, area);
    }
}
