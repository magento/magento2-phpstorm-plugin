/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewUiComponentFormAction;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.RoutesXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewUiComponentFormValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.LayoutXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleControllerClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.RoutesXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.UiComponentDataProviderGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.UiComponentFormGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.ControllerFrontendPhp;
import com.magento.idea.magento2plugin.magento.files.FormButtonBlockPhp;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderPhp;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.ui.table.ComboBoxEditor;
import com.magento.idea.magento2plugin.ui.table.DeleteRowButton;
import com.magento.idea.magento2plugin.ui.table.TableButton;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ConstructorCallsOverridableMethod"
})
public class NewUiComponentFormDialog extends AbstractDialog {
    private final NewUiComponentFormValidator validator;
    private final Project project;
    private final String moduleName;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private FilteredComboBox formAreaSelect;
    private JTextField formName;
    private JTextField formLabel;
    private JTable formButtons;
    private JButton addButton;
    private JLabel formButtonsLabel;
    private JLabel formNameLabel;
    private JLabel formLabelLabel;
    private JLabel fieldsetsLabel;
    private JTable fieldsets;
    private JLabel fieldsLabel;
    private JTable fields;
    private JButton addFieldset;
    private JButton addField;
    private JTextField route;
    private JLabel routeLabel;
    private JLabel viewControllerLabel;
    private JLabel controllerNameLabel;
    private JTextField viewControllerName;
    private JTextField viewActionName;
    private JLabel actionNameLabel;
    private JLabel saveControllerLabel;
    private JLabel submitControllerNameLabel;
    private JTextField submitControllerName;
    private JLabel submitActionNameLabel;
    private JTextField submitActionName;
    private JTextField dataProviderClassName;
    private JLabel dataProviderLabel;
    private JLabel dataProviderClassNameLabel;
    private JLabel dataProviderDirectoryLabel;
    private JTextField dataProviderDirectory;

    /**
     * Open new dialog for adding new controller.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewUiComponentFormDialog(final Project project, final PsiDirectory directory) {
        super();
        this.project = project;
        this.validator = new NewUiComponentFormValidator(this);
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        initButtonsTable();
        initFieldSetsTable();
        initFieldTable();

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        onCancel();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    protected void initButtonsTable() {
        DefaultTableModel model = getFormButtonsModel();
        model.setDataVector(
                new Object[][] {},
                new Object[] { "Class", "Directory", "Type", "Label", "Sort Order", "Action" }
        );

        TableColumn column = formButtons.getColumn("Action");
        column.setCellRenderer(new TableButton("Delete"));
        column.setCellEditor(
                new DeleteRowButton(new JCheckBox()));

        addButton.addActionListener(e -> {
            model.addRow(new Object[] {"Button","Block/Adminhtml/Form","Save","","0","Delete"});
        });

        String[] buttonTypes = new String[] {
            FormButtonBlockPhp.TYPE_SAVE,
            FormButtonBlockPhp.TYPE_BACK,
            FormButtonBlockPhp.TYPE_DELETE,
            FormButtonBlockPhp.TYPE_CUSTOM
        };

        TableColumn typeColumn = formButtons.getColumn("Type");
        typeColumn.setCellEditor(new ComboBoxEditor(buttonTypes));
        typeColumn.setCellRenderer(new ComboBoxTableRenderer<>(buttonTypes));
    }

    protected void initFieldSetsTable() {
        DefaultTableModel model = getFieldsetsModel();
        model.setDataVector(
                new Object[][] {{"General","10","Delete"}},
                new Object[] { "Label", "SortOrder", "Action"}
        );

        TableColumn column = fieldsets.getColumn("Action");
        column.setCellRenderer(new TableButton("Delete"));
        column.setCellEditor(
                new DeleteRowButton(new JCheckBox()));

        addFieldset.addActionListener(e -> {
            model.addRow(new Object[] {"","","Delete"});
        });
        model.addTableModelListener(
            e -> {
                initFieldsetsColumn();
            }
        );
    }

    protected void initFieldTable() {
        DefaultTableModel model = getFieldsModel();
        model.setDataVector(
                new Object[][] {},
                new Object[] {
                    "Name",
                    "Label",
                    "SortOrder",
                    "Fieldset",
                    "Form Element Type",
                    "Data Type",
                    "Source",
                    "Action"
                }
        );

        TableColumn column = fields.getColumn("Action");
        column.setCellRenderer(new TableButton("Delete"));
        column.setCellEditor(
                new DeleteRowButton(new JCheckBox()));

        addField.addActionListener(e -> {
            model.addRow(new Object[] {
                    "",
                    "",
                    "",
                    getFieldsetLabels()[0],
                    "input",
                    "text",
                    "",
                    "Delete"
            });
        });

        initFieldsetsColumn();
        initFormElementTypeColumn();
    }

    private void initFormElementTypeColumn() {
        String[] formElementTypes = new String[]{
            "hidden",
            "file",
            "input",
            "date",
            "boolean",
            "checkbox",
            "checkboxset",
            "email",
            "select",
            "multiselect",
            "text",
            "textarea",
            "price",
            "radioset",
            "wysiwyg"
        };

        TableColumn formElementTypeColumn = fields.getColumn("Form Element Type");
        formElementTypeColumn.setCellEditor(new ComboBoxEditor(formElementTypes));
        formElementTypeColumn.setCellRenderer(new ComboBoxTableRenderer<>(formElementTypes));
    }

    private void initFieldsetsColumn() {
        String[] fieldsets = getFieldsetLabels();

        TableColumn fieldsetColumn = fields.getColumn("Fieldset");
        fieldsetColumn.setCellEditor(new ComboBoxEditor(fieldsets));
        fieldsetColumn.setCellRenderer(new ComboBoxTableRenderer<>(fieldsets));
    }

    private String[] getFieldsetLabels() {
        DefaultTableModel model = getFieldsetsModel();
        String[] fieldsets = new String[model.getRowCount()];
        for (int count = 0; count < model.getRowCount(); count++) {
            fieldsets[count] = model.getValueAt(count, 0).toString();
        }

        return fieldsets;
    }

    /**
     * Get controller name.
     *
     * @return String
     */
    public String getFormName() {
        return formName.getText().trim();
    }

