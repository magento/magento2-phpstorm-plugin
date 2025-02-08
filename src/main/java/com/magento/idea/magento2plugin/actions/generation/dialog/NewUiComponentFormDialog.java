/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewUiComponentFormAction;
import com.magento.idea.magento2plugin.actions.generation.data.AclXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.RoutesXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AclResourceIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpNamespaceNameRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.RouteIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.StartWithNumberOrCapitalLetterRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.ui.component.FormButtonsValidator;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.ui.component.FormFieldsValidator;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.ui.component.FormFieldsetsValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.AclXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.LayoutXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleControllerClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.RoutesXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.UiComponentDataProviderGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.UiComponentFormGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.ControllerFrontendPhp;
import com.magento.idea.magento2plugin.magento.files.FormButtonBlockFile;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.uicomponent.FormElementType;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.ui.table.ComboBoxEditor;
import com.magento.idea.magento2plugin.ui.table.DeleteRowButton;
import com.magento.idea.magento2plugin.ui.table.TableButton;
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesListUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.TooManyMethods",
        "PMD.ConstructorCallsOverridableMethod",
        "PMD.ExcessiveImports",
        "PMD.GodClass"
})
public class NewUiComponentFormDialog extends AbstractDialog {

    private static final String VIEW_ACTION_NAME = "View Action Name";
    private static final String SUBMIT_ACTION_NAME = "Submit Action Name";
    private static final String DATA_PROVIDER_CLASS_NAME = "Data Provider Class Name";
    private static final String DATA_PROVIDER_DIRECTORY = "Data Provider Directory";

    private static final String CLASS_COLUMN = "Class";
    private static final String DIRECTORY_COLUMN = "Directory";
    private static final String TYPE_COLUMN = "Type";
    private static final String LABEL_COLUMN = "Label";
    private static final String SORT_ORDER_COLUMN = "Sort Order";
    private static final String ACTION_COLUMN = "Action";
    private static final String DELETE_COLUMN = "Delete";
    private static final String NAME_COLUMN = "Name";
    private static final String FIELDSET_COLUMN = "Fieldset";
    private static final String FORM_ELEMENT_TYPE_COLUMN = "Form Element Type";
    private static final String DATA_TYPE_COLUMN = "Data Type";
    private static final String SOURCE_COLUMN = "Source";

