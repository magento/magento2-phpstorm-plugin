/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewBlockAction;
import com.magento.idea.magento2plugin.actions.generation.NewCronjobAction;
import com.magento.idea.magento2plugin.actions.generation.data.CronjobClassData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewCronjobValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.CronjobClassGenerator;
import com.magento.idea.magento2plugin.indexes.CronGroupIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class NewCronjobDialog extends AbstractDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField cronjobName;
    private JTextField cronjobDirectory;
    private JRadioButton fixedScheduleRadioButton;
    private JRadioButton configurableScheduleRadioButton;
    private JRadioButton everyMinuteRadioButton;
    private JRadioButton customScheduleRadioButton;
    private JTextField scheduleMask;
    private JRadioButton atMidnightRadioButton;
    private JPanel fixedSchedulePanel;
    private JTextField configPathField;
    private JPanel configurableSchedulePanel;
    private FilteredComboBox cronGroupComboBox;

    private Project project;
    private PsiDirectory baseDir;
    private String moduleName;
    private NewCronjobValidator validator;

    public NewCronjobDialog(Project project, PsiDirectory directory) {
        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = NewCronjobValidator.getInstance(this);

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
            scheduleMask.setEditable(false);
            scheduleMask.setText("* * * * *");
        });

        atMidnightRadioButton.addActionListener(e -> {
            scheduleMask.setEditable(false);
            scheduleMask.setText("0 0 * * *");
        });

        customScheduleRadioButton.addActionListener(e -> {
            scheduleMask.setText("* * * * *");
            scheduleMask.setEditable(true);
            scheduleMask.grabFocus();
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
        return this.cronjobName.getText().trim();
    }

    public String getCronjobDirectory() {
        return this.cronjobDirectory.getText().trim();
    }

    public String getCronjobModule() {
        return this.moduleName;
    }

    protected void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        List<String> cronGroups = CronGroupIndex.getInstance(project).getGroups();

        this.cronGroupComboBox = new FilteredComboBox(cronGroups);
    }

    /**
     * When new cronjob dialog is filled, validate the input data and generate a new crobjob
     */
    private void onOK() {
        if (!validator.validate()) {
            return;
        }

        CronjobClassData cronjobClassData = this.getCronjobClassData();
        this.generate(cronjobClassData);

        this.setVisible(false);
    }


    /**
     * Generate new cronjob file and register it in crontab.xml
     */
    private void generate(CronjobClassData cronjobClassData) {
        CronjobClassGenerator cronjobFileGenerator = new CronjobClassGenerator(project, cronjobClassData);

        cronjobFileGenerator.generate(NewCronjobAction.ACTION_NAME, true);
    }

    private CronjobClassData getCronjobClassData() {
        return new CronjobClassData(
            this.getCronjobClassName(),
            this.getCronjobDirectory(),
            this.getCronjobModule()
        );
    }
}
