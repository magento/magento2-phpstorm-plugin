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
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewCronjobValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.CronjobClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.CrontabXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.indexes.CronGroupIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class NewCronjobDialog extends AbstractDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField cronjobClassNameField;
    private JTextField cronjobDirectoryField;
    private JRadioButton fixedScheduleRadioButton;
    private JRadioButton configurableScheduleRadioButton;
    private JRadioButton everyMinuteRadioButton;
    private JRadioButton customScheduleRadioButton;
    private JTextField cronjobScheduleField;
    private JRadioButton atMidnightRadioButton;
    private JPanel fixedSchedulePanel;
    private JTextField configPathField;
    private JPanel configurableSchedulePanel;
    private FilteredComboBox cronGroupComboBox;
    private JTextField cronjobNameField;

    private Project project;
    private String moduleName;
    private NewCronjobValidator validator;
    private CamelCaseToSnakeCase camelCaseToSnakeCase;

    public NewCronjobDialog(Project project, PsiDirectory directory) {
        this.project = project;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = NewCronjobValidator.getInstance();
        this.camelCaseToSnakeCase = CamelCaseToSnakeCase.getInstance();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Create a new Magento 2 cronjob..");
        pushToMiddle();

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        fixedScheduleRadioButton.addActionListener(e -> {
            configurableSchedulePanel.setVisible(false);
            fixedSchedulePanel.setVisible(true);
        });

        configurableScheduleRadioButton.addActionListener(e -> {
            fixedSchedulePanel.setVisible(false);
            configurableSchedulePanel.setVisible(true);
            configPathField.grabFocus();
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
        cronjobNameField.setText(this.suggestCronjobName(this.moduleName, "CleanTableCronjob"));
        cronjobClassNameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                cronjobNameField.setText(suggestCronjobName(moduleName, cronjobClassNameField.getText()));
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void open(Project project, PsiDirectory directory) {
        NewCronjobDialog dialog = new NewCronjobDialog(project, directory);

        dialog.pack();
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

    protected void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        List<String> cronGroups = CronGroupIndex.getInstance(project).getGroups();

        this.cronGroupComboBox = new FilteredComboBox(cronGroups);
    }

    /**
     * Retrieve the default cronjob name
     *
     * @param moduleName
     *
     * @return String
     */
    private String suggestCronjobName(String moduleName, String cronjobClassname) {
        if (cronjobClassname == null || cronjobClassname.isEmpty()) {
            return this.moduleName.toLowerCase();
        }

        String cronjobClassnameToSnakeCase = this.camelCaseToSnakeCase.convert(cronjobClassname);

        return this.moduleName.toLowerCase() + "_" + cronjobClassnameToSnakeCase;
    }

    /**
     * When new cronjob dialog is filled, validate the input data and generate a new crobjob
     */
    private void onOK() {
        if (!validator.validate(this.project,this)) {
            return;
        }

        NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
            this.getCronjobModule(),
            this.getCronjobClassName(),
            this.getCronjobDirectory()
        );

        String cronjobNamespace = namespaceBuilder.getCronjobNamespace();
        String cronjobInstance = namespaceBuilder.getClassFqn();

        CronjobClassData cronjobClassData = this.getCronjobClassData(cronjobNamespace);
        CrontabXmlData crontabXmlData = this.getCrontabXmlData(cronjobInstance);

        // todo: catch validation exceptions
        this.generate(cronjobClassData, crontabXmlData);
        this.setVisible(false);
    }

    /**
     * Generate new cronjob file and register it in crontab.xml
     */
    private void generate(CronjobClassData cronjobClassData, CrontabXmlData crontabXmlData) {
        CronjobClassGenerator cronjobFileGenerator = new CronjobClassGenerator(project, cronjobClassData);
        CrontabXmlGenerator crontabXmlGenerator = new CrontabXmlGenerator(project, crontabXmlData);

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

    private CronjobClassData getCronjobClassData(String cronjobNamespace) {
        return new CronjobClassData(
            this.getCronjobClassName(),
            this.getCronjobDirectory(),
            cronjobNamespace,
            this.getCronjobModule()
        );
    }

    /**
     *
     * @param cronjobInstance
     *
     * @return CrontabXmlData
     */
    private CrontabXmlData getCrontabXmlData(String cronjobInstance) {
        String cronSchedule = this.isFixedScheduleType() ? this.getCronjobSchedule() : null;
        String cronScheduleConfigPath = this.isConfigurableScheduleType()
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
