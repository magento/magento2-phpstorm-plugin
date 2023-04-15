/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewDataModelAction;
import com.magento.idea.magento2plugin.actions.generation.OverrideClassByAPreferenceAction;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelData;
import com.magento.idea.magento2plugin.actions.generation.data.DataModelInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.DataModelGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.DataModelInterfaceGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PreferenceDiXmlGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import com.magento.idea.magento2plugin.ui.table.ComboBoxEditor;
import com.magento.idea.magento2plugin.ui.table.DeleteRowButton;
import com.magento.idea.magento2plugin.ui.table.TableButton;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.ExcessiveImports"
})
public class NewDataModelDialog extends AbstractDialog {

    private static final String MODEL_NAME = "Model Name";
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_TYPE = "Type";
    private static final String PROPERTY_ACTION = "Action";
    private static final String PROPERTY_DELETE = "Delete";

    private final Project project;
    private final String moduleName;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final List<String> properties;

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable propertyTable;
    private JButton addProperty;
    private JCheckBox createInterface;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS, message = {PhpClassRule.MESSAGE, MODEL_NAME})
    private JTextField modelName;

    private JLabel modelNameErrorMessage;//NOPMD

    /**
     * Constructor.
     */
    public NewDataModelDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.properties = new ArrayList<>();

        setContentPane(contentPanel);
        setModal(false);
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

        initPropertiesTable();

        // call onCancel() on ESCAPE KEY press
        contentPanel.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(new FocusOnAFieldListener(() -> modelName.requestFocusInWindow()));
    }

    /**
     * Opens the dialog window.
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewDataModelDialog dialog = new NewDataModelDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Proceed with generation.
     */
    private void onOK() {
        if (propertyTable.isEditing()) {
            propertyTable.getCellEditor().stopCellEditing();
        }

        if (validateFormFields()) {
            formatProperties();
            generateDataModelFile();

            if (createInterface.isSelected()) {
                generateDataModelInterfaceFile();
                generatePreferenceForInterface();
            }
            exit();
        }
    }

    @Override
    protected boolean validateFormFields() {
        boolean valid = false;

        if (super.validateFormFields()) {
            valid = true;
            final String errorTitle = commonBundle.message("common.error");
            final int column = 0;

            if (propertyTable.getRowCount() == 0) {
                valid = false;
                JOptionPane.showMessageDialog(
                        null,
                        validatorBundle.message("validator.properties.notEmpty"),
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );
            }

            for (int row = 0; row < propertyTable.getRowCount(); row++) {
                final String propertyName = ((String) propertyTable.getValueAt(row, column)).trim();
                if (propertyName.isEmpty()) {
                    valid = false;
                    final String errorMessage = validatorBundle.message(
                            "validator.notEmpty", "name"
                    );
                    JOptionPane.showMessageDialog(
                            null,
                            errorMessage,
                            errorTitle,
                            JOptionPane.ERROR_MESSAGE
                    );
                    break;
                } else if (!propertyName.matches(RegExUtil.LOWER_SNAKE_CASE)) {
                    valid = false;
                    final String errorMessage = validatorBundle.message(
                            "validator.lowerSnakeCase", "name"
                    );
                    JOptionPane.showMessageDialog(
                            null,
                            errorMessage,
                            errorTitle,
                            JOptionPane.ERROR_MESSAGE
                    );
                    break;
                }
            }
        }

        return valid;
    }

    /**
     * Generate DTO interface file.
     */
    private void generateDataModelInterfaceFile() {
        new DataModelInterfaceGenerator(new DataModelInterfaceData(
                getDtoInterfaceName(),
                getModuleName(),
                ClassPropertyFormatterUtil.joinProperties(properties)
        ), project).generate(NewDataModelAction.ACTION_NAME, true);
    }

    /**
     * Generate DTO model file.
     */
    private void generateDataModelFile() {
        new DataModelGenerator(project, new DataModelData(
                getDtoModelName(),
                getDtoInterfaceName(),
                getModuleName(),
                ClassPropertyFormatterUtil.joinProperties(properties),
                createInterface.isSelected()
        )).generate(NewDataModelAction.ACTION_NAME, true);
    }

    /**
     * Generate preference for interface DTO.
     */
    private void generatePreferenceForInterface() {
        new PreferenceDiXmlGenerator(new PreferenceDiXmFileData(
                getModuleName(),
                new DataModelInterfaceFile(getModuleName(), getDtoInterfaceName()).getClassFqn(),
                new DataModelFile(getModuleName(), getDtoModelName()).getClassFqn(),
                "base"
        ), project).generate(OverrideClassByAPreferenceAction.ACTION_NAME);
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
     * Get DTO model name.
     *
     * @return String
     */
    private String getDtoModelName() {
        return modelName.getText().trim();
    }

    /**
     * Get DTO interface name.
     *
     * @return String
     */
    private String getDtoInterfaceName() {
        return modelName.getText().trim().concat("Interface");
    }

    /**
     * Formats properties into an array of ClassPropertyData objects.
     */
    private void formatProperties() {
        properties.addAll(ClassPropertyFormatterUtil.formatProperties(getPropertiesTable()));
    }

    /**
     * Initialize properties table.
     */
    private void initPropertiesTable() {
        final DefaultTableModel propertiesTable = getPropertiesTable();

        propertiesTable.setDataVector(
                new Object[][]{},
                new Object[]{
                        PROPERTY_NAME,
                        PROPERTY_TYPE,
                        PROPERTY_ACTION
                }
        );

        final TableColumn column = propertyTable.getColumn(PROPERTY_ACTION);
        column.setCellRenderer(new TableButton(PROPERTY_DELETE));
        column.setCellEditor(new DeleteRowButton(new JCheckBox()));

        addProperty.addActionListener(e -> {
            propertiesTable.addRow(new Object[]{
                    "",
                    PropertiesTypes.valueOf(PropertiesTypes.INT.toString())
                            .getPropertyType(),
                    PROPERTY_DELETE
            });
        });

        initPropertyTypeColumn();
    }

    /**
     * Initialize property type column.
     */
    private void initPropertyTypeColumn() {
        final TableColumn formElementTypeColumn = propertyTable.getColumn(PROPERTY_TYPE);
        formElementTypeColumn.setCellEditor(
                new ComboBoxEditor(PropertiesTypes.getPropertyTypes())
        );
        formElementTypeColumn.setCellRenderer(
                new ComboBoxTableRenderer<>(PropertiesTypes.getPropertyTypes())
        );
    }

    /**
     * Get properties table.
     *
     * @return DefaultTableModel
     */
    private DefaultTableModel getPropertiesTable() {
        return (DefaultTableModel) propertyTable.getModel();
    }
}
