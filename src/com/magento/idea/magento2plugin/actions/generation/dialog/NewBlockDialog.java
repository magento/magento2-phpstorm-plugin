/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewBlockAction;
import com.magento.idea.magento2plugin.actions.generation.data.BlockFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewBlockValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleBlockClassGenerator;
import com.magento.idea.magento2plugin.magento.files.BlockPhp;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class NewBlockDialog extends AbstractDialog {
    private final NewBlockValidator validator;
    private final PsiDirectory baseDir;
    private final GetModuleNameByDirectory getModuleNameByDir;
    private final String moduleName;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField blockName;
    private JRadioButton adminhtmlRadioButton;
    private JTextField blockParentDir;
    private JTextPane warning;
    private Project project;

    public NewBlockDialog(Project project, PsiDirectory directory) {
        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = NewBlockValidator.getInstance(this);
        this.getModuleNameByDir = GetModuleNameByDirectory.getInstance(project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle("Create a new Magento 2 block..");
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();
        suggestBlockDirectory();

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

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void open(Project project, PsiDirectory directory) {
        NewBlockDialog dialog = new NewBlockDialog(project, directory);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void onOK() {
        if (!validator.validate()) {
            return;
        }
        generateFile();
        this.setVisible(false);
    }

    private PsiFile generateFile() {
        return new ModuleBlockClassGenerator(new BlockFileData(
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
        String path = baseDir.getVirtualFile().getPath();
        String moduleIdentifierPath = getModuleIdentifierPath();
        if (moduleIdentifierPath == null) {
            blockParentDir.setText(BlockPhp.DEFAULT_DIR);
            return;
        }
        String[] pathParts = path.split(moduleIdentifierPath);
        if (pathParts.length != 2) {
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
        String[]parts = moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        return parts[0] + File.separator + parts[1];
    }

    private String getNamespace() {
        String[]parts = moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        String directoryPart = getBlockDirectory().replace(File.separator, Package.FQN_SEPARATOR);
        return parts[0] + Package.FQN_SEPARATOR + parts[1] + directoryPart;
    }

    public void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
