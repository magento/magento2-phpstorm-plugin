/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewDataModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelData;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.DataModelGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.DataModelInterfaceGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.DataModel;
import com.magento.idea.magento2plugin.magento.files.DataModelInterface;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NewDataModelDialog extends AbstractDialog {
    private final Project project;
    private final PsiDirectory directory;
    private final String moduleName;
    private NamespaceBuilder interfaceNamespace;
    private NamespaceBuilder modelNamespace;

    private static final String MODEL_NAME = "Model Name";

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, MODEL_NAME})
    private JTextField modelName;

    public NewDataModelDialog(final Project project, final PsiDirectory directory) {
        super();

        this.project = project;
        this.directory = directory;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle(NewDataModelAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        // call onCancel() on dialog close
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE KEY press
        contentPanel.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    public static void open(final Project project, final PsiDirectory directory) {
        final NewDataModelDialog dialog = new NewDataModelDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    protected void onOK() {
        if (validateFormFields()) {
            buildNamespaces();
            generateModelInterfaceFile();
            generateModelFile();
            this.setVisible(false);
        }
    }

    @Override
    public void onCancel() {
        dispose();
    }

    private void generateModelInterfaceFile() {
        new DataModelInterfaceGenerator(project, new DataModelInterfaceData(
                getInterfaceNamespace(),
                getInterfaceName(),
                getModuleName(),
                getInterfaceFQN()
        )).generate(NewDataModelAction.ACTION_NAME, true);
    }

    private void generateModelFile() {
        new DataModelGenerator(project, new DataModelData(
                getModelNamespace(),
                getModelName(),
                getModuleName(),
                getModelFQN(),
                getInterfaceFQN()
        )).generate(NewDataModelAction.ACTION_NAME, true);
    }

    private void buildNamespaces() {
        interfaceNamespace = new NamespaceBuilder(
                getModuleName(), getInterfaceName(), DataModelInterface.DIRECTORY
        );
        modelNamespace = new NamespaceBuilder(
                getModuleName(), getModelName(), DataModel.DIRECTORY
        );
    }

    private String getModuleName() {
        return moduleName;
    }

    private String getInterfaceNamespace() {
        return interfaceNamespace.getNamespace();
    }

    private String getInterfaceName() {
        return modelName.getText().trim().concat("Interface");
    }

    private String getInterfaceFQN() {
        return interfaceNamespace.getClassFqn();
    }

    private String getModelNamespace() {
        return modelNamespace.getNamespace();
    }

    private String getModelName() {
        return modelName.getText().trim();
    }

    private String getModelFQN() {
        return modelNamespace.getClassFqn();
    }
}
