/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewCronGroupAction;
import com.magento.idea.magento2plugin.actions.generation.data.CronGroupXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleCronGroupXmlGenerator;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
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
import javax.swing.SpinnerNumberModel;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.TooManyMethods",
        "PMD.ExcessiveImports,"
})
public class NewCronGroupDialog extends AbstractDialog {
    private final String moduleName;
    private final Project project;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private static final String NAME = "name";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER,
            message = {IdentifierRule.MESSAGE, NAME})
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
        super();
        this.project = project;
        setContentPane(contentPanel);
        setModal(true);
        setTitle(NewCronGroupAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        buttonOK.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());
        addToggleListenersForCronGroupOptions();
        addDefaultValuesToCronGroupOptions();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
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
    @SuppressWarnings({"PMD.NullAssignment"})
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
        if (!validateFormFields()) {
            return;
        }

        generateFile();
        this.setVisible(false);
    }

    @Override
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
        scheduleGenerateEvery.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        scheduleAheadFor.setModel(new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
        scheduleLifetime.setModel(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
        historyCleanupEvery.setModel(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1));
        historySuccessLifetime.setModel(new SpinnerNumberModel(60, 1, Integer.MAX_VALUE, 1));
        historyFailureLifetime.setModel(new SpinnerNumberModel(600, 1, Integer.MAX_VALUE, 1));
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
        final String yesString = bundle.message("common.yes");

        return useSeparateProcess.getSelectedItem().toString().equals(yesString) ? 1 : 0;
    }

    private Boolean getAddUseSeparateProcess() {
        return addUseSeparateProcess.isSelected();
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        this.useSeparateProcess = new FilteredComboBox(getYesNoOptions());
    }

    private List<String> getYesNoOptions() {
        return new ArrayList<>(Arrays.asList(
                bundle.message("common.yes"),
                bundle.message("common.no")
        ));
    }
}