    /**
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return formAreaSelect.getSelectedItem().toString();
    }

    /**
     * Open new controller dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewUiComponentFormDialog dialog = new NewUiComponentFormDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (!validator.validate()) {
            return;
        }

        generateRoutesXmlFile();
        generateViewControllerFile();
        generateSubmitControllerFile();
        generateDataProviderFile();
        generateLayoutFile();
        generateFormFile();
        this.setVisible(false);
    }

    private PsiFile generateDataProviderFile() {
        NamespaceBuilder namespace = getDataProviderNamespace();
        return new UiComponentDataProviderGenerator(new UiComponentDataProviderData(
            UiComponentDataProviderPhp.CUSTOM_TYPE,
            getDataProviderClassName(),
            namespace.getNamespace(),
            getDataProviderDirectory(),
            ""
        ), getModuleName(), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    @NotNull
    private NamespaceBuilder getDataProviderNamespace() {
        return new NamespaceBuilder(getModuleName(), getDataProviderClassName(), getDataProviderDirectory());
    }

    private PsiFile generateFormFile() {
        return new UiComponentFormGenerator(new UiComponentFormFileData(
                getFormName(),
                getArea(),
                getModuleName(),
                getFormLabel(),
                getButtons(),
                getFieldsets(),
                getFields(),
                getRoute(),
                getSubmitControllerName(),
                getSubmitActionName(),
                getDataProviderNamespace().getClassFqn()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, true);
    }

    private PsiFile generateRoutesXmlFile() {
        return new RoutesXmlGenerator(new RoutesXmlData(
            getArea(),
            getRoute(),
            getModuleName()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    private PsiFile generateViewControllerFile() {
        NamespaceBuilder namespace = new NamespaceBuilder(getModuleName(), getViewActionName(), getViewControllerDirectory());
        return new ModuleControllerClassGenerator(new ControllerFileData(
            getViewControllerDirectory(),
            getViewActionName(),
            getModuleName(),
            getArea(),
            HttpMethod.GET.toString(),
            null,
            true,
            namespace.getNamespace()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    private PsiFile generateSubmitControllerFile() {
        NamespaceBuilder namespace = new NamespaceBuilder(getModuleName(), getViewActionName(), getSubmitControllerDirectory());
        return new ModuleControllerClassGenerator(new ControllerFileData(
            getSubmitControllerDirectory(),
            getSubmitActionName(),
            getModuleName(),
            getArea(),
            HttpMethod.POST.toString(),
            null,
            true,
            namespace.getNamespace()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    private PsiFile generateLayoutFile() {
        return new LayoutXmlGenerator(new LayoutXmlData(
            getArea(),
            getRoute(),
            getModuleName(),
            getViewControllerName(),
            getViewActionName(),
            getFormName()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    protected void onCancel() {
        dispose();
    }

    private List<String> getAreaList() {
        return new ArrayList<>(
                Arrays.asList(
                        Areas.adminhtml.toString(),
                        Areas.frontend.toString()
                )
        );
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        this.formAreaSelect = new FilteredComboBox(getAreaList());
    }

    private String getModuleName() {
        return moduleName;
    }

    public String getFormLabel() {
        return formLabel.getText().trim();
    }

    public ArrayList<UiComponentFormButtonData> getButtons() {
        DefaultTableModel model = getFormButtonsModel();
        ArrayList<UiComponentFormButtonData> buttons = new ArrayList<UiComponentFormButtonData>();
        for (int count = 0; count < model.getRowCount(); count++) {
            String buttonDirectory = model.getValueAt(count, 1).toString();
            String buttonClassName = model.getValueAt(count, 0).toString();
            String buttonType = model.getValueAt(count, 2).toString();
            String buttonLabel = model.getValueAt(count, 3).toString();
            String buttonSortOrder = model.getValueAt(count, 4).toString();
            final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(getModuleName(), buttonClassName, buttonDirectory);

            UiComponentFormButtonData buttonData = new UiComponentFormButtonData(
                    buttonDirectory,
                    buttonClassName,
                    getModuleName(),
                    buttonType,
                    namespaceBuilder.getNamespace(),
                    buttonLabel,
                    buttonSortOrder,
                    getFormName(),
                    namespaceBuilder.getClassFqn()
            );

            buttons.add(
                    buttonData
            );
        }

        return buttons;
    }

    public ArrayList<UiComponentFormFieldsetData> getFieldsets() {
        DefaultTableModel model = getFieldsetsModel();
        ArrayList<UiComponentFormFieldsetData> fieldsets = new ArrayList<UiComponentFormFieldsetData>();
        for (int count = 0; count < model.getRowCount(); count++) {
            String label = model.getValueAt(count, 0).toString();
            String sortOrder = model.getValueAt(count, 1).toString();

            UiComponentFormFieldsetData fieldsetData = new UiComponentFormFieldsetData(
                    label,
                    sortOrder
            );

            fieldsets.add(
                    fieldsetData
            );
        }

        return fieldsets;
    }

    public ArrayList<UiComponentFormFieldData> getFields() {
        DefaultTableModel model = getFieldsModel();
        ArrayList<UiComponentFormFieldData> fieldsets = new ArrayList<UiComponentFormFieldData>();
        for (int count = 0; count < model.getRowCount(); count++) {
            String name = model.getValueAt(count, 0).toString();
            String label = model.getValueAt(count, 1).toString();
            String sortOrder = model.getValueAt(count, 2).toString();
            String fieldset = model.getValueAt(count, 3).toString();
            String formElementType = model.getValueAt(count, 4).toString();
            String dataType = model.getValueAt(count, 5).toString();
            String source = model.getValueAt(count, 6).toString();

            UiComponentFormFieldData fieldsetData = new UiComponentFormFieldData(
                    name,
                    label,
                    sortOrder,
                    fieldset,
                    formElementType,
                    dataType,
                    source
            );

            fieldsets.add(
                    fieldsetData
            );
        }

        return fieldsets;
    }

    protected DefaultTableModel getFormButtonsModel() {
        return (DefaultTableModel) formButtons.getModel();
    }

    protected DefaultTableModel getFieldsetsModel() {
        return (DefaultTableModel) fieldsets.getModel();
    }

    protected DefaultTableModel getFieldsModel() {
        return (DefaultTableModel) fields.getModel();
    }

    public String getRoute() {
        return route.getText().trim();
    }

    public String getViewActionName() {
        return viewActionName.getText().trim();
    }

    public String getViewControllerName() {
        return viewControllerName.getText().trim();
    }

    public String getSubmitActionName() {
        return submitActionName.getText().trim();
    }

    public String getSubmitControllerName() {
        return submitControllerName.getText().trim();
    }

    private String getViewControllerDirectory() {
        return getControllerDirectory() + getViewControllerName();
    }

    private String getSubmitControllerDirectory() {
        return getControllerDirectory() + getSubmitControllerName();
    }

    public String getDataProviderClassName() {
        return dataProviderClassName.getText().trim();
    }

    public String getDataProviderDirectory() {
        return dataProviderDirectory.getText().trim();
    }

    private String getControllerDirectory() {
        final String area = getArea();
        String directory = area.equals(Areas.adminhtml.toString())
            ? ControllerBackendPhp.DEFAULT_DIR : ControllerFrontendPhp.DEFAULT_DIR;

        return directory + File.separator;
    }
}
