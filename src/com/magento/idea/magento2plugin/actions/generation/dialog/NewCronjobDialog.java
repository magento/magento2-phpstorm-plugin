/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewCronjobAction;
import com.magento.idea.magento2plugin.actions.generation.data.CronjobClassData;
import com.magento.idea.magento2plugin.actions.generation.data.CrontabXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.BoxNotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ConfigPathRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.CronScheduleRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.CronjobClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.CrontabXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.indexes.CronGroupIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SuppressWarnings({
        "PMD.UncommentedEmptyMethodBody",
        "PMD.OnlyOneReturn",
        "PMD.UnusedPrivateMethod",
        "PMD.CommentSize",
        "PMD.TooManyFields",
        "PMD.MissingSerialVersionUID",
        "PMD.AvoidCatchingGenericException",
        "PMD.ImmutableField",
        "PMD.AccessorMethodGeneration",
        "PMD.ExcessiveImports",
})
public class NewCronjobDialog extends AbstractDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton fixedScheduleRadioButton;
    private JRadioButton configurableScheduleRadioButton;
    private JRadioButton everyMinuteRadioButton;
    private JRadioButton customScheduleRadioButton;
    private JRadioButton atMidnightRadioButton;
    private JPanel fixedSchedulePanel;
    private JPanel configurableSchedulePanel;
    private static final String CLASS_NAME = "class name";
    private static final String DIRECTORY = "directory";
    private static final String CRON_NAME = "name";
    private static final String SCHEDULE = "schedule";
    private static final String CONFIG_PATH = "config path";
    private static final String CRON_GROUP = "cron group";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CLASS_NAME})
    private JTextField cronjobClassNameField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DIRECTORY})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, DIRECTORY})
    private JTextField cronjobDirectoryField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CRON_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER,
            message = {IdentifierRule.MESSAGE, CRON_NAME})
    private JTextField cronjobNameField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, SCHEDULE})
    @FieldValidation(rule = RuleRegistry.CRON_SCHEDULE,
            message = {CronScheduleRule.MESSAGE, SCHEDULE})
    private JTextField cronjobScheduleField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONFIG_PATH})
    @FieldValidation(rule = RuleRegistry.CONFIG_PATH,
            message = {ConfigPathRule.MESSAGE, CONFIG_PATH})
    private JTextField configPathField;

    @FieldValidation(rule = RuleRegistry.BOX_NOT_EMPTY,
            message = {BoxNotEmptyRule.MESSAGE, CRON_GROUP})
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CRON_GROUP})
    private FilteredComboBox cronGroupComboBox;

    private Project project;
    private String moduleName;
    private CamelCaseToSnakeCase camelCaseToSnakeCase;

    /**
     * Open a new cronjob generation dialog form.
     *
     * @param project Project
     * @param directory Directory
     */
    public NewCronjobDialog(final Project project, final PsiDirectory directory) {
        super();
        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.camelCaseToSnakeCase = CamelCaseToSnakeCase.getInstance();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(NewCronjobAction.ACTION_DESCRIPTION);
        configPathField.setEditable(false);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        fixedScheduleRadioButton.addActionListener(e -> {
            configurableSchedulePanel.setVisible(false);
            fixedSchedulePanel.setVisible(true);
            configPathField.setEditable(false);
        });

        configurableScheduleRadioButton.addActionListener(e -> {
            fixedSchedulePanel.setVisible(false);
            configurableSchedulePanel.setVisible(true);
            configPathField.setEditable(true);
            configPathField.grabFocus();
            everyMinuteRadioButton.doClick();
        });

        everyMinuteRadioButton.addActionListener(e -> {
            cronjobScheduleField.setEditable(false);
            cronjobScheduleField.setText("* * * * *");
        });

        atMidnightRadioButton.addActionListener(e -> {
            cronjobScheduleField.setEditable(false);
            cronjobScheduleField.setText("0 0 * * *");
        });

        customScheduleRadioButton.addActionListener(e -> {
            cronjobScheduleField.setText("* * * * *");
            cronjobScheduleField.setEditable(true);
            cronjobScheduleField.grabFocus();
        });

        // suggest unique cronjob name based on the module and class names
        cronjobNameField.setText(this.suggestCronjobName("CleanTableCronjob"));
        cronjobClassNameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {

            }

            @Override
            public void focusLost(final FocusEvent event) {
                cronjobNameField.setText(suggestCronjobName(cronjobClassNameField.getText()));
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Open a new cronjib dialog form.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewCronjobDialog dialog = new NewCronjobDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    public String getCronjobClassName() {
        return this.cronjobClassNameField.getText().trim();
    }

    public String getCronjobDirectory() {
        return this.cronjobDirectoryField.getText().trim();
    }

    public String getCronjobModule() {
        return this.moduleName;
    }

    public String getCronjobName() {
        return this.cronjobNameField.getText().trim();
    }

    public String getCronjobGroup() {
        return this.cronGroupComboBox.getSelectedItem().toString();
    }

    public boolean isFixedScheduleType() {
        return this.fixedScheduleRadioButton.isSelected();
    }

    public String getCronjobSchedule() {
        return this.cronjobScheduleField.getText().trim();
    }

    public boolean isConfigurableScheduleType() {
        return this.configurableScheduleRadioButton.isSelected();
    }

    public String getCronjobScheduleConfigPath() {
        return this.configPathField.getText().trim();
    }

    @Override
    protected void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        final List<String> cronGroups = CronGroupIndex.getInstance(project).getGroups();

        this.cronGroupComboBox = new FilteredComboBox(cronGroups);
    }

    /**
     * Retrieve the default cronjob name.
     *
     * @return String
     */
    private String suggestCronjobName(final String cronjobClassname) {
        if (cronjobClassname == null || cronjobClassname.isEmpty()) {
            return this.moduleName.toLowerCase(new java.util.Locale("en","EN"));
        }

        final String cronjobClassnameToSnakeCase = this.camelCaseToSnakeCase.convert(
                cronjobClassname
        );

        return this.moduleName.toLowerCase(new java.util.Locale("en","EN"))
                + "_"
                + cronjobClassnameToSnakeCase;
    }

    /**
     * When new cronjob dialog is filled, validate the input data and generate a new cronjob.
     */
    private void onOK() {
        if (!validateFormFields()) {
            return;
        }

        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                this.getCronjobModule(),
                this.getCronjobClassName(),
                this.getCronjobDirectory()
        );

        final String cronjobNamespace = namespaceBuilder.getNamespace();
        final String cronjobInstance = namespaceBuilder.getClassFqn();

        final CronjobClassData cronjobClassData = this.getCronjobClassData(cronjobNamespace);
        final CrontabXmlData crontabXmlData = this.getCrontabXmlData(cronjobInstance);

        // todo: catch validation exceptions
        this.generate(cronjobClassData, crontabXmlData);
        this.setVisible(false);
    }

    /**
     * Generate new cronjob file and register it in crontab.xml.
     */
    private void generate(
            final CronjobClassData cronjobClassData,
            final CrontabXmlData crontabXmlData) {

        final CronjobClassGenerator cronjobFileGenerator = new CronjobClassGenerator(
                project,
                cronjobClassData
        );

        final CrontabXmlGenerator crontabXmlGenerator = new CrontabXmlGenerator(
                project,
                crontabXmlData
        );

        try {
            cronjobFileGenerator.generate(NewCronjobAction.ACTION_NAME, true);
            crontabXmlGenerator.generate(NewCronjobAction.ACTION_NAME);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    "Generation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private CronjobClassData getCronjobClassData(final String cronjobNamespace) {
        return new CronjobClassData(
            this.getCronjobClassName(),
            this.getCronjobDirectory(),
            cronjobNamespace,
            this.getCronjobModule()
        );
    }

    /**
     * Get new crontab xml data object.
     *
     * @param cronjobInstance Cron job instance
     *
     * @return CrontabXmlData
     */
    private CrontabXmlData getCrontabXmlData(final String cronjobInstance) {
        final String cronSchedule = this.isFixedScheduleType() ? this.getCronjobSchedule() : null;
        final String cronScheduleConfigPath = this.isConfigurableScheduleType()
                ? this.getCronjobScheduleConfigPath()
                : null;

        return new CrontabXmlData(
            this.getCronjobModule(),
            this.getCronjobGroup(),
            this.getCronjobName(),
            cronjobInstance,
            cronSchedule,
            cronScheduleConfigPath
        );
    }
}
