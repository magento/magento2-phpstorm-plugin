/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.generation;

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
import com.magento.idea.magento2plugin.generation.validator.NewModuleFormValidator;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class NewModuleForm implements ListSelectionListener {
    private NewModuleFormValidator newModuleFormValidator;
    private JPanel contentPane;
    private JTextField packageName;
    private JLabel packageNameLabel;
    private JTextField moduleName;
    private JLabel moduleNameLabel;
    private JTextArea moduleDescription;
    private JLabel moduleDescriptionLabel;
    private JTextField moduleVersion;
    private JLabel moduleVersionLabel;
    private JList moduleLicense;
    private JLabel moduleLicenseLabel;
    private JTextField moduleLicenseCustom;
    private JScrollPane moduleLicenseScrollPanel;
    private JLabel magentoPathLabel;
    private TextFieldWithBrowseButton magentoPath;
    private List<ProjectGeneratorPeer.SettingsListener> myStateListeners;
    private CamelCaseToHyphen camelCaseToHyphen;

    public NewModuleForm(
    ) {
        this.camelCaseToHyphen = CamelCaseToHyphen.getInstance();
        this.newModuleFormValidator = NewModuleFormValidator.getInstance(this);
        this.myStateListeners = new ArrayList();
        Runnable listener = () -> {
            this.fireStateChanged();
        };
        addPathListener();
        addComponentChangesListener(listener);
        setLicenses();
    }

    private void addPathListener() {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderListener = new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(
                "Magento Root Directory",
                "Choose Magento root directory",
                this.magentoPath,
                null,
                descriptor,
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        ) {
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

    public MagentoProjectGeneratorSettings getSettings() {
        Settings.State state = new Settings.State();
        state.setPluginEnabled(true);
        state.setMftfSupportEnabled(true);
        state.setDefaultLicenseName(Settings.DEFAULT_LICENSE);
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

    public ValidationInfo validate() {
        String message = newModuleFormValidator.validate();
        if (message == null) {
            return null;
        }

        return new ValidationInfo(message, getContentPane());
    }

    public List getModuleLicenses() {
        List selectedLicenses = this.moduleLicense.getSelectedValuesList();
        Package.License customLicense = Package.License.CUSTOM;

        if (selectedLicenses.contains(customLicense.getLicenseName())) {
            selectedLicenses.remove(customLicense.getLicenseName());
            selectedLicenses.add(moduleLicenseCustom.getText());
        }

        return selectedLicenses;
    }

    public void addSettingsStateListener(ProjectGeneratorPeer.SettingsListener listener) {
        this.myStateListeners.add(listener);
    }

    public void addComponentChangesListener(final Runnable listener) {
        this.magentoPath.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(@NotNull DocumentEvent e) {
                listener.run();
            }
        });
        this.moduleName.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(@NotNull DocumentEvent e) {
                listener.run();
            }
        });
        this.packageName.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(@NotNull DocumentEvent e) {
                listener.run();
            }
        });
        this.moduleVersion.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(@NotNull DocumentEvent e) {
                listener.run();
            }
        });
    }

    private void fireStateChanged() {
        boolean validSettings = this.validate() == null;
        Iterator iterator = this.myStateListeners.iterator();

        while(iterator.hasNext()) {
            ProjectGeneratorPeer.SettingsListener listener = (ProjectGeneratorPeer.SettingsListener)iterator.next();
            listener.stateChanged(validSettings);
        }
    }

    private void setLicenses() {
        Package.License[] licenses = Package.License.values();
        Vector<String> licenseNames = new Vector<>(licenses.length);

        for (Package.License license: licenses) {
            licenseNames.add(license.getLicenseName());
        }

        moduleLicense.setListData(licenseNames);
        moduleLicense.setSelectedIndex(0);
        moduleLicense.addListSelectionListener(this);
    }

    private void handleModuleCustomLicenseInputVisibility () {
        boolean isCustomLicenseSelected = false;

        for (Object value: moduleLicense.getSelectedValuesList()) {
            if (Package.License.CUSTOM.getLicenseName().equals(value.toString())) {
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
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        handleModuleCustomLicenseInputVisibility();
    }
}
