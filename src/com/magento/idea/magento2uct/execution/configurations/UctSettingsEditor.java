/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.configurations;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2uct.execution.DownloadUctCommand;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.util.module.UctExecutableValidatorUtil;
import com.magento.idea.magento2uct.util.module.UctModuleLocatorUtil;
import com.magento.idea.magento2uct.versioning.IssueSeverityLevel;
import com.magento.idea.magento2uct.versioning.SupportedVersion;
import java.awt.Color;
import java.awt.Container;
import java.nio.file.Path;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import org.jdesktop.swingx.JXHyperlink;
import org.jetbrains.annotations.NotNull;

public class UctSettingsEditor extends SettingsEditor<UctRunConfiguration> {

    private final Project project;
    private String uctExecutablePath;

    private JPanel contentPanel;
    private LabeledComponent<TextFieldWithBrowseButton> myScriptName;
    private LabeledComponent<TextFieldWithBrowseButton> projectRoot;
    private JComboBox<ComboBoxItemData> comingVersion;
    private JComboBox<ComboBoxItemData> minIssueLevel;
    private JRadioButton hasIgnoreCurrentVersionIssues;
    private JPanel warningPanel;
    private JLabel myScriptNameLabel;//NOPMD
    private JLabel comingVersionLabel;//NOPMD
    private JLabel myScriptNameError;//NOPMD
    private JLabel comingVersionError;//NOPMD
    private JLabel minIssueLevelLabel;//NOPMD
    private JLabel minIssueLevelComment;//NOPMD
    private JLabel hasIgnoreCurrentVersionIssuesComment;//NOPMD
    private JLabel hasIgnoreCurrentVersionIssuesComment2;//NOPMD
    private JLabel projectRootLabel;//NOPMD
    private JLabel projectRootComment;//NOPMD
    private JLabel infoLabel;//NOPMD
    private JLabel infoLabel2;//NOPMD
    private JLabel uctLookupFailedWarning;//NOPMD
    private JXHyperlink installTypeOne;//NOPMD

