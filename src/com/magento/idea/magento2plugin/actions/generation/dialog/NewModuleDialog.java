/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;//NOPMD

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewModuleAction;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleReadmeMdData;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleRegistrationPhpData;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.StartWithNumberOrCapitalLetterRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleComposerJsonGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleReadmeMdGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleRegistrationPhpGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleXmlGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.packages.Licenses;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.DataClass", "PMD.UnusedPrivateMethod"})
public class NewModuleDialog extends AbstractDialog implements ListSelectionListener { //NOPMD
    private static final String MODULE_DESCRIPTION = "module description";
    private static final String MODULE_VERSION = "module version";
    private static final String MODULE_NAME = "module name";
    private static final String PACKAGE_NAME = "package name";
    private static final String MAGENTO_BEFORE_DECLARATIVE_SCHEMA_VERSION = "2.2.11";
    private static final String DEFAULT_MODULE_PREFIX = "module";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, PACKAGE_NAME})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {StartWithNumberOrCapitalLetterRule.MESSAGE, PACKAGE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, PACKAGE_NAME})
    private JTextField packageName;

    /* TODO: module name !== package name */
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MODULE_NAME})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {StartWithNumberOrCapitalLetterRule.MESSAGE, MODULE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, MODULE_NAME})
    private JTextField moduleName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MODULE_DESCRIPTION})
    private JTextArea moduleDescription;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MODULE_VERSION})
    private JTextField moduleVersion;

    private JTextField moduleLicenseCustom;

    private JList moduleDependencies;
    private JList moduleLicense;

    private JScrollPane moduleLicenseScrollPanel;//NOPMD
    private JScrollPane moduleDependenciesScrollPanel;//NOPMD

    private JLabel moduleLicenseLabel;//NOPMD
    private JLabel moduleVersionLabel;//NOPMD
    private JLabel moduleDependenciesLabel;//NOPMD
    private JLabel moduleDescriptionLabel;//NOPMD
    private JLabel moduleNameLabel;//NOPMD
    private JLabel packageNameLabel;

    private JPanel contentPane;

    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox moduleReadmeMdCheckbox;

    @NotNull
    private final Project project;
    @NotNull
    private final PsiDirectory initialBaseDir;
    private String detectedPackageName;
    private final ModuleIndex moduleIndex;
    private final CamelCaseToHyphen camelCaseToHyphen;

    /**
     * Constructor.
     *
     * @param project Project
     * @param initialBaseDir PsiDirectory
     */
    public NewModuleDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory initialBaseDir
    ) {
        super();

        this.project = project;
        this.initialBaseDir = initialBaseDir;
        this.camelCaseToHyphen = CamelCaseToHyphen.getInstance();
        this.moduleIndex = new ModuleIndex(project);
        detectPackageName(initialBaseDir);
        setContentPane(contentPane);
        setModal(true);
        setTitle(NewModuleAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        setLicenses();
        setModuleDependencies();

        moduleLicenseCustom.setToolTipText("Custom License Name");
        moduleLicenseCustom.setText(Settings.getDefaultLicenseName(project));

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(new FocusOnAFieldListener(() -> {
            if (packageName.isVisible()) {
                packageName.requestFocusInWindow();
            } else {
                moduleName.requestFocusInWindow();
            }
        }));
    }

    private void detectPackageName(final @NotNull PsiDirectory initialBaseDir) {
        final VirtualFile initialBaseDirVf = initialBaseDir.getVirtualFile();

        if (MagentoBasePathUtil.isCustomVendorDirValid(initialBaseDirVf.getPath())) {
            packageName.setVisible(false);
            packageNameLabel.setVisible(false);
            this.detectedPackageName = initialBaseDir.getName();
            packageName.setText(this.detectedPackageName);
        }
    }

    protected void onOK() {
        if (validateFormFields()) {
            generateFiles();
        }
        exit();
    }

    private void generateFiles() {
        final PsiFile composerJson = generateComposerJson();
        if (composerJson == null) {
            return;
        }

        final PsiFile registrationPhp = generateRegistrationPhp();
        if (registrationPhp == null) {
            return;
        }

        if (isCreateModuleReadme()) {
            generateReadmeMd();
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
                getSetupVersion(),
                getBaseDir(),
                getModuleDependencies(),
                true
        ), project).generate(NewModuleAction.ACTION_NAME, true);
    }

    private void generateReadmeMd() {
        new ModuleReadmeMdGenerator(new ModuleReadmeMdData(
                getPackageName(),
                getModuleName(),
                getBaseDir()
        ), project).generate(NewModuleAction.ACTION_NAME);
    }

    private PsiDirectory getBaseDir() {
        if (detectedPackageName != null) {
            return this.initialBaseDir.getParent();
        }
        return this.initialBaseDir;
    }

    /**
     * Getter for Package Name.
     *
     * @return String
     */
    public String getPackageName() {
        if (detectedPackageName != null) {
            return detectedPackageName;
        }
        return this.packageName.getText().trim();
    }

    /**
     * Getter for Module Name.
     *
     * @return String
     */
    public String getModuleName() {
        return this.moduleName.getText().trim();
    }

    /**
     * Getter for Module Description.
     *
     * @return String
     */
    public String getModuleDescription() {
        return this.moduleDescription.getText().trim();
    }

    /**
     * get Module Version.
     *
     * @return string
     */
    public String getModuleVersion() {
        return this.moduleVersion.getText().trim();
    }

    /**
     * Get module version.
     *
     * @return string|null
     */
    public String getSetupVersion() {
        final String magentoVersion = getMagentoVersion();
        if (!MagentoVersionUtil.compare(
                magentoVersion, MAGENTO_BEFORE_DECLARATIVE_SCHEMA_VERSION)
        ) {
            return this.moduleVersion.getText().trim();
        }
        return null;
    }

    /**
     * Get magento version.
     *
     * @return string
     */
    public String getMagentoVersion() {
        return this.getSettings().magentoVersion;
    }

    /**
     * Getter for module license.
     *
     * @return String
     */
    public List getModuleLicense() {
        final List selectedLicenses = this.moduleLicense.getSelectedValuesList();
        final Licenses customLicense = Licenses.CUSTOM;

        if (selectedLicenses.contains(customLicense.getLicenseName())) {
            selectedLicenses.remove(customLicense.getLicenseName());
            selectedLicenses.add(moduleLicenseCustom.getText());
        }

        return selectedLicenses;
    }

    public List<String> getModuleDependencies() {
        return moduleDependencies.getSelectedValuesList();
    }

    /**
     * Getter for Module Readme Md Checkbox.
     *
     * @return Boolean
     */
    public Boolean isCreateModuleReadme() {
        return this.moduleReadmeMdCheckbox.isSelected();
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param initialBaseDir PsiDirectory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory initialBaseDir
    ) {
        final NewModuleDialog dialog = new NewModuleDialog(project, initialBaseDir);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    @NotNull
    private String getComposerPackageName() {
        return camelCaseToHyphen.convert(getPackageName())
                .concat("/")
                .concat(DEFAULT_MODULE_PREFIX + "-")
                .concat(camelCaseToHyphen.convert(getModuleName()));
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

    private void setModuleDependencies() {
        final List<String> moduleNames = moduleIndex.getModuleNames();
        Vector<String> licenseNames = new Vector<>(moduleNames.size() + 1);//NOPMD
        licenseNames.add(ComposerJson.NO_DEPENDENCY_LABEL);
        for (final String name : moduleNames) {
            licenseNames.add(name);
        }
        moduleDependencies.setListData(licenseNames);
        moduleDependencies.setSelectedIndex(0);
        moduleDependencies.addListSelectionListener(this);
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

    private void handleModuleSelectedDependencies() {
        // unselect the "None" dependency when others are selected
        int[] selectedDependencies = moduleDependencies.getSelectedIndices();
        if (moduleDependencies.getSelectedIndices().length > 1
                && moduleDependencies.isSelectedIndex(0)) {
            selectedDependencies = ArrayUtils.remove(selectedDependencies, 0);
            moduleDependencies.setSelectedIndices(selectedDependencies);
        }
    }

    @Override
    public void valueChanged(final ListSelectionEvent listSelectionEvent) {
        handleModuleCustomLicenseInputVisibility();
        handleModuleSelectedDependencies();
    }

    /**
     * Get settings.
     *
     * @return Settings
     */
    public Settings getSettings() {
        return Settings.getInstance(project);
    }

}
