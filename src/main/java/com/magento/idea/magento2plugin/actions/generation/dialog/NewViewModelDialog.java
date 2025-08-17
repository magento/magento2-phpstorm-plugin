/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewViewModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.ViewModelFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.StartWithNumberOrCapitalLetterRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleViewModelClassGenerator;
import com.magento.idea.magento2plugin.magento.files.ViewModelPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jetbrains.annotations.Nullable;

public class NewViewModelDialog extends AbstractDialog {

    private static final String VIEW_MODEL_NAME = "View Model Name";
    private static final String VIEW_MODEL_DIR = "View Model Directory";

    private final Project project;
    private final PsiDirectory baseDir;
    private final String moduleName;

    private JPanel contentPanel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, VIEW_MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, VIEW_MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, VIEW_MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {StartWithNumberOrCapitalLetterRule.MESSAGE, VIEW_MODEL_NAME})
    private JTextField viewModelName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, VIEW_MODEL_DIR})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, VIEW_MODEL_DIR})
    private JTextField viewModelParentDir;

    private JLabel viewModelNameErrorMessage;//NOPMD
    private JLabel viewModelParentDirErrorMessage;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewViewModelDialog(final Project project, final PsiDirectory directory) {
        super(project);

        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setTitle(NewViewModelAction.ACTION_DESCRIPTION);
        suggestViewModelDirectory();

        // DialogWrapper handles button actions and ESC key automatically

        init();
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewViewModelDialog dialog = new NewViewModelDialog(project, directory);
        dialog.centerDialog(dialog);
        dialog.showDialog();
    }

    /**
     * Create center panel.
     *
     * @return JComponent
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    protected void onWriteActionOK() {
        generateFile();
        exit();
    }

    private void generateFile() {
        new ModuleViewModelClassGenerator(new ViewModelFileData(
                getViewModelDirectory(),
                getViewModelName(),
                getModuleName(),
                getNamespace()
        ), project).generate(NewViewModelAction.ACTION_NAME, true);
    }

    private String getModuleName() {
        return moduleName;
    }

    public String getViewModelName() {
        return viewModelName.getText().trim();
    }

    public String getViewModelDirectory() {
        return viewModelParentDir.getText().trim();
    }

    private void suggestViewModelDirectory() {
        final String moduleIdentifierPath = getModuleIdentifierPath();
        if (moduleIdentifierPath == null) {
            viewModelParentDir.setText(ViewModelPhp.DEFAULT_DIR);
            return;
        }

        final String path = baseDir.getVirtualFile().getPath();
        final String[] pathParts = path.split(moduleIdentifierPath);
        final int partsMaxLength = 2;
        if (pathParts.length != partsMaxLength) {
            viewModelParentDir.setText(ViewModelPhp.DEFAULT_DIR);
            return;
        }

        if (pathParts[1] != null) {
            viewModelParentDir.setText(pathParts[1].substring(1));
            return;
        }
        viewModelParentDir.setText(ViewModelPhp.DEFAULT_DIR);
    }

    private String getModuleIdentifierPath() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        return parts[0] + File.separator + parts[1];
    }

    private String getNamespace() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        final String directoryPart = getViewModelDirectory().replace(
                File.separator,
                Package.fqnSeparator
        );
        return parts[0] + Package.fqnSeparator + parts[1] + Package.fqnSeparator + directoryPart;
    }
}
