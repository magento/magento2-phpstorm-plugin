/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewCronGroupAction;
import com.magento.idea.magento2plugin.actions.generation.data.CronGroupXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewCronGroupValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleCronGroupXmlGenerator;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class NewCronGroupDialog extends AbstractDialog {
    private final NewCronGroupValidator validator;
    private final String moduleName;
    private final Project project;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField cronGroupName;
    private JSpinner scheduleGenerateEvery;
    private JSpinner scheduleAheadFor;
    private JSpinner scheduleLifetime;
    private JSpinner historyCleanupEvery;
    private JSpinner historySuccessLifetime;
    private JSpinner historyFailureLifetime;
    private JCheckBox addScheduleGenerateEvery;
    private JCheckBox addScheduleAheadFor;
    private JCheckBox addScheduleLifetime;
    private JCheckBox addHistoryCleanupEvery;
    private JCheckBox addHistorySuccessLifetime;
    private JCheckBox addHistoryFailureLifetime;
    private FilteredComboBox useSeparateProcess;
    private JCheckBox addUseSeparateProcess;

    /**
     * New CRON group dialogue constructor.
     *
     * @param project Project
     * @param directory Directory
     */
    public NewCronGroupDialog(final Project project, final PsiDirectory directory) {
        this.project = project;
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.validator = NewCronGroupValidator.getInstance();
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);

        buttonOK.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());
        addToggleListenersForCronGroupOptions();
        addDefaultValuesToCronGroupOptions();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Get cron group XML data.
     *
     * @return CronGroupXmlData
     */
    public CronGroupXmlData getCronGroupXmlData() {
        return new CronGroupXmlData(
                getModuleName(),
                getCronGroupName(),
                getAddScheduleGenerateEvery() ? getScheduleGenerateEvery() : null,
                getAddScheduleAheadFor() ? getScheduleAheadFor() : null,
                getAddScheduleLifetime() ? getScheduleLifetime() : null,
                getAddHistoryCleanupEvery() ? getHistoryCleanupEvery() : null,
                getAddHistorySuccessLifetime() ? getHistorySuccessLifetime() : null,
                getAddHistoryFailureLifetime() ? getHistoryFailureLifetime() : null,
                getAddUseSeparateProcess() ? getUseSeparateProcess() : null
        );
    }

    /**
     * Open new CRON group dialogue.
     *
     * @param project Project
     * @param directory Directory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewCronGroupDialog dialog = new NewCronGroupDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (!validator.validate(this)) {
            return;
        }

        generateFile();
        this.setVisible(false);
    }

    protected void onCancel() {
        dispose();
    }

    private void generateFile() {
        final ModuleCronGroupXmlGenerator cronGroupXmlGenerator = new ModuleCronGroupXmlGenerator(
                this.getCronGroupXmlData(),
                project
        );
        cronGroupXmlGenerator.generate(NewCronGroupAction.ACTION_NAME, true);
    }

    private void addToggleListenersForCronGroupOptions() {
        addScheduleGenerateEvery.addActionListener(event -> onAddScheduleGenerateEveryChanges());
        addScheduleAheadFor.addActionListener(event -> onAddScheduleAheadForChanges());
        addScheduleLifetime.addActionListener(event -> onAddScheduleLifetimeChanges());
        addHistoryCleanupEvery.addActionListener(event -> onAddHistoryCleanupEveryChanges());
        addHistorySuccessLifetime.addActionListener(event -> onAddHistorySuccessLifetime());
        addHistoryFailureLifetime.addActionListener(event -> onAddHistoryFailureLifetime());
        addUseSeparateProcess.addActionListener(event -> onAddUseSeparateProcess());
    }

    private void addDefaultValuesToCronGroupOptions() {
        scheduleGenerateEvery.setValue(1);
        scheduleAheadFor.setValue(4);
        scheduleLifetime.setValue(2);
        historyCleanupEvery.setValue(10);
        historySuccessLifetime.setValue(60);
        historyFailureLifetime.setValue(600);
    }

    private void onAddScheduleGenerateEveryChanges() {
        scheduleGenerateEvery.setEnabled(getAddScheduleGenerateEvery());
    }

    private void onAddScheduleAheadForChanges() {
        scheduleAheadFor.setEnabled(getAddScheduleAheadFor());
    }

    private void onAddScheduleLifetimeChanges() {
        scheduleLifetime.setEnabled(getAddScheduleLifetime());
    }

    private void onAddHistoryCleanupEveryChanges() {
        historyCleanupEvery.setEnabled(getAddHistoryCleanupEvery());
    }

    private void onAddHistorySuccessLifetime() {
        historySuccessLifetime.setEnabled(getAddHistorySuccessLifetime());
    }

    private void onAddHistoryFailureLifetime() {
        historyFailureLifetime.setEnabled(getAddHistoryFailureLifetime());
    }

    private void onAddUseSeparateProcess() {
        useSeparateProcess.setEnabled(getAddUseSeparateProcess());
    }

    private String getModuleName() {
        return this.moduleName;
    }

    private String getCronGroupName() {
        return cronGroupName.getText().trim();
    }

    private int getScheduleGenerateEvery() {
        return (int) scheduleGenerateEvery.getValue();
    }

    private Boolean getAddScheduleGenerateEvery() {
        return addScheduleGenerateEvery.isSelected();
    }

    private int getScheduleAheadFor() {
        return (int) scheduleAheadFor.getValue();
    }

    private Boolean getAddScheduleAheadFor() {
        return addScheduleAheadFor.isSelected();
    }

    private int getScheduleLifetime() {
        return (int) scheduleLifetime.getValue();
    }

    private Boolean getAddScheduleLifetime() {
        return addScheduleLifetime.isSelected();
    }

    private int getHistoryCleanupEvery() {
        return (int) historyCleanupEvery.getValue();
    }

    private Boolean getAddHistoryCleanupEvery() {
        return addHistoryCleanupEvery.isSelected();
    }

    private int getHistorySuccessLifetime() {
        return (int) historySuccessLifetime.getValue();
    }

    private Boolean getAddHistorySuccessLifetime() {
        return addHistorySuccessLifetime.isSelected();
    }

    private int getHistoryFailureLifetime() {
        return (int) historyFailureLifetime.getValue();
    }

    private Boolean getAddHistoryFailureLifetime() {
        return addHistoryFailureLifetime.isSelected();
    }

    private int getUseSeparateProcess() {
        return useSeparateProcess.getSelectedItem().toString().equals("Yes") ? 1 : 0;
    }

    private Boolean getAddUseSeparateProcess() {
        return addUseSeparateProcess.isSelected();
    }

    private void createUIComponents() {
        this.useSeparateProcess = new FilteredComboBox(getYesNoOptions());
    }

    private List<String> getYesNoOptions() {
        return new ArrayList<>(Arrays.asList("Yes", "No"));
    }
}
