/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.NewCLICommandAction;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewCLICommandValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.CLICommandClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.CLICommandDiXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;

import javax.swing.*;
import java.awt.event.*;

public class NewCLICommandDialog extends AbstractDialog {
    private JPanel contentPane;
    private JTextField cliCommandClassNameField;
    private JTextField cliCommandParentDirectoryField;
    private JTextField cliCommandNameField;
    private JTextArea cliCommandDescriptionField;
    private JButton buttonCancel;
    private JButton buttonOK;

    private final Project project;
    private final String moduleName;
    private final NewCLICommandValidator validator;
    private final CamelCaseToSnakeCase camelCaseToSnakeCase;

    public NewCLICommandDialog(Project project, PsiDirectory directory) {
        this.project = project;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = NewCLICommandValidator.getInstance();
        this.camelCaseToSnakeCase = CamelCaseToSnakeCase.getInstance();

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

    public String getDIXmlItemName() {
        String cliCommandClassNameToSnakeCase = this.camelCaseToSnakeCase.convert(this.getCLICommandClassName());

        return this.moduleName.toLowerCase() + "_" + cliCommandClassNameToSnakeCase;
    }

    public String getCLICommandModule() {
        return this.moduleName;
    }

    public String getCLICommandClassFqn() {
        NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                moduleName,
                this.getCLICommandClassName(),
                this.getCLICommandParentDirectory()
        );
        return namespaceBuilder.getClassFqn();
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
            this.generateCLICommandClass();
            this.generateCLICommandDiXmlRegistration();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    this.bundle.message("common.cli.generate.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void generateCLICommandClass() {
        CLICommandClassData cliCommandClassData = this.initializeCLICommandClassData();
        CLICommandClassGenerator cliCommandClassGenerator = new CLICommandClassGenerator(project, cliCommandClassData);
        cliCommandClassGenerator.generate(NewCLICommandAction.ACTION_NAME, true);
    }

    private void generateCLICommandDiXmlRegistration() {
        CLICommandXmlData cliCommandXmlData = this.initializeCLICommandDIXmlData();
        CLICommandDiXmlGenerator cliCommandDiXmlGenerator = new CLICommandDiXmlGenerator(project, cliCommandXmlData);
        cliCommandDiXmlGenerator.generate(NewCLICommandAction.ACTION_NAME);
    }

    private CLICommandXmlData initializeCLICommandDIXmlData() {
        return new CLICommandXmlData(
                this.getCLICommandModule(),
                this.getCLICommandClassFqn(),
                this.getDIXmlItemName()
        );
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
