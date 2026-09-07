/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewBlockAction;
import com.magento.idea.magento2plugin.actions.generation.data.BlockFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpDirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleBlockClassGenerator;
import com.magento.idea.magento2plugin.magento.files.BlockPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import org.jetbrains.annotations.Nullable;

public class NewBlockDialog extends AbstractDialog {

    private final PsiDirectory baseDir;
    private final String moduleName;
    private JPanel contentPanel;
    private final Project project;
    private JTextPane warning;//NOPMD
    private JRadioButton adminhtmlRadioButton;//NOPMD
    private static final String NAME = "name";
    private static final String DIRECTORY = "directory";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS, message = {PhpClassRule.MESSAGE, NAME})
    private JTextField blockName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, DIRECTORY})
    @FieldValidation(rule = RuleRegistry.PHP_DIRECTORY,
            message = {PhpDirectoryRule.MESSAGE, DIRECTORY})
    private JTextField blockParentDir;

    private JLabel blockNameErrorMessage;//NOPMD
    private JLabel blockParentDirErrorMessage;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewBlockDialog(final Project project, final PsiDirectory directory) {
        super(project);

        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setTitle(NewBlockAction.ACTION_DESCRIPTION);
        suggestBlockDirectory();

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
        final NewBlockDialog dialog = new NewBlockDialog(project, directory);
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
        new ModuleBlockClassGenerator(new BlockFileData(
                getBlockDirectory(),
                getBlockName(),
                getModuleName(),
                getNamespace()
        ), project).generate(NewBlockAction.ACTION_NAME, true);
    }

    private String getModuleName() {
        return moduleName;
    }

    public String getBlockName() {
        return blockName.getText().trim();
    }

    public String getBlockDirectory() {
        return blockParentDir.getText().trim();
    }

    private void suggestBlockDirectory() {
        final String moduleIdentifierPath = getModuleIdentifierPath();

        if (moduleIdentifierPath == null) {
            blockParentDir.setText(BlockPhp.DEFAULT_DIR);
            return;
        }
        final String path = baseDir.getVirtualFile().getPath();
        final String[] pathParts = path.split(moduleIdentifierPath);
        final int minimumPathParts = 2;

        if (pathParts.length != minimumPathParts) {
            blockParentDir.setText(BlockPhp.DEFAULT_DIR);
            return;
        }

        if (pathParts[1] != null) {
            blockParentDir.setText(pathParts[1].substring(1));
            return;
        }
        blockParentDir.setText(BlockPhp.DEFAULT_DIR);
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
        final String directoryPart = getBlockDirectory().replace(
                File.separator,
                Package.fqnSeparator
        );

        return parts[0] + Package.fqnSeparator + parts[1] + Package.fqnSeparator + directoryPart;
    }
}