    private final FormButtonsValidator formButtonsValidator;
    private final FormFieldsetsValidator formFieldsetsValidator;
    private final FormFieldsValidator formFieldsValidator;
    private final Project project;
    private final String moduleName;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private FilteredComboBox formAreaSelect;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, "Name"})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE, "Name"})
    private JTextField formName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, "Label"})
    private JTextField formLabel;

    private JTable formButtons;
    private JButton addButton;
    private JTable fieldsets;
    private JTable fields;
    private JButton addFieldset;
    private JButton addField;

    @FieldValidation(rule = RuleRegistry.ROUTE_ID, message = {RouteIdRule.MESSAGE})
    private JTextField route;

    @FieldValidation(rule = RuleRegistry.PHP_NAMESPACE_NAME,
            message = {PhpNamespaceNameRule.MESSAGE, "View Controller Name"})
    private JTextField viewControllerName;

    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, VIEW_ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, VIEW_ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, VIEW_ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {StartWithNumberOrCapitalLetterRule.MESSAGE, VIEW_ACTION_NAME})
    private JTextField viewActionName;

    @FieldValidation(rule = RuleRegistry.PHP_NAMESPACE_NAME,
            message = {PhpNamespaceNameRule.MESSAGE, "Submit Controller Name"})
    private JTextField submitControllerName;

    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, SUBMIT_ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, SUBMIT_ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, SUBMIT_ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {StartWithNumberOrCapitalLetterRule.MESSAGE, SUBMIT_ACTION_NAME})
    private JTextField submitActionName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DATA_PROVIDER_CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, DATA_PROVIDER_CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, DATA_PROVIDER_CLASS_NAME})
    private JTextField dataProviderClassName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DATA_PROVIDER_DIRECTORY})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, DATA_PROVIDER_DIRECTORY})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {AlphanumericRule.MESSAGE, DATA_PROVIDER_DIRECTORY})
    private JTextField dataProviderDirectory;

    private JLabel aclLabel;

    @FieldValidation(rule = RuleRegistry.ACL_RESOURCE_ID, message = {AclResourceIdRule.MESSAGE})
    private JTextField acl;
    @FieldValidation(rule = RuleRegistry.ACL_RESOURCE_ID, message = {AclResourceIdRule.MESSAGE})
    private FilteredComboBox parentAcl;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, "Acl Title"})
    private JTextField aclTitle;

    private JLabel formButtonsLabel;//NOPMD
    private JLabel formNameLabel;//NOPMD
    private JLabel formLabelLabel;//NOPMD
    private JLabel fieldsetsLabel;//NOPMD
    private JLabel fieldsLabel;//NOPMD
    private JLabel routeLabel;//NOPMD
    private JLabel viewControllerLabel;//NOPMD
    private JLabel controllerNameLabel;//NOPMD
    private JLabel actionNameLabel;//NOPMD
    private JLabel saveControllerLabel;//NOPMD
    private JLabel submitControllerNameLabel;//NOPMD
    private JLabel submitActionNameLabel;//NOPMD
    private JLabel dataProviderLabel;//NOPMD
    private JLabel dataProviderClassNameLabel;//NOPMD
    private JLabel dataProviderDirectoryLabel;//NOPMD
    private JLabel formNameErrorMessage;//NOPMD
    private JLabel formLabelErrorMessage;//NOPMD
    private JLabel routeErrorMessage;//NOPMD
    private JLabel viewControllerNameErrorMessage;//NOPMD
    private JLabel viewActionNameErrorMessage;//NOPMD
    private JLabel submitControllerNameErrorMessage;//NOPMD
    private JLabel submitActionNameErrorMessage;//NOPMD
    private JLabel dataProviderClassNameErrorMessage;//NOPMD
    private JLabel dataProviderDirectoryErrorMessage;//NOPMD
    private JLabel aclErrorMessage;//NOPMD
    private JLabel parentAclErrorMessage;//NOPMD
    private JLabel aclTitleErrorMessage;//NOPMD

    /**
     * Open new dialog for adding new controller.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewUiComponentFormDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();
        this.project = project;
        formButtonsValidator = new FormButtonsValidator(this);
        formFieldsetsValidator = new FormFieldsetsValidator(this);
        formFieldsValidator = new FormFieldsValidator(this);
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPane);
        setModal(false);
        setTitle(NewUiComponentFormAction.ACTION_DESCRIPTION);
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

        initButtonsTable();
        initFieldSetsTable();
        initFieldTable();

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        onCancel();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        formAreaSelect.addActionListener(e -> toggleAcl());
        formAreaSelect.setEnabled(false);
        acl.setText(getModuleName() + "::manage");

        addComponentListener(new FocusOnAFieldListener(() -> formName.requestFocusInWindow()));
    }

    protected void initButtonsTable() {
        final DefaultTableModel model = getFormButtonsModel();
        model.setDataVector(
                new Object[][] {},
                new Object[] {
                        CLASS_COLUMN,
                        DIRECTORY_COLUMN,
                        TYPE_COLUMN,
                        LABEL_COLUMN,
                        SORT_ORDER_COLUMN,
                        ACTION_COLUMN
                }
        );

        final TableColumn column = formButtons.getColumn(ACTION_COLUMN);
        column.setCellRenderer(new TableButton(DELETE_COLUMN));
        column.setCellEditor(
                new DeleteRowButton(new JCheckBox()));

        addButton.addActionListener(e -> {
            model.addRow(
                    new Object[] {"Button","Block/Adminhtml/Form","Save","","0",DELETE_COLUMN}
            );
        });

        final String[] buttonTypes = {
            FormButtonBlockFile.TYPE_SAVE,
            FormButtonBlockFile.TYPE_BACK,
            FormButtonBlockFile.TYPE_DELETE,
            FormButtonBlockFile.TYPE_CUSTOM
        };

        final TableColumn typeColumnObject = formButtons.getColumn(TYPE_COLUMN);
        typeColumnObject.setCellEditor(new ComboBoxEditor(buttonTypes));
        typeColumnObject.setCellRenderer(new ComboBoxTableRenderer<>(buttonTypes));
    }

    protected void initFieldSetsTable() {
        final Integer rowPosition = 10;
        final DefaultTableModel model = getFieldsetsModel();
        model.setDataVector(
                new Object[][] {{"general", "General", rowPosition, DELETE_COLUMN}},
                new Object[] { NAME_COLUMN, LABEL_COLUMN, SORT_ORDER_COLUMN, ACTION_COLUMN}
        );

        final TableColumn column = fieldsets.getColumn(ACTION_COLUMN);
        column.setCellRenderer(new TableButton(DELETE_COLUMN));
        column.setCellEditor(
                new DeleteRowButton(new JCheckBox()));

        addFieldset.addActionListener(e -> {
            model.addRow(new Object[] {"", "", rowPosition + 10, DELETE_COLUMN});
        });
        model.addTableModelListener(
                event -> {
                    initFieldsetsColumn();
                }
        );
    }

    protected void initFieldTable() {
        final DefaultTableModel model = getFieldsModel();
        model.setDataVector(
                new Object[][] {},
                new Object[] {
                    NAME_COLUMN,
                    LABEL_COLUMN,
                    SORT_ORDER_COLUMN,
                    FIELDSET_COLUMN,
                    FORM_ELEMENT_TYPE_COLUMN,
                    DATA_TYPE_COLUMN,
                    SOURCE_COLUMN,
                    ACTION_COLUMN
                }
        );

        final TableColumn column = fields.getColumn(ACTION_COLUMN);
        column.setCellRenderer(new TableButton(DELETE_COLUMN));
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
                    DELETE_COLUMN
            });
        });

        initFieldsetsColumn();
        initFormElementTypeColumn();
    }

    private void initFormElementTypeColumn() {
        final String[] formElementTypes = FormElementType.getTypeList().toArray(new String[0]);
        final TableColumn formElementTypeColumn = fields.getColumn(FORM_ELEMENT_TYPE_COLUMN);

        formElementTypeColumn.setCellEditor(new ComboBoxEditor(formElementTypes));
        formElementTypeColumn.setCellRenderer(new ComboBoxTableRenderer<>(formElementTypes));
    }

    private void initFieldsetsColumn() {
        final String[] fieldsets = getFieldsetLabels();

        final TableColumn fieldsetColumn = fields.getColumn(FIELDSET_COLUMN);
        fieldsetColumn.setCellEditor(new ComboBoxEditor(fieldsets));
        fieldsetColumn.setCellRenderer(new ComboBoxTableRenderer<>(fieldsets));
    }

    private String[] getFieldsetLabels() {
        final DefaultTableModel model = getFieldsetsModel();
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
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewUiComponentFormDialog dialog = new NewUiComponentFormDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (formButtons.isEditing()) {
            formButtons.getCellEditor().stopCellEditing();
        }

        if (fieldsets.isEditing()) {
            fieldsets.getCellEditor().stopCellEditing();
        }

        if (fields.isEditing()) {
            fields.getCellEditor().stopCellEditing();
        }

        if (validateFormFields()) {
            generateRoutesXmlFile();
            generateViewControllerFile();
            generateSubmitControllerFile();
            generateDataProviderFile();
            generateLayoutFile();
            generateFormFile();
            generateAclXmlFile();
            exit();
        }
    }

    /**
     * Generate data provider file.
     */
    private void generateDataProviderFile() {
        new UiComponentDataProviderGenerator(new UiComponentDataProviderData(
            getDataProviderClassName(),
            getDataProviderDirectory()
        ), getModuleName(), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    /**
     * Generate form file.
     */
    private void generateFormFile() {
        new UiComponentFormGenerator(new UiComponentFormFileData(
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
                getDataProviderClassName(),
                getDataProviderDirectory()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, true);
    }

    /**
     * Generate route xml file.
     */
    private void generateRoutesXmlFile() {
        new RoutesXmlGenerator(new RoutesXmlData(
            getArea(),
            getRoute(),
            getModuleName()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    /**
     * Generate view controller file.
     */
    private void generateViewControllerFile() {
        final NamespaceBuilder namespace = new NamespaceBuilder(
                getModuleName(),
                getViewActionName(),
                getViewControllerDirectory()
        );
        new ModuleControllerClassGenerator(new ControllerFileData(
            getViewControllerDirectory(),
            getViewActionName(),
            getModuleName(),
            getArea(),
            HttpMethod.GET.toString(),
            getAcl(),
            true,
            namespace.getNamespace()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    /**
     * Generate submit controller file.
     */
    private void generateSubmitControllerFile() {
        final NamespaceBuilder namespace = new NamespaceBuilder(
                getModuleName(),
                getViewActionName(),
                getSubmitControllerDirectory()
        );
        new ModuleControllerClassGenerator(new ControllerFileData(
            getSubmitControllerDirectory(),
            getSubmitActionName(),
            getModuleName(),
            getArea(),
            HttpMethod.POST.toString(),
            getAcl(),
            true,
            namespace.getNamespace()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    /**
     * Generate layout file.
     */
    private void generateLayoutFile() {
        new LayoutXmlGenerator(new LayoutXmlData(
            getArea(),
            getRoute(),
            getModuleName(),
            getViewControllerName(),
            getViewActionName(),
            getFormName()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    /**
     * Generate ACL XML file.
     */
    private void generateAclXmlFile() {
        new AclXmlGenerator(new AclXmlData(
            getParentAcl(),
            getAcl(),
            getAclTitle()
        ), getModuleName(), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    private List<String> getAreaList() {
        return new ArrayList<>(
                Arrays.asList(
                        Areas.adminhtml.toString(),
                        Areas.frontend.toString()
                )
        );
    }

    private List<String> getAclResourcesList() {
        return GetAclResourcesListUtil.execute(project);
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        this.formAreaSelect = new FilteredComboBox(getAreaList());
        this.parentAcl = new FilteredComboBox(getAclResourcesList());

        if (getAclResourcesList().contains(ModuleMenuXml.defaultAcl)) {
            parentAcl.setSelectedItem(ModuleMenuXml.defaultAcl);
        }
    }

    private String getModuleName() {
        return moduleName;
    }

    public String getFormLabel() {
        return formLabel.getText().trim();
    }

    /**
     * Return form buttons list.
     *
     * @return List[UiComponentFormButtonData]
     */
    public List<UiComponentFormButtonData> getButtons() {
        final DefaultTableModel model = getFormButtonsModel();
        final List<UiComponentFormButtonData> buttons = new ArrayList<>();
        for (int count = 0; count < model.getRowCount(); count++) {
            final String buttonDirectory = model.getValueAt(count, 1).toString();
            final String buttonClassName = model.getValueAt(count, 0).toString();
            final String buttonType = model.getValueAt(count, 2).toString();
            final String buttonLabel = model.getValueAt(count, 3).toString();
            final String buttonSortOrder = model.getValueAt(count, 4).toString();
            final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(//NOPMD
                    getModuleName(),
                    buttonClassName,
                    buttonDirectory
            );

            final UiComponentFormButtonData buttonData = new UiComponentFormButtonData(//NOPMD
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

    /**
     * Returns form fieldsets.
     *
     * @return List[UiComponentFormFieldsetData]
     */
    public List<UiComponentFormFieldsetData> getFieldsets() {
        final DefaultTableModel model = getFieldsetsModel();
        final ArrayList<UiComponentFormFieldsetData> fieldsets =
                new ArrayList<>();
        for (int count = 0; count < model.getRowCount(); count++) {
            final String name = model.getValueAt(count, 0).toString();
            final String label = model.getValueAt(count, 1).toString();
            final String sortOrder = model.getValueAt(count, 2).toString();

            final UiComponentFormFieldsetData fieldsetData = new UiComponentFormFieldsetData(//NOPMD
                    name,
                    label,
                    sortOrder
            );

            fieldsets.add(
                    fieldsetData
            );
        }

        return fieldsets;
    }

    /**
     * Returns form fields list.
     *
     * @return List[UiComponentFormFieldData]
     */
    public List<UiComponentFormFieldData> getFields() {
        final DefaultTableModel model = getFieldsModel();
        final ArrayList<UiComponentFormFieldData> fieldsets = new ArrayList<>();
        for (int count = 0; count < model.getRowCount(); count++) {
            final String name = model.getValueAt(count, 0).toString();
            final String label = model.getValueAt(count, 1).toString();
            final String sortOrder = model.getValueAt(count, 2).toString();
            final String fieldset = model.getValueAt(count, 3).toString();
            final String formElementType = model.getValueAt(count, 4).toString();
            final String dataType = model.getValueAt(count, 5).toString();
            final String source = model.getValueAt(count, 6).toString();

            final UiComponentFormFieldData fieldsetData = new UiComponentFormFieldData(//NOPMD
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

    public String getAcl() {
        return acl.getText().trim();
    }

    public String getParentAcl() {
        return parentAcl.getSelectedItem().toString().trim();
    }

    public String getAclTitle() {
        return aclTitle.getText().trim();
    }

    private String getControllerDirectory() {
        final String area = getArea();
        final String directory = area.equals(Areas.adminhtml.toString())
                ? ControllerBackendPhp.DEFAULT_DIR : ControllerFrontendPhp.DEFAULT_DIR;

        return directory + File.separator;
    }

    private void toggleAcl() {
        final String area = getArea();
        if (area.equals(Areas.adminhtml.toString())) {
            acl.setVisible(true);
            aclLabel.setVisible(true);
            return;
        }
        acl.setVisible(false);
        aclLabel.setVisible(false);
    }

    @Override
    protected boolean validateFormFields() {
        return super.validateFormFields()
                && formButtonsValidator.validate()
                && formFieldsetsValidator.validate()
                && formFieldsValidator.validate();
    }
}
