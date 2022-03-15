/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.AbstractDialog;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import com.magento.idea.magento2uct.actions.ConfigureUctAction;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.util.module.UctModulePathValidatorUtil;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports"})
public class ConfigurationDialog extends AbstractDialog {

    private final Project project;
    private final UctSettingsService settingsService;

    private JCheckBox enable;
    private LabeledComponent<TextFieldWithBrowseButton> modulePath;
    private LabeledComponent<TextFieldWithBrowseButton> additionalPath;
    private JCheckBox ignoreCurrentVersion;
    private JCheckBox hasAdditionalPath;//NOPMD
    private JComboBox<ComboBoxItemData> currentVersion;
    private JComboBox<ComboBoxItemData> targetVersion;
    private JComboBox<ComboBoxItemData> issueSeverityLevel;

    private JPanel contentPanel;
    private JButton buttonCancel;
    private JButton buttonOk;
    private JLabel currentVersionLabel;//NOPMD
    private JLabel modulePathLabel;//NOPMD
    private JLabel targetVersionLabel;//NOPMD
    private JLabel issueSeverityLevelLabel;//NOPMD
    private JLabel modulePathError;//NOPMD
    private JLabel enableComment;//NOPMD
    private JLabel enableCommentPath;//NOPMD
    private JLabel additionalPathLabel;//NOPMD
    private JLabel additionalPathError;//NOPMD

    /**
     * Configuration dialog.
     *
     * @param project Project
     */
    public ConfigurationDialog(final @NotNull Project project) {
        super();

        this.project = project;
        settingsService = UctSettingsService.getInstance(project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle(ConfigureUctAction.ACTION_NAME);
        getRootPane().setDefaultButton(buttonOk);

        hasAdditionalPath.addActionListener(event ->
                refreshAdditionalFields(hasAdditionalPath.isSelected()));
        buttonOk.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());

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

        modulePathError.setText("");
        modulePathError.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        modulePathError.setForeground(new Color(252, 119, 83));
        additionalPathError.setText("");
        additionalPathError.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        additionalPathError.setForeground(new Color(252, 119, 83));
        enableComment.setForeground(JBColor.blue);
        enableCommentPath.setForeground(JBColor.blue);
        setDefaultValues();
        refreshAdditionalFields(hasAdditionalPath.isSelected());
    }

