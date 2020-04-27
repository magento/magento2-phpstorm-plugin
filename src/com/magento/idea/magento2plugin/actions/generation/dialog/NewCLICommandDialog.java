/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewCLICommandAction;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewCLICommandValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.CLICommandClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;


import javax.swing.*;
import java.awt.event.*;

public class NewCLICommandDialog extends AbstractDialog {
    private JPanel contentPane;
    private JTextField cliCommandClassNameField;
    private JTextField cliCommandParentDirectoryField;
    private JTextField cliCommandNameField;
    private JTextField cliCommandDescriptionField;
    private JButton buttonCancel;
    private JButton buttonOK;

    private final Project project;
    private final String moduleName;
    private final NewCLICommandValidator validator;

    public NewCLICommandDialog(Project project, PsiDirectory directory) {
        this.project = project;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = NewCLICommandValidator.getInstance();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(this.bundle.message("common.cli.create.new.cli.command.title"));

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

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

    public static void open(Project project, PsiDirectory directory) {
        NewCLICommandDialog dialog = new NewCLICommandDialog(project, directory);

        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    public String getCLICommandClassName() {
        return this.cliCommandClassNameField.getText().trim();
    }

    public String getCLICommandParentDirectory() {
        return this.cliCommandParentDirectoryField.getText().trim();
    }

    public String getCLICommandName() {
        return this.cliCommandNameField.getText().trim();
    }

    public String getCLICommandDescription() {
        return this.cliCommandDescriptionField.getText().trim();
    }

    private String getCLICommandModule() {
        return this.moduleName;
    }

    private void onOK() {
        if (!validator.validate(this.project,this)) {
            return;
        }
        this.generate();
        this.setVisible(false);
    }

    private void generate() {
        try {
            CLICommandClassData cliCommandClassData = this.initializeCLICommandClassData();
            CLICommandClassGenerator cliCommandClassGenerator = new CLICommandClassGenerator(project, cliCommandClassData);
            cliCommandClassGenerator.generate(NewCLICommandAction.ACTION_NAME, true);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    this.bundle.message("common.cli.generate.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private CLICommandClassData initializeCLICommandClassData() {
        return new CLICommandClassData(
                this.getCLICommandClassName(),
                this.getCLICommandParentDirectory(),
                this.getCLICommandName(),
                this.getCLICommandDescription(),
                this.getCLICommandNamespace(),
                this.getCLICommandModule()
        );
    }

    private String getCLICommandNamespace() {
        NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                this.getCLICommandModule(),
                this.getCLICommandClassName(),
                this.getCLICommandParentDirectory()
        );

        return namespaceBuilder.getNamespace();
    }
}