    /**
     * Form constructor.
     *
     * @param project Project
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public UctSettingsEditor(final @NotNull Project project) {
        super();
        this.project = project;
        initializeComboboxSources();
        validateSettingsForm();
        infoLabel.setForeground(JBColor.blue);
        infoLabel2.setForeground(JBColor.blue);
        uctLookupFailedWarning.setForeground(JBColor.orange);
        uctLookupFailedWarning.setVisible(false);
        warningPanel.setVisible(false);
        installTypeOne.addActionListener(event -> downloadUctAction());
    }

    @Override
    protected void resetEditorFrom(final @NotNull UctRunConfiguration uctRunConfiguration) {
        lookupUctExecutablePath(uctRunConfiguration);

        if (uctExecutablePath != null) {
            myScriptName.getComponent().setText(uctExecutablePath);
            uctRunConfiguration.setScriptName(uctExecutablePath);
        }

        if (!uctRunConfiguration.getProjectRoot().isEmpty()) {
            projectRoot.getComponent().setText(uctRunConfiguration.getProjectRoot());
        } else {
            final String projectRootCandidate = project.getBasePath() != null
                    ? project.getBasePath()
                    : "";

            projectRoot.getComponent().setText(projectRootCandidate);
            uctRunConfiguration.setProjectRoot(projectRootCandidate);
        }

        if (!uctRunConfiguration.getComingVersion().isEmpty()) {
            final String storedComingVersion = uctRunConfiguration.getComingVersion();
            setSelectedValueByItsKey(comingVersion, storedComingVersion);
        }

        if (uctRunConfiguration.getMinIssueLevel() > 0) {
            final IssueSeverityLevel storedLevel =
                    IssueSeverityLevel.getByLevel(uctRunConfiguration.getMinIssueLevel());
            setSelectedValueByItsKey(minIssueLevel, String.valueOf(storedLevel.getLevel()));
        }

        hasIgnoreCurrentVersionIssues.setSelected(
                uctRunConfiguration.hasIgnoreCurrentVersionIssues()
        );
    }

    @Override
    protected void applyEditorTo(final @NotNull UctRunConfiguration uctRunConfiguration) {
        if (uctRunConfiguration.isNewlyCreated()) {
            uctRunConfiguration.setIsNewlyCreated(false);
        }
        uctRunConfiguration.setScriptName(myScriptName.getComponent().getText());
        uctRunConfiguration.setProjectRoot(projectRoot.getComponent().getText());

        final ComboBoxItemData selectedComingVersion =
                (ComboBoxItemData) comingVersion.getSelectedItem();

        if (selectedComingVersion != null) {
            uctRunConfiguration.setComingVersion(selectedComingVersion.getKey());
        } else {
            uctRunConfiguration.setComingVersion("");
        }

        final ComboBoxItemData selectedMinIssueLevel =
                (ComboBoxItemData) minIssueLevel.getSelectedItem();

        if (selectedMinIssueLevel != null) {
            int severityLevel = IssueSeverityLevel.getDefaultIssueSeverityLevel().getLevel();
            final String minIssueLevelValue = selectedMinIssueLevel.getKey();

            if (!minIssueLevelValue.isEmpty()) {
                severityLevel = Integer.parseInt(minIssueLevelValue);
            }
            uctRunConfiguration.setMinIssueLevel(severityLevel);
        }

        uctRunConfiguration.setHasIgnoreCurrentVersionIssues(
                hasIgnoreCurrentVersionIssues.isSelected()
        );
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return contentPanel;
    }

    /**
     * Download UCT action.
     */
    private void downloadUctAction() {
        ApplicationManager.getApplication().invokeAndWait(
                () -> {
                    final DownloadUctCommand command = new DownloadUctCommand(project);
                    command.execute();
                }
        );
        ApplicationManager.getApplication().invokeAndWait(
                () -> {
                    final Container parent = this.getComponent().getFocusCycleRootAncestor();

                    if (parent != null) {
                        parent.setVisible(false);
                    }
                    JOptionPane.showMessageDialog(
                            null,
                            "Open UCT Run Configuration after project updates indexes for newly "
                                    + "created files.\nThe UCT runnable will be "
                                    + "filled automatically.",
                            "The UCT is installed successfully",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
        );
    }

    /**
     * Try to find UCT inside the project.
     *
     * @param uctRunConfiguration UctRunConfiguration
     */
    private void lookupUctExecutablePath(
            final @NotNull UctRunConfiguration uctRunConfiguration
    ) {
        final String storedUctExecutable = getStoredUctExecutablePath(uctRunConfiguration);

        if (storedUctExecutable != null) {
            uctExecutablePath = storedUctExecutable;
            return;
        }
        final VirtualFile uctExecutableVf = UctModuleLocatorUtil.locateUctExecutable(project);

        if (uctExecutableVf == null) {
            warningPanel.setVisible(true);
            uctLookupFailedWarning.setVisible(true);
        } else {
            uctExecutablePath = uctExecutableVf.getPath();
        }
    }

    /**
     * Get stored UCT executable path (in settings).
     *
     * @param uctRunConfiguration UctRunConfiguration
     *
     * @return String
     */
    private String getStoredUctExecutablePath(
            final @NotNull UctRunConfiguration uctRunConfiguration
    ) {
        String uctExecutablePath;

        if (!uctRunConfiguration.getScriptName().isEmpty()) {
            uctExecutablePath = uctRunConfiguration.getScriptName();

            return uctExecutablePath;
        }
        if (!uctRunConfiguration.isNewlyCreated()) {
            return null;
        }
        final UctSettingsService settingsService = UctSettingsService.getInstance(project);

        if (settingsService == null) {
            return null;
        }
        uctExecutablePath = settingsService.getUctExecutablePath();

        if (!uctExecutablePath.isEmpty()) {
            if (UctExecutableValidatorUtil.validate(uctExecutablePath)) {
                return uctExecutablePath;
            } else {
                settingsService.setUctExecutablePath("");
            }
        }

        return null;
    }

    /**
     * Initialize combobox sources.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void initializeComboboxSources() {
        comingVersion.addItem(new ComboBoxItemData("", "Choose a target version"));

        for (final String version : SupportedVersion.getSupportedVersions()) {
            comingVersion.addItem(new ComboBoxItemData(version, version));
        }

        minIssueLevel.addItem(
                new ComboBoxItemData("", "Choose a minimum issue level to show in report")
        );

        for (final IssueSeverityLevel level : IssueSeverityLevel.getSeverityLabels()) {
            minIssueLevel.addItem(
                    new ComboBoxItemData(String.valueOf(level.getLevel()), level.getLabel())
            );
        }
    }

    /**
     * Validate settings form.
     */
    private void validateSettingsForm() {
        comingVersionError.setText("");
        myScriptNameError.setText("");
        myScriptNameError.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        myScriptNameError.setForeground(new Color(252, 119, 83));
        comingVersionError.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        comingVersionError.setForeground(new Color(252, 119, 83));

        validateExecutablePathField();
        validateComingVersionField((ComboBoxItemData) comingVersion.getSelectedItem());

        myScriptName
                .getComponent()
                .getTextField()
                .getDocument()
                .addDocumentListener(new DocumentAdapter() {
                    @SuppressWarnings("PMD.AccessorMethodGeneration")
                    @Override
                    protected void textChanged(@NotNull DocumentEvent event) {
                        validateExecutablePathField();
                    }
                });

        comingVersion.addItemListener(event -> {
            final ComboBoxItemData selectedItem = (ComboBoxItemData) event.getItem();

            validateComingVersionField(selectedItem);
        });
    }

    /**
     * Validate executable path field.
     */
    private void validateExecutablePathField() {
        final String executableScript = myScriptName.getComponent().getText();

        if (executableScript.isEmpty()) {
            myScriptNameError.setText("Please, specify UCT executable (bin/uct)");
        } else {
            uctLookupFailedWarning.setVisible(false);
            warningPanel.setVisible(false);
            myScriptNameError.setText("");
        }
    }

    /**
     * Validate coming version field.
     *
     * @param selectedItem ComboBoxItemData
     */
    private void validateComingVersionField(final ComboBoxItemData selectedItem) {
        if (selectedItem != null && selectedItem.getKey().isEmpty()) {
            comingVersionError.setText("Please, specify target version");
        } else {
            comingVersionError.setText("");
        }
    }

    /**
     * Set selected combobox item by key.
     *
     * @param comboBox JComboBox[ComboBoxItemData]
     * @param key String
     */
    private void setSelectedValueByItsKey(
            final JComboBox<ComboBoxItemData> comboBox,
            final String key
    ) {
        for (int index = 0; index < comboBox.getItemCount(); index++) {
            if (comboBox.getItemAt(index).getKey().equals(key)) {
                comboBox.setSelectedIndex(index);
            }
        }
    }

    private void createUIComponents() {
        myScriptName = new LabeledComponent<>();
        myScriptName.setComponent(new TextFieldWithBrowseButton());
        myScriptName
                .getComponent()
                .addBrowseFolderListener(
                        new TextBrowseFolderListener(
                                new FileChooserDescriptor(
                                        true,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false
                                )
                        )
                );
        projectRoot = new LabeledComponent<>();
        projectRoot.setComponent(new TextFieldWithBrowseButton());
        projectRoot
                .getComponent()
                .addBrowseFolderListener(
                        new TextBrowseFolderListener(
                                new FileChooserDescriptor(
                                        false,
                                        true,
                                        false,
                                        false,
                                        false,
                                        false
                                )
                        )
                );
    }
}
