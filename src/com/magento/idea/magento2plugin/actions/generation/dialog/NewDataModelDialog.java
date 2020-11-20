/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
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
import com.magento.idea.magento2plugin.ui.table.ComboBoxEditor;
import com.magento.idea.magento2plugin.ui.table.DeleteRowButton;
import com.magento.idea.magento2plugin.ui.table.TableButton;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;

@SuppressWarnings({
        "PMD.ExcessiveImports",
})
public class NewDataModelDialog extends AbstractDialog {
    private final Project project;
    private final String moduleName;
    private NamespaceBuilder interfaceNamespace;
    private NamespaceBuilder modelNamespace;
    private String formattedProperties;

    private static final String MODEL_NAME = "Model Name";
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_TYPE = "Type";
    private static final String PROPERTY_ACTION = "Action";
    private static final String PROPERTY_DELETE = "Delete";

    private static final String[] PROPERTY_TYPES = {"int", "float", "string", "bool"};

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable properties;
    private JButton addProperty;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, MODEL_NAME})
    private JTextField modelName;

    /**
     * Constructor.
     */
    public NewDataModelDialog(final Project project, final PsiDirectory directory) {
        super();

        this.project = project;
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

        initPropertiesTable();

        // call onCancel() on ESCAPE KEY press
        contentPanel.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Opens the dialog window.
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewDataModelDialog dialog = new NewDataModelDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (validateFormFields()) {
            buildNamespaces();
            formatProperties();
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
                getInterfaceFQN(),
                getProperties()
        )).generate(NewDataModelAction.ACTION_NAME, true);
    }

    private void generateModelFile() {
        new DataModelGenerator(project, new DataModelData(
                getModelNamespace(),
                getModelName(),
                getModuleName(),
                getModelFQN(),
                getInterfaceFQN(),
                getProperties()
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

    /**
     * Formats properties into a string format, ready for templating.
     * "UPPER_SNAKE;lower_snake;type;UpperCamel;lowerCamel".
     */
    private void formatProperties() {
        final DefaultTableModel propertiesTable = getPropertiesTable();
        final ArrayList<String> properties = new ArrayList<>();
        final ArrayList<String> propertyData = new ArrayList<>();
        final int rowCount = propertiesTable.getRowCount();
        String name;
        String type;

        for (int index = 0; index < rowCount; index++, propertyData.clear()) {
            name = propertiesTable.getValueAt(index, 0).toString();
            type = propertiesTable.getValueAt(index, 1).toString();
            propertyData.add(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, name));
            propertyData.add(name);
            propertyData.add(type);
            propertyData.add(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name));
            propertyData.add(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
            properties.add(StringUtils.join(propertyData, ";"));
        }

        formattedProperties = StringUtils.join(properties, ",");
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

    private String getProperties() {
        return formattedProperties;
    }

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

        final TableColumn column = properties.getColumn(PROPERTY_ACTION);
        column.setCellRenderer(new TableButton(PROPERTY_DELETE));
        column.setCellEditor(new DeleteRowButton(new JCheckBox()));

        addProperty.addActionListener(e -> {
            propertiesTable.addRow(new Object[]{
                    "",
                    PROPERTY_TYPES[0],
                    PROPERTY_DELETE
            });
        });

        initPropertyTypeColumn();
    }

    private void initPropertyTypeColumn() {
        final TableColumn formElementTypeColumn = properties.getColumn(PROPERTY_TYPE);
        formElementTypeColumn.setCellEditor(new ComboBoxEditor(PROPERTY_TYPES));
        formElementTypeColumn.setCellRenderer(new ComboBoxTableRenderer<>(PROPERTY_TYPES));
    }

    private DefaultTableModel getPropertiesTable() {
        return (DefaultTableModel) properties.getModel();
    }
}