    /**
     * Open configuration dialog window.
     *
     * @param project Project
     */
    public static void open(final @NotNull Project project) {
        final ConfigurationDialog dialog = new ConfigurationDialog(project);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Save configuration.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void onOK() {
        modulePathError.setText("");
        additionalPathError.setText("");

        if (modulePath.getComponent().getText().isEmpty()
                || !UctModulePathValidatorUtil.validate(modulePath.getComponent().getText())) {
            modulePathError.setText("The `Path To Analyse` field is empty or invalid");
            return;
        }
        if (hasAdditionalPath.isSelected() && additionalPath.getComponent().getText().isEmpty()
                || hasAdditionalPath.isSelected()
                && !UctModulePathValidatorUtil.validate(additionalPath.getComponent().getText())) {
            additionalPathError.setText("The `Path To Analyse` field is empty or invalid");
            return;
        }
        settingsService.setEnabled(enable.isSelected());

        final ComboBoxItemData currentVersionItemData =
                (ComboBoxItemData) currentVersion.getSelectedItem();
        String currentVersionValue = "";

        if (currentVersionItemData != null) {
            currentVersionValue = currentVersionItemData.getKey();
        }

        settingsService.setCurrentVersion(
                currentVersionValue.isEmpty()
                ? null//NOPMD
                : SupportedVersion.getVersion(currentVersionValue)
        );
        final SupportedVersion targetVersionValue = SupportedVersion.getVersion(
                targetVersion.getSelectedItem().toString()
        );

        if (targetVersionValue != null) {
            settingsService.setTargetVersion(targetVersionValue);
        }
        settingsService.setModulePath(modulePath.getComponent().getText());
        settingsService.setMinIssueSeverityLevel(
                Integer.parseInt(
                        ((ComboBoxItemData) issueSeverityLevel.getSelectedItem()).getKey()
                )
        );
        settingsService.setIgnoreCurrentVersion(ignoreCurrentVersion.isSelected());
        settingsService.setHasAdditionalPath(hasAdditionalPath.isSelected());
        settingsService.setAdditionalPath(additionalPath.getComponent().getText());
        exit();
    }

    /**
     * Set default values for inputs.
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity"})
    private void setDefaultValues() {
        enable.setSelected(settingsService.isEnabled());

        if (settingsService.getModulePath() == null) {
            final String basePath = Settings.getMagentoPath(project);

            if (basePath != null) {
                modulePath.getComponent().setText(basePath);
            }
        } else {
            modulePath.getComponent().setText(settingsService.getModulePath());
        }

        if (settingsService.getCurrentVersion() == null) {
            setSelectedValueByItsKey(currentVersion, null);
            final String basePath = Settings.getMagentoPath(project);

            if (basePath != null) {
                final String resolvedVersion = MagentoVersionUtil.get(project, basePath);

                if (resolvedVersion != null
                        && !MagentoVersionUtil.DEFAULT_VERSION.equals(resolvedVersion)) {
                    final SupportedVersion versionCandidate = SupportedVersion.getVersion(
                            resolvedVersion
                    );
                    setSelectedValueByItsKey(
                            currentVersion,
                            versionCandidate == null ? null : versionCandidate.getVersion()
                    );
                }
            }
        } else {
            setSelectedValueByItsKey(
                    currentVersion,
                    settingsService.getCurrentVersion().getVersion()
            );
        }

        if (settingsService.getTargetVersion() != null) {
            setSelectedValueByItsKey(
                    targetVersion,
                    settingsService.getTargetVersion().getVersion()
            );
        }

        if (settingsService.getMinIssueLevel() == null) {
            setSelectedValueByItsKey(
                    issueSeverityLevel,
                    String.valueOf(IssueSeverityLevel.WARNING.getLevel())
            );
        } else {
            setSelectedValueByItsKey(
                    issueSeverityLevel,
                    String.valueOf(settingsService.getMinIssueLevel().getLevel())
            );
        }
        final Boolean shouldIgnore = settingsService.shouldIgnoreCurrentVersion();
        ignoreCurrentVersion.setSelected(Objects.requireNonNullElse(shouldIgnore, false));

        final Boolean isShowAdditionalPath = settingsService.getHasAdditionalPath();
        hasAdditionalPath.setSelected(
                Objects.requireNonNullElse(isShowAdditionalPath, false)
        );

        if (settingsService.getAdditionalPath() != null) {
            additionalPath.getComponent().setText(settingsService.getAdditionalPath());
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

    /**
     * Create custom components and fill their entries.
     */
    @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidInstantiatingObjectsInLoops"})
    private void createUIComponents() {
        targetVersion = new ComboBox<>();

        for (final String version : SupportedVersion.getSupportedVersions()) {
            targetVersion.addItem(new ComboBoxItemData(version, version));
        }
        currentVersion = new ComboBox<>();
        currentVersion.addItem(new ComboBoxItemData("", "Less than 2.3.0"));

        for (final String version : SupportedVersion.getSupportedVersions()) {
            currentVersion.addItem(new ComboBoxItemData(version, version));
        }
        issueSeverityLevel = new ComboBox<>();

        for (final IssueSeverityLevel level : IssueSeverityLevel.getSeverityLabels()) {
            issueSeverityLevel.addItem(
                    new ComboBoxItemData(String.valueOf(level.getLevel()), level.getLabel())
            );
        }

        modulePath = new LabeledComponent<>();
        modulePath.setComponent(new TextFieldWithBrowseButton());
        modulePath.getComponent().addBrowseFolderListener(
                new TextBrowseFolderListener(
                        new FileChooserDescriptor(false, true, false, false, false, false)
                )
        );

        additionalPath = new LabeledComponent<>();
        additionalPath.setComponent(new TextFieldWithBrowseButton());
        additionalPath.getComponent().addBrowseFolderListener(
                new TextBrowseFolderListener(
                        new FileChooserDescriptor(false, true, false, false, false, false)
                )
        );
    }

    private void refreshAdditionalFields(final boolean isEnabled) {
        additionalPath.setEnabled(isEnabled);
        additionalPath.setVisible(isEnabled);
        additionalPathLabel.setVisible(isEnabled);
        additionalPathError.setVisible(isEnabled);
    }
}
