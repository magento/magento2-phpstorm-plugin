/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewEmailTemplateAction;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplateHtmlData;
import com.magento.idea.magento2plugin.actions.generation.data.EmailTemplatesXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.dialog.NewEmailTemplateDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleEmailTemplateHtmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleEmailTemplatesXmlGenerator;
import com.magento.idea.magento2plugin.magento.files.EmailTemplateHtml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class NewEmailTemplateDialog extends AbstractDialog {

    private static final String EMAIL_TEMPLATE_ID = "id";
    private static final String LABEL = "label";
    private static final String FILENAME = "file name";

    private final String moduleName;
    private final Project project;
    private final NewEmailTemplateDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, EMAIL_TEMPLATE_ID})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER,
            message = {IdentifierRule.MESSAGE, EMAIL_TEMPLATE_ID})
    private JTextField identifier;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, LABEL})
    private JTextField label;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, FILENAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER,
            message = {IdentifierRule.MESSAGE, FILENAME})
    private JTextField fileName;

    private FilteredComboBox area;
    private FilteredComboBox templateType;
    private JTextField subject;

    private JLabel identifierErrorMessage;//NOPMD
    private JLabel labelErrorMessage;//NOPMD
    private JLabel fileNameErrorMessage;//NOPMD

    /**
     * New email template dialog.
     *
     * @param project Project
     * @param directory Directory
     */
    public NewEmailTemplateDialog(final Project project, final PsiDirectory directory) {
        super();
        setContentPane(contentPane);
        setModal(true);
        setTitle(NewEmailTemplateAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        this.project = project;
        this.validator = new NewEmailTemplateDialogValidator(project);
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent windowEvent) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                actionEvent -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(new FocusOnAFieldListener(() -> identifier.requestFocusInWindow()));
    }

    /**
     * Get email template data.
     *
     * @return EmailTemplatesData
     */
    public EmailTemplatesXmlData getEmailTemplateData() {
        return new EmailTemplatesXmlData(
                getModuleName(),
                getIdentifier(),
                getLabel(),
                getFileName(),
                getTemplateType(),
                getArea()
        );
    }

    /**
     * Get email template HTML data.
     *
     * @return EmailTemplateHtmlData
     */
    public EmailTemplateHtmlData getEmailTemplateHtmlData() {
        return new EmailTemplateHtmlData(
                getModuleName(),
                getFileName(),
                getArea(),
                getSubject(),
                getTemplateType()
        );
    }

    /**
     * Get identifier.
     *
     * @return String
     */
    public String getIdentifier() {
        return identifier.getText().trim();
    }

    /**
     * Get label.
     *
     * @return String
     */
    public String getLabel() {
        return label.getText().trim();
    }

    /**
     * Get subject.
     *
     * @return String
     */
    public String getSubject() {
        return subject.getText().trim();
    }

    /**
     * Get file name.
     *
     * @return String
     */
    public String getFileName() {
        return fileName.getText().trim();
    }

    /**
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return area.getSelectedItem().toString();
    }

    /**
     * Get template type.
     *
     * @return String
     */
    public String getTemplateType() {
        return templateType.getSelectedItem().toString();
    }

    /**
     * Open new controller dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewEmailTemplateDialog dialog = new NewEmailTemplateDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private String getModuleName() {
        return this.moduleName;
    }

    private void onOK() {
        final boolean emailTemplateCanBeDeclared = !this.validator.validate(this);

        if (!validateFormFields() || emailTemplateCanBeDeclared) {
            return;
        }
        generateFile();
        exit();
    }

    private void generateFile() {
        final ModuleEmailTemplateHtmlGenerator moduleEmailTemplateHtmlGenerator;
        moduleEmailTemplateHtmlGenerator = new ModuleEmailTemplateHtmlGenerator(
                this.getEmailTemplateHtmlData(),
                project
        );
        moduleEmailTemplateHtmlGenerator.generate(NewEmailTemplateAction.ACTION_NAME);
        final ModuleEmailTemplatesXmlGenerator xmlGenerator = new ModuleEmailTemplatesXmlGenerator(
                this.getEmailTemplateData(),
                project
        );
        xmlGenerator.generate(NewEmailTemplateAction.ACTION_NAME, true);
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        this.area = new FilteredComboBox(getAreaList());
        this.templateType = new FilteredComboBox(getTypeList());
    }

    private List<String> getAreaList() {
        return new ArrayList<>(
                Arrays.asList(
                        Areas.frontend.toString(),
                        Areas.adminhtml.toString()
                )
        );
    }

    private List<String> getTypeList() {
        return new ArrayList<>(
                Arrays.asList(
                        EmailTemplateHtml.HTML_TYPE,
                        EmailTemplateHtml.TEXT_TYPE
                )
        );
    }
}
