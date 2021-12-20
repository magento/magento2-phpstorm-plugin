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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

public class NewBlockDialog extends AbstractDialog {

    private final PsiDirectory baseDir;
    private final String moduleName;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
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
        super();

        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle(NewBlockAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        suggestBlockDirectory();

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

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
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(new FocusOnAFieldListener(() -> blockName.requestFocusInWindow()));
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewBlockDialog dialog = new NewBlockDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    protected void onOK() {
        if (validateFormFields()) {
            generateFile();
            exit();
        }
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
