/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.ide.IdeView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.actions.generation.NewModuleAction;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleRegistrationPhpData;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewModuleDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleComposerJsonGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleRegistrationPhpGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.util.NavigateToCreatedFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class NewModuleDialog extends AbstractDialog implements ListSelectionListener {
    @NotNull
    private final Project project;
    @NotNull
    private final PsiDirectory initialBaseDir;
    @Nullable
    private final PsiFile file;
    @Nullable
    private final IdeView view;
    @Nullable
    private final Editor editor;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final NewModuleDialogValidator validator;
    private final CamelCaseToHyphen camelCaseToHyphen;
    private final NavigateToCreatedFile navigateToCreatedFile;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
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
    private JList moduleDependencies;
    private JLabel moduleDependenciesLabel;
    private JScrollPane moduleDependenciesScrollPanel;
    private String detectedPackageName;
    private ModuleIndex moduleIndex;

    public NewModuleDialog(
            @NotNull Project project,
            @NotNull PsiDirectory initialBaseDir,
            @Nullable PsiFile file,
            @Nullable IdeView view,
            @Nullable Editor editor
    ) {
        this.project = project;
        this.initialBaseDir = initialBaseDir;
        this.file = file;
        this.view = view;
        this.editor = editor;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.camelCaseToHyphen = CamelCaseToHyphen.getInstance();
        this.validator = NewModuleDialogValidator.getInstance(this);
        this.navigateToCreatedFile = NavigateToCreatedFile.getInstance();
        this.moduleIndex = ModuleIndex.getInstance(project);
        detectPackageName(initialBaseDir);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();
        setLicenses();
        setModuleDependencies();

        moduleLicenseCustom.setToolTipText("Custom License Name");
        moduleLicenseCustom.setText(Settings.getDefaultLicenseName(project));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void detectPackageName(@NotNull PsiDirectory initialBaseDir) {
        PsiDirectory parentDir = initialBaseDir.getParent();
        if (parentDir != null && parentDir.toString().endsWith(Package.PACKAGES_ROOT)) {
            packageName.setVisible(false);
            packageNameLabel.setVisible(false);
            this.detectedPackageName = initialBaseDir.getName();
        }
    }

    private void onOK() {
        if (!validator.validate()) {
            return;
        }
        generateFiles();
        this.setVisible(false);
    }

    private void generateFiles() {
        PsiFile composerJson = generateComposerJson();
        if (composerJson == null) {
            return;
        }

        PsiFile registrationPhp = generateRegistrationPhp();
        if (registrationPhp == null) {
            return;
        }
        generateModuleXml();
    }

    private PsiFile generateComposerJson() {
        return new ModuleComposerJsonGenerator(new ModuleComposerJsonData(
                getPackageName(),
                getModuleName(),
                getBaseDir(),
                getModuleDescription(),
                getComposerPackageName(),
                getModuleVersion(),
                getModuleLicense(),
                getModuleDependencies(),
                true
        ), project).generate(NewModuleAction.ACTION_NAME);
    }

    private PsiFile generateRegistrationPhp() {
        return new ModuleRegistrationPhpGenerator(new ModuleRegistrationPhpData(
                    getPackageName(),
                    getModuleName(),
                    getBaseDir(),
                    true
            ), project).generate(NewModuleAction.ACTION_NAME);
    }

    private void generateModuleXml() {
        new ModuleXmlGenerator(new ModuleXmlData(
                getPackageName(),
                getModuleName(),
                getBaseDir(),
                true
        ), project).generate(NewModuleAction.ACTION_NAME, true);
    }

    private PsiDirectory getBaseDir() {
        return detectedPackageName != null ? this.initialBaseDir.getParent() : this.initialBaseDir;
    }

    public String getPackageName() {
        if (detectedPackageName != null) {
            return detectedPackageName;
        }
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

    public List getModuleLicense() {
        List selectedLicenses = this.moduleLicense.getSelectedValuesList();
        Package.License customLicense = Package.License.CUSTOM;

        if (selectedLicenses.contains(customLicense.getLicenseName())) {
            selectedLicenses.remove(customLicense.getLicenseName());
            selectedLicenses.add(moduleLicenseCustom.getText());
        }

        return selectedLicenses;
    }

    public List<String> getModuleDependencies() {
        return moduleDependencies.getSelectedValuesList();
    }

    public static void open(@NotNull Project project, @NotNull PsiDirectory initialBaseDir, @Nullable PsiFile file, @Nullable IdeView view, @Nullable Editor editor) {
        NewModuleDialog dialog = new NewModuleDialog(project, initialBaseDir, file, view, editor);
        dialog.pack();
        dialog.setVisible(true);
    }

    @NotNull
    private String getComposerPackageName() {
        return camelCaseToHyphen.convert(getPackageName())
                .concat("/")
                .concat(camelCaseToHyphen.convert(getModuleName()));
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

    private void setModuleDependencies() {
        List<String> moduleNames = moduleIndex.getModuleNames();
        Vector<String> licenseNames = new Vector<>(moduleNames.size());
        for (String name : moduleNames) {
            licenseNames.add(name);
        }
        moduleDependencies.setListData(licenseNames);
        moduleDependencies.setSelectedIndex(0);
        moduleDependencies.addListSelectionListener(this);
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

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        handleModuleCustomLicenseInputVisibility();
    }
}
