/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewCLICommandAction;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandXmlData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewCLICommandValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.CLICommandClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.CLICommandDiXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SuppressWarnings({"PMD.MissingSerialVersionUID"})
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
    private final CamelCaseToSnakeCase toSnakeCase;

    /**
     * Open new dialog for adding a new CLI Command.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewCLICommandDialog(final Project project, final PsiDirectory directory) {
        super();
        this.project = project;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = new NewCLICommandValidator();
        this.toSnakeCase = CamelCaseToSnakeCase.getInstance();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(this.bundle.message("common.cli.create.new.cli.command.title"));

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Open a new CLI command dialog.
     *
     * @param project Project
     * @param directory Directory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewCLICommandDialog dialog = new NewCLICommandDialog(project, directory);

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

    /**
     * Get di.xml item nme for CLI command based on the module name and class name.
     * @return di.xml item name
     */
    public String getDIXmlItemName() {
        final String diItemName = this.toSnakeCase.convert(this.getCLICommandClassName());

        return this.moduleName.toLowerCase(new Locale("en","EN"))
                + "_"
                + diItemName;
    }

    /**
     * Get current module name for which a CLI Command is being added.
     *
     * @return module name
     */
    public String getCLICommandModule() {
        return this.moduleName;
    }

    /**
     * Get a CLI Command PHP class namespace.
     *
     * @return PHP class namespace
     */
    public String getCLICommandClassFqn() {
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
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
        } catch (Exception exception) { //NOPMD
            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    this.bundle.message("common.cli.generate.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void generateCLICommandClass() {
        final CLICommandClassData dataClass = this.initializeCLICommandClassData();
        final CLICommandClassGenerator classGenerator =
                new CLICommandClassGenerator(project, dataClass);
        classGenerator.generate(NewCLICommandAction.ACTION_NAME, true);
    }

    private void generateCLICommandDiXmlRegistration() {
        final CLICommandXmlData cliCommandXmlData = this.initializeCLICommandDIXmlData();
        final CLICommandDiXmlGenerator diGenerator =
                new CLICommandDiXmlGenerator(project, cliCommandXmlData);
        diGenerator.generate(NewCLICommandAction.ACTION_NAME);
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
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                this.getCLICommandModule(),
                this.getCLICommandClassName(),
                this.getCLICommandParentDirectory()
        );

        return namespaceBuilder.getNamespace();
    }
}
