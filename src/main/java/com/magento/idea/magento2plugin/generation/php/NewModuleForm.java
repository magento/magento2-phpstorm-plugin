/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.generation.php;//NOPMD

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.ui.DocumentAdapter;
import com.magento.idea.magento2plugin.generation.php.validator.NewModuleFormValidator;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Licenses;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.UnusedPrivateMethod"})
public class NewModuleForm implements ListSelectionListener {
    private final NewModuleFormValidator newModuleFormValidator;
    private JPanel contentPane;
    private JTextField packageName;
    private JTextField moduleName;
    private final CamelCaseToHyphen camelCaseToHyphen;
    private JTextArea moduleDescription;
    private final List<ProjectGeneratorPeer.SettingsListener> myStateListeners;
    private JTextField moduleVersion;
    private TextFieldWithBrowseButton magentoPath;
    private JList moduleLicense;
    private JTextField moduleLicenseCustom;
    private JLabel moduleLicenseLabel;//NOPMD
    private JLabel moduleVersionLabel;//NOPMD
    private JScrollPane moduleLicenseScrollPanel;//NOPMD
    private JLabel magentoPathLabel;//NOPMD
    private JLabel moduleDescriptionLabel;//NOPMD
    private JLabel moduleNameLabel;//NOPMD
    private JLabel packageNameLabel;//NOPMD

    /**
     * Constructor.
     */
    public NewModuleForm() {
        this.camelCaseToHyphen = CamelCaseToHyphen.getInstance();
        this.newModuleFormValidator = NewModuleFormValidator.getInstance(this);
        this.myStateListeners = new ArrayList();
        final Runnable listener = () -> {
            this.fireStateChanged();
        };
        addPathListener();
        addComponentChangesListener(listener);
        setLicenses();
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
                    @Override
                    protected VirtualFile getInitialFile() {
                        String directoryName = this.getComponentText();
                        if (!StringUtil.isEmptyOrSpaces(directoryName)) {
                            String lastSavedPath = Settings.getLastMagentoPath();
                            if (!StringUtil.isEmptyOrSpaces(lastSavedPath)) {
                                lastSavedPath = FileUtil.toSystemIndependentName(lastSavedPath);
                                return LocalFileSystem.getInstance().findFileByPath(lastSavedPath);
                            }
                        }

                        return super.getInitialFile();
                    }
                };
        this.magentoPath.addActionListener(browseFolderListener);
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    /**
     * Returns project generation settings.
     *
     * @return MagentoProjectGeneratorSettings
     */
    public MagentoProjectGeneratorSettings getSettings() {
        final Settings.State state = new Settings.State();
        state.setPluginEnabled(true);
        state.setMftfSupportEnabled(true);
        state.setDefaultLicenseName(Settings.defaultLicense);
        state.setMagentoPathAndUpdateLastUsed(this.magentoPath.getTextField().getText().trim());

        return new MagentoProjectGeneratorSettings(
                state,
                getPackageName(),
                getModuleName(),
                getModuleDescription(),
                getComposerPackageName(),
                getModuleVersion(),
                getModuleLicenses()
        );
    }

    public String getPackageName() {
        return this.packageName.getText().trim();
    }

    public String getModuleName() {
        return this.moduleName.getText().trim();
    }

    public String getModuleDescription() {
        return this.moduleDescription.getText().trim();
    }

    public String getModuleVersion() {
        return this.moduleVersion.getText().trim();
    }

    public String getMagentoPath() {
        return this.magentoPath.getTextField().getText().trim();
    }

    /**
     * Validate settings.
     *
     * @return ValidationInfo
     */
    public ValidationInfo validate() {
        final String message = newModuleFormValidator.validate();
        if (message == null) {
            return null;
        }

        return new ValidationInfo(message, getContentPane());
    }

    /**
     * Get Licenses.
     *
     * @return List
     */
    public List getModuleLicenses() {
        final List selectedLicenses = this.moduleLicense.getSelectedValuesList();
        final Licenses customLicense = Licenses.CUSTOM;

        if (selectedLicenses.contains(customLicense.getLicenseName())) {
            selectedLicenses.remove(customLicense.getLicenseName());
            selectedLicenses.add(moduleLicenseCustom.getText());
        }

        return selectedLicenses;
    }

    public void addSettingsStateListener(final ProjectGeneratorPeer.SettingsListener listener) {
        this.myStateListeners.add(listener);
    }

    private void addComponentChangesListener(final Runnable listener) {
        this.magentoPath.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                listener.run();
            }
        });
        this.moduleName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                listener.run();
            }
        });
        this.packageName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                listener.run();
            }
        });
        this.moduleVersion.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                listener.run();
            }
        });
    }

    private void fireStateChanged() {
        final boolean validSettings = this.validate() == null;
        final Iterator iterator = this.myStateListeners.iterator();

        while (iterator.hasNext()) {
            final ProjectGeneratorPeer.SettingsListener listener =
                    (ProjectGeneratorPeer.SettingsListener)iterator.next();
            listener.stateChanged(validSettings);
        }
    }

    private void setLicenses() {
        final Licenses[] licenses = Licenses.values();
        Vector<String> licenseNames = new Vector<>(licenses.length);//NOPMD

        for (final Licenses license: licenses) {
            licenseNames.add(license.getLicenseName());
        }

        moduleLicense.setListData(licenseNames);
        moduleLicense.setSelectedIndex(0);
        moduleLicense.addListSelectionListener(this);
    }

    private void handleModuleCustomLicenseInputVisibility() {
        boolean isCustomLicenseSelected = false;

        for (final Object value: moduleLicense.getSelectedValuesList()) {
            if (Licenses.CUSTOM.getLicenseName().equals(value.toString())) {
                isCustomLicenseSelected = true;

                break;
            }
        }

        moduleLicenseCustom.setEnabled(isCustomLicenseSelected);
        moduleLicenseCustom.setEditable(isCustomLicenseSelected);
    }

    @NotNull
    private String getComposerPackageName() {
        return camelCaseToHyphen.convert(getPackageName())
                .concat(File.separator)
                .concat(camelCaseToHyphen.convert(getModuleName()));
    }

    private void createUIComponents() {
        this.magentoPath = new TextFieldWithBrowseButton();
    }

    @Override
    public void valueChanged(final ListSelectionEvent listSelectionEvent) {
        handleModuleCustomLicenseInputVisibility();
    }
}
