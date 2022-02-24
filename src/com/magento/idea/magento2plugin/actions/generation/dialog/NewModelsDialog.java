/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.DocumentAdapter;
import com.magento.idea.magento2plugin.actions.generation.NewModelsAction;
import com.magento.idea.magento2plugin.actions.generation.data.CollectionData;
import com.magento.idea.magento2plugin.actions.generation.data.ModelData;
import com.magento.idea.magento2plugin.actions.generation.data.ResourceModelData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleCollectionGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleModelGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleResourceModelGenerator;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PMD.TooManyFields")
public class NewModelsDialog extends AbstractDialog {

    private final String moduleName;
    private final Project project;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private static final String ACTION_NAME = "Create Models";
    private static final String MODEL_NAME = "Model Name";
    private static final String RESOURCE_MODEL_NAME = "Resource Model Name";
    private static final String DB_TABLE_NAME = "DB Table Name";
    private static final String ENTITY_ID_COLUMN_NAME = "Entity ID Column Name";
    private static final String COLLECTION_NAME = "Collection Name";
    private static final String COLLECTION_DIRECTORY = "Collection Directory";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, MODEL_NAME})
    private JTextField modelName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, RESOURCE_MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, RESOURCE_MODEL_NAME})
    private JTextField resourceModelName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DB_TABLE_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER,
            message = {IdentifierRule.MESSAGE, DB_TABLE_NAME})
    private JTextField dbTableName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, ENTITY_ID_COLUMN_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER,
            message = {IdentifierRule.MESSAGE, ENTITY_ID_COLUMN_NAME})
    private JTextField entityIdColumn;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, COLLECTION_DIRECTORY})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, COLLECTION_DIRECTORY})
    private JTextField collectionDirectory;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, COLLECTION_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, COLLECTION_NAME})
    private JTextField collectionName;

    private JLabel modelNameLabel;//NOPMD
    private JLabel dbTableNameLabel;//NOPMD
    private JLabel resourceModelLabel;//NOPMD
    private JLabel entityIdColumnLabel;//NOPMD
    private JLabel collectionDirectoryLabel;//NOPMD
    private JLabel collectionNameLabel;//NOPMD
    private JLabel modelNameErrorMessage;//NOPMD
    private JLabel resourceModelNameErrorMessage;//NOPMD
    private JLabel dbTableNameErrorMessage;//NOPMD
    private JLabel entityIdColumnErrorMessage;//NOPMD
    private JLabel collectionDirectoryErrorMessage;//NOPMD
    private JLabel collectionNameErrorMessage;//NOPMD

    /**
     * Open new dialog for adding new controller.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewModelsDialog(final Project project, final PsiDirectory directory) {
        super();
        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPane);
        setModal(true);
        setTitle(NewModelsAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        this.modelName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                updateText();
            }
        });

        addComponentListener(new FocusOnAFieldListener(() -> modelName.requestFocusInWindow()));
    }

    /**
     * Update collection and resource model text.
     */
    public void updateText() {
        final String modelName = this.modelName.getText();
        this.resourceModelName.setText(modelName);
        this.collectionDirectory.setText(modelName);
    }

    /**
     * Open new controller dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewModelsDialog dialog = new NewModelsDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Process generation.
     */
    private void onOK() {
        if (validateFormFields()) {
            generateModelFile();
            generateResourceModelFile();
            generateCollectionFile();
            exit();
        }
    }

    /**
     * Generate model file.
     */
    private void generateModelFile() {
        new ModuleModelGenerator(new ModelData(
                getModuleName(),
                getDbTableName(),
                getModelName(),
                getResourceModelName()
        ), project).generate(NewModelsDialog.ACTION_NAME, true);
    }

    /**
     * Generate resource model file.
     */
    private void generateResourceModelFile() {
        new ModuleResourceModelGenerator(new ResourceModelData(
                getModuleName(),
                getDbTableName(),
                getResourceModelName(),
                getEntityIdColumn()
        ), project).generate(NewModelsDialog.ACTION_NAME, true);
    }

    /**
     * Generate collection file.
     */
    private void generateCollectionFile() {
        new ModuleCollectionGenerator(new CollectionData(
                getModuleName(),
                getDbTableName(),
                getModelName(),
                getResourceModelName(),
                getCollectionName(),
                getCollectionDirectory()
        ), project).generate(NewModelsDialog.ACTION_NAME, true);
    }

    /**
     * Get module name.
     *
     * @return String
     */
    private String getModuleName() {
        return moduleName;
    }

    /**
     * Get model name.
     *
     * @return String
     */
    private String getModelName() {
        return modelName.getText().trim();
    }

    /**
     * Get resource model name.
     *
     * @return String
     */
    private String getResourceModelName() {
        return resourceModelName.getText().trim();
    }

    /**
     * Get collection name.
     *
     * @return String
     */
    private String getCollectionName() {
        return collectionName.getText().trim();
    }

    /**
     * Get db table name.
     *
     * @return String
     */
    private String getDbTableName() {
        return dbTableName.getText().trim();
    }

    /**
     * Get entity id column name.
     *
     * @return String
     */
    private String getEntityIdColumn() {
        return entityIdColumn.getText().trim();
    }

    /**
     * Get collection directory.
     *
     * @return String
     */
    private String getCollectionDirectory() {
        return collectionDirectory.getText().trim();
    }
}
