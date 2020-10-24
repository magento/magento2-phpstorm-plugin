/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.frameworks.PhpFrameworkConfigurable;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.project.validator.SettingsFormValidator;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.TooManyMethods"
})
public class SettingsForm implements PhpFrameworkConfigurable {
    private final Project project;
    private JCheckBox pluginEnabled;
    private JButton buttonReindex;
    private JPanel panel;
    private JButton regenerateUrnMapButton;
    private JTextField magentoVersion;
    private JTextField moduleDefaultLicenseName;
    private JCheckBox mftfSupportEnabled;
    private TextFieldWithBrowseButton magentoPath;
    private final SettingsFormValidator validator = new SettingsFormValidator(this);
    private JLabel magentoVersionLabel;//NOPMD
    private JLabel magentoPathLabel;//NOPMD

    public SettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Magento";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        buttonReindex.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(final MouseEvent event) {
                        reindex();
                        super.mouseClicked(event);
                    }
                }
        );

        regenerateUrnMapButton.addMouseListener(
                new RegenerateUrnMapListener(project)
        );

        refreshFormStatus(getSettings().pluginEnabled);
        pluginEnabled.addActionListener(e -> refreshFormStatus(pluginEnabled.isSelected()));

        moduleDefaultLicenseName.setText(getSettings().defaultLicense);
        mftfSupportEnabled.setSelected(getSettings().mftfSupportEnabled);
        magentoPath.getTextField().setText(getSettings().magentoPath);
        resolveMagentoVersion();

        addPathListener();
        addMagentoVersionListener();

        return (JComponent) panel;
    }

    private void refreshFormStatus(final boolean isEnabled) {
        buttonReindex.setEnabled(isEnabled);
        regenerateUrnMapButton.setEnabled(isEnabled);
        magentoVersion.setEnabled(isEnabled);
        mftfSupportEnabled.setEnabled(isEnabled);
        magentoPath.setEnabled(isEnabled);
        moduleDefaultLicenseName.setEnabled(isEnabled);
    }

    protected void reindex() {
        IndexManager.manualReindex();
        MagentoComponentManager.getInstance(project).flushModules();
    }

    @Override
    public boolean isModified() {
        final boolean licenseChanged = !moduleDefaultLicenseName.getText().equals(
                Settings.defaultLicense
        );
        final boolean versionChanged = !magentoVersion.getText().equals(
                getSettings().magentoVersion
        );
        final boolean statusChanged = !pluginEnabled.isSelected() == getSettings().pluginEnabled;
        final boolean mftfSupportChanged = mftfSupportEnabled.isSelected()
                != getSettings().mftfSupportEnabled;
        final boolean magentoPathChanged = isMagentoPathChanged();

        return statusChanged || licenseChanged || mftfSupportChanged
                || magentoPathChanged || versionChanged;
    }

    private void resolveMagentoVersion() {
        if (getSettings().magentoVersion == null) {
            this.updateMagentoVersion();
            return;
        }
        magentoVersion.setText(getSettings().magentoVersion);
    }

    private boolean isMagentoPathChanged() {
        return !magentoPath.getTextField().getText().equals(getSettings().magentoPath);
    }

    @Override
    public void apply() throws ConfigurationException {
        this.validator.validate();
        saveSettings();

        ConfigurationManager.getInstance().refreshIncludePaths(getSettings().getState(), project);

        if (buttonReindex.isEnabled()) {
            reindex();
        }
    }

    private void saveSettings() {
        getSettings().pluginEnabled = pluginEnabled.isSelected();
        getSettings().defaultLicense = moduleDefaultLicenseName.getText();
        getSettings().mftfSupportEnabled = mftfSupportEnabled.isSelected();
        getSettings().magentoPath = getMagentoPath();
        getSettings().magentoVersion = getMagentoVersion();
        buttonReindex.setEnabled(getSettings().pluginEnabled);
        regenerateUrnMapButton.setEnabled(getSettings().pluginEnabled);
    }

    @NotNull
    public String getMagentoVersion() {
        return magentoVersion.getText().trim();
    }

    @NotNull
    public String getMagentoPath() {
        return magentoPath.getTextField().getText().trim();
    }

    @Override
    public void reset() {
        pluginEnabled.setSelected(getSettings().pluginEnabled);
    }

    @Override
    public void disposeUIResources() {
        //do nothing
    }

    public Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void addPathListener() {
        final FileChooserDescriptor descriptor =
                FileChooserDescriptorFactory.createSingleFolderDescriptor();
        final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderListener
                = new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(
                "Magento Root Directory",
                "Choose Magento root directory",
                this.magentoPath,
                null,
                descriptor,
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
            ) {
                    @Nullable
                    @Override
                    protected VirtualFile getInitialFile() {
                        return super.getInitialFile();
                    }
                };
        this.magentoPath.addActionListener(browseFolderListener);
    }

    private void addMagentoVersionListener() {
        final DocumentListener onPathChange = new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent documentEvent) {
                updateMagentoVersion();
            }

            @Override
            public void removeUpdate(final DocumentEvent documentEvent) {
                updateMagentoVersion();
            }

            @Override
            public void changedUpdate(final DocumentEvent documentEvent) {
                updateMagentoVersion();
            }
        };
        this.magentoPath.getTextField().getDocument().addDocumentListener(onPathChange);
    }

    /**
     * Updates Magento version according to root composer.json.
     */
    public void updateMagentoVersion() {
        final String magentoPathValue = this.magentoPath.getTextField().getText();
        final String resolvedVersion = MagentoVersionUtil.get(project, magentoPathValue);
        magentoVersion.setText(resolvedVersion);
    }

    @Override
    public boolean isBeingUsed() {
        return this.pluginEnabled.isSelected();
    }

    @NotNull
    @Override
    public String getId() {
        return "Magento2.SettingsForm";
    }
}

