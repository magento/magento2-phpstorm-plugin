/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.actions.generation.NewEntityAction;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.reflection.GetReflectionFieldUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ProcessWorker;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.data.FieldValidationData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AclResourceIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.MenuIdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.RouteIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.TableNameLength;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorPoolHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.provider.NewEntityGeneratorsProviderUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DbSchemaGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.magento.files.actions.NewActionFile;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import com.magento.idea.magento2plugin.magento.packages.database.TableEngines;
import com.magento.idea.magento2plugin.magento.packages.database.TableResources;
import com.magento.idea.magento2plugin.magento.packages.uicomponent.FormElementType;
import com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.ui.table.TableGroupWrapper;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesListUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.UnusedPrivateField",
        "PMD.ExcessiveImports"
})
public class NewEntityDialog extends AbstractDialog {

    private final @NotNull Project project;
    private final String moduleName;
    private JPanel contentPane;
    private JTabbedPane tabbedPane1;
    private JPanel propertiesPanel;
    private JTable propertyTable;
    private JButton addProperty;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel generalTable;
    private JCheckBox createUiComponent;
    private JLabel entityNameLabel;
    private JLabel dbTableNameLabel;
    private JLabel entityIdColumnNameLabel;
    private JLabel routeLabel;
    private JLabel aclLabel;
    private JTextField aclTitle;
    private FilteredComboBox parentAcl;
    private JTextField formLabel;
    private JLabel formLabelLabel;
    private JLabel gridNameLabel;
    private JLabel parentMenuItemLabel;
    private JLabel sortOrderLabel;
    private JLabel menuIdentifierLabel;
    private JLabel menuTitleLabel;
    private JTextField menuTitle;
    private FilteredComboBox parentMenu;
    private JCheckBox addToolBar;
    private JCheckBox addBookmarksCheckBox;
    private JCheckBox addColumnsControlCheckBox;
    private JCheckBox addFullTextSearchCheckBox;
    private JCheckBox addListingFiltersCheckBox;
    private JCheckBox addListingPagingCheckBox;
    private JComboBox<ComboBoxItemData> tableEngine;
    private JLabel tableEngineLabel;
    private JComboBox<ComboBoxItemData> tableResource;
    private JLabel tableResourceLabel;
    private JCheckBox createInterface;
    private final List<String> properties;
    private TableGroupWrapper entityPropertiesTableGroupWrapper;

    private static final String ACTION_NAME = "Create Entity";
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_TYPE = "Type";
    private static final String ACL_ID = "ACL ID";
    private static final String FORM_NAME = "Form Name";
    private static final String GRID_NAME = "Grid Name";
    private static final String IDENTIFIER = "Identifier";
    private static final String SORT_ORDER = "Sort Order";
    private static final String UI_COMPONENTS_TAB_NAME = "Admin UI Components";
    private static final String TABLE_NAME = "DB Table Name";
    private static final String ENTITY_NAME = "Entity Name";
    private static final String ROUTER = "Route";
    private static final String ENTITY_ID = "Entity ID Field Name";

    private static final String MODEL_SUFFIX = "Model";
    private static final String RESOURCE_MODEL_SUFFIX = "Resource";
    private static final String COLLECTION_MODEL_SUFFIX = "Collection";
    private static final String DTO_MODEL_SUFFIX = "Data";
    private static final String DTO_INTERFACE_SUFFIX = "Interface";
    private static final String DATA_PROVIDER_SUFFIX = "DataProvider";

    private static final String DEFAULT_MENU_SORT_ORDER = "100";

    private static final boolean OPEN_FILES_FLAG = false;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, ENTITY_ID})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE, ENTITY_ID})
    private JTextField entityId;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, ENTITY_NAME})
    @FieldValidation(
            rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, ENTITY_NAME}
            )
    private JTextField entityName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, TABLE_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE, message = {Lowercase.MESSAGE, TABLE_NAME})
    @FieldValidation(
            rule = RuleRegistry.ALPHANUMERIC_WITH_UNDERSCORE,
            message = {AlphanumericWithUnderscoreRule.MESSAGE, TABLE_NAME}
    )
    @FieldValidation(rule = RuleRegistry.TABLE_NAME_LENGTH, message = {TableNameLength.MESSAGE})
    private JTextField dbTableName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, ROUTER})
    @FieldValidation(rule = RuleRegistry.ROUTE_ID, message = {RouteIdRule.MESSAGE})
    private JTextField route;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, FORM_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE, FORM_NAME})
    private JTextField formName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, GRID_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE, FORM_NAME})
    private JTextField gridName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, ACL_ID})
    @FieldValidation(rule = RuleRegistry.ACL_RESOURCE_ID, message = {AclResourceIdRule.MESSAGE})
    private JTextField acl;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, SORT_ORDER})
    @FieldValidation(rule = RuleRegistry.NUMERIC, message = {NumericRule.MESSAGE, SORT_ORDER})
    private JTextField sortOrder;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, IDENTIFIER})
    @FieldValidation(rule = RuleRegistry.MENU_IDENTIFIER, message = {MenuIdentifierRule.MESSAGE})
    private JTextField menuIdentifier;
    private JLabel formNameLabel;
    private JPanel uiComponentsPanel;
    private JPanel formNamePanel;
    private JPanel gridNamePanel;
    private JPanel aclIdPanel;
    private JPanel menuIdPanel;
    private JLabel formNameErrorMessage;
    private JLabel gridNameErrorMessage;
    private JLabel aclErrorMessage;
    private JLabel menuIdentifierErrorMessage;
    private JLabel sortOrderErrorMessage;
    private JLabel entityNameErrorMessage;
    private JLabel dbTableNameErrorMessage;
    private JLabel entityIdErrorMessage;
    private JLabel routeErrorMessage;
    private JCheckBox createWebApi;
    private JTextField observerName;
    private final ProcessWorker.InProgressFlag onOkActionFired;

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewEntityDialog(final @NotNull Project project, final PsiDirectory directory) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.properties = new ArrayList<>();

        setContentPane(contentPane);
        setModal(false);
        setTitle(NewEntityAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        onOkActionFired = new ProcessWorker.InProgressFlag(false);
        buttonOK.addActionListener(this::generateNewEntityFiles);
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }

            @SuppressWarnings("PMD.AccessorMethodGeneration")
            @Override
            public void windowOpened(final WindowEvent event) {
                entityName.requestFocus();
            }
        });

        initializeComboboxSources();
        initPropertiesTable();

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        entityName.addKeyListener(new KeyAdapter() {
            @SuppressWarnings("PMD.AccessorMethodGeneration")
            @Override
            public void keyReleased(final KeyEvent event) {
                entityName.setText(StringUtils.capitalize(entityName.getText()));
            }
        });

        entityName.getDocument().addDocumentListener(new DocumentAdapter() {
            @SuppressWarnings("PMD.AccessorMethodGeneration")
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                autoCompleteIdentifiers();
            }
        });

        toggleUiComponentsPanel();

        createUiComponent.addItemListener(event -> toggleUiComponentsPanel());
        registerTabbedPane(tabbedPane1);

        sortOrder.setText(DEFAULT_MENU_SORT_ORDER);
    }

    /**
     * Open new controller dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewEntityDialog dialog = new NewEntityDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Filter fields to validate if createUiComponent checkbox isn't selected.
     *
     * @return List[FieldValidationData]
     */
    @Override
    protected List<FieldValidationData> getFieldsToValidate() {
        final List<FieldValidationData> filteredFields = new LinkedList<>();

        if (createUiComponent.isSelected()) {
            filteredFields.addAll(super.getFieldsToValidate());
        } else {
            final List<Field> fieldsToIgnore = new ArrayList<>();
            fieldsToIgnore.add(GetReflectionFieldUtil.getByName("route", this.getClass()));
            fieldsToIgnore.add(GetReflectionFieldUtil.getByName("formName", this.getClass()));
            fieldsToIgnore.add(GetReflectionFieldUtil.getByName("gridName", this.getClass()));

            for (final FieldValidationData fieldData : super.getFieldsToValidate()) {
                if (fieldsToIgnore.contains(fieldData.getField())) {
                    continue;
                }
                filteredFields.add(fieldData);
            }
        }

        return filteredFields;
    }

    /**
     * Initialize combobox sources.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void initializeComboboxSources() {
        for (final String engine : TableEngines.getTableEnginesList()) {
            tableEngine.addItem(new ComboBoxItemData(engine, engine));
        }
        for (final String resource : TableResources.getTableResourcesList()) {
            tableResource.addItem(new ComboBoxItemData(resource, resource));
        }
    }

    /**
     * Initialize properties table.
     */
    private void initPropertiesTable() {
        final List<String> columns = new LinkedList<>(Arrays.asList(
                PROPERTY_NAME,
                PROPERTY_TYPE
        ));
        final Map<String, List<String>> sources = new HashMap<>();
        sources.put(PROPERTY_TYPE, PropertiesTypes.getPropertyTypesList());

        // Initialize entity properties Table Group
        entityPropertiesTableGroupWrapper = new TableGroupWrapper(
                propertyTable,
                addProperty,
                columns,
                new HashMap<>(),
                sources
        );
        entityPropertiesTableGroupWrapper.initTableGroup();
    }

    /**
     * Generate new entity files.
     *
     * @param event ActionEvent
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void generateNewEntityFiles(final @NotNull ActionEvent event) {
        if (!onOkActionFired.isInProgress()) {
            buttonOK.setEnabled(false);
            buttonCancel.setEnabled(false);

            if (propertyTable.isEditing()) {
                propertyTable.getCellEditor().stopCellEditing();
            }

            new ProcessWorker(
                    this::onOK,
                    this::releaseDialogAfterGeneration,
                    onOkActionFired
            ).execute();
        }
    }

    /**
     * Perform code generation using input data.
     */
    private void onOK() {
        if (!validateFormFields()) {
            onOkActionFired.setInProgress(false);
            return;
        }
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        formatProperties();

        final NewEntityDialogData dialogData = getNewEntityDialogData();
        final EntityCreatorContextData context = getEntityCreatorContextData(dialogData);
        final EntityCreatorContext generationContext = new EntityCreatorContext();
        generationContext.putUserData(
                EntityCreatorContext.DTO_TYPE,
                dialogData.hasDtoInterface()
                        ? context.getDtoInterfaceNamespaceBuilder().getClassFqn()
                        : context.getDtoModelNamespaceBuilder().getClassFqn()
        );
        generationContext.putUserData(EntityCreatorContext.ENTITY_ID, dialogData.getIdFieldName());
        GenerationContextRegistry.getInstance().setContext(generationContext);

        final GeneratorPoolHandler generatorPoolHandler = new GeneratorPoolHandler(context);

        NewEntityGeneratorsProviderUtil.initializeGenerators(
                generatorPoolHandler,
                context,
                dialogData
        );

        generatorPoolHandler.run();
        onOkActionFired.setFinished(true);
    }

    /**
     * Release dialog buttons and hide.
     */
    private void releaseDialogAfterGeneration() {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        buttonCancel.setEnabled(true);
        buttonOK.setEnabled(true);

        if (onOkActionFired.isFinished()) {
            exit();
        }
    }

    /**
     * Get entity creator context data.
     *
     * @param dialogData NewEntityDialogData
     *
     * @return EntityCreatorContextData
     */
    private EntityCreatorContextData getEntityCreatorContextData(
            final @NotNull NewEntityDialogData dialogData
    ) {
        final String entityName = dialogData.getEntityName();
        final String dtoClassName = entityName.concat(DTO_MODEL_SUFFIX);
        final String dtoInterfaceClassName = entityName.concat(DTO_INTERFACE_SUFFIX);

        final String actionsPathPrefix = dialogData.getRoute() + File.separator
                + entityName.toLowerCase(Locale.getDefault()) + File.separator;
        final NamespaceBuilder dtoModelNamespace =
                new DataModelFile(moduleName, dtoClassName).getNamespaceBuilder();
        final NamespaceBuilder dtoInterfaceNamespace =
                new DataModelInterfaceFile(moduleName, dtoInterfaceClassName).getNamespaceBuilder();

        final NamespaceBuilder formViewNamespaceBuilder =
                new NamespaceBuilder(
                        moduleName,
                        "Edit",
                        ControllerBackendPhp.DEFAULT_DIR + File.separator + entityName
                );

        return new EntityCreatorContextData(
                project,
                moduleName,
                ACTION_NAME,
                OPEN_FILES_FLAG,
                dialogData.hasWebApi(),
                actionsPathPrefix.concat("index"),
                actionsPathPrefix.concat("edit"),
                actionsPathPrefix.concat("new"),
                actionsPathPrefix.concat("delete"),
                dtoModelNamespace,
                dtoInterfaceNamespace,
                formViewNamespaceBuilder,
                new NewActionFile(moduleName, entityName).getNamespaceBuilder(),
                getEntityProperties(),
                getButtons(),
                getFieldSets(),
                getFields()
        );
    }

    /**
     * Formats properties into an array of ClassPropertyData objects.
     */
    private void formatProperties() {
        final String name = getEntityIdColumn();
        final String type = "int";

        properties.add(ClassPropertyFormatterUtil.formatSingleProperty(name, type));
        properties.addAll(ClassPropertyFormatterUtil.formatProperties(getPropertiesTable()));
    }

    /**
     * Get controller name.
     *
     * @return String
     */
    private String getFormName() {
        return formName.getText().trim();
    }

    /**
     * Returns form fieldSets.
     *
     * @return List[UiComponentFormFieldsetData]
     */
    private List<UiComponentFormFieldsetData> getFieldSets() {
        final ArrayList<UiComponentFormFieldsetData> fieldSets = new ArrayList<>();
        final UiComponentFormFieldsetData fieldsetData = new UiComponentFormFieldsetData(
                "general",
                "General",
                "10"
        );
        fieldSets.add(fieldsetData);

        return fieldSets;
    }

    /**
     * Return form buttons list.
     *
     * @return List[UiComponentFormButtonData]
     */
    private List<UiComponentFormButtonData> getButtons() {
        final List<UiComponentFormButtonData> buttons = new ArrayList<>();
        final String directory = "Block/Form/" + entityName.getText().trim();

        final NamespaceBuilder namespaceBuilderSave = new NamespaceBuilder(
                moduleName,
                "Save",
                directory
        );
        buttons.add(new UiComponentFormButtonData(
                directory,
                "Save",
                moduleName,
                "Save",
                namespaceBuilderSave.getNamespace(),
                "Save Entity",
                "10",
                getFormName(),
                namespaceBuilderSave.getClassFqn()
        ));

        final NamespaceBuilder namespaceBuilderBack = new NamespaceBuilder(
                moduleName,
                "Back",
                directory
        );
        buttons.add(new UiComponentFormButtonData(
                directory,
                "Back",
                moduleName,
                "Back",
                namespaceBuilderBack.getNamespace(),
                "Back To Grid",
                "20",
                getFormName(),
                namespaceBuilderBack.getClassFqn()
        ));

        final NamespaceBuilder namespaceBuilderDelete = new NamespaceBuilder(
                moduleName,
                "Delete",
                directory
        );
        buttons.add(new UiComponentFormButtonData(
                directory,
                "Delete",
                moduleName,
                "Delete",
                namespaceBuilderDelete.getNamespace(),
                "Delete Entity",
                "30",
                getFormName(),
                namespaceBuilderDelete.getClassFqn()
        ));
        return buttons;
    }

    /**
     * Returns form fields list.
     *
     * @return List[UiComponentFormFieldData]
     */
    private List<UiComponentFormFieldData> getFields() {
        final DefaultTableModel model = getPropertiesTable();
        final ArrayList<UiComponentFormFieldData> fieldsets = new ArrayList<>();

        fieldsets.add(
                new UiComponentFormFieldData(
                        getEntityIdColumn(),
                        "Entity ID",
                        "0",
                        "general",
                        FormElementType.HIDDEN.getType(),
                        "text",
                        getEntityIdColumn()
                )
        );

        for (int count = 0; count < model.getRowCount(); count++) {

            final String name = model.getValueAt(count, 0).toString();
            final String dataType = model.getValueAt(count, 1).toString();

            final String label = Arrays.stream(name.split("_")).map(
                    string -> string.substring(0, 1).toUpperCase(Locale.getDefault())
                            + string.substring(1)).collect(Collectors.joining(" ")
            );
            final String sortOrder = String.valueOf(count).concat("0");
            final String fieldset = "general";

            final PropertiesTypes property =
                    PropertiesTypes.getByValue(model.getValueAt(count, 1).toString());
            final String formElementType =
                    FormElementType.getDefaultForProperty(property).getType();

            final String source = model.getValueAt(count, 0).toString(); //todo: convert

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

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        final List<String> aclResourcesList = GetAclResourcesListUtil.execute(project);
        final Collection<String> menuReferences = FileBasedIndex
                .getInstance().getAllKeys(MenuIndex.KEY, project);
        final ArrayList<String> menuReferencesList = new ArrayList<>(menuReferences);
        Collections.sort(menuReferencesList);

        this.parentAcl = new FilteredComboBox(aclResourcesList);
        this.parentMenu = new FilteredComboBox(menuReferencesList);

        if (aclResourcesList.contains(ModuleMenuXml.defaultAcl)) {
            parentAcl.setSelectedItem(ModuleMenuXml.defaultAcl);
        }
    }

    /**
     * Get entity properties table columns data and format to suitable for generator.
     *
     * @return List of entity properties stored in HashMap.
     */
    private List<Map<String, String>> getEntityProperties() {
        final List<Map<String, String>> shortColumnsData =
                entityPropertiesTableGroupWrapper.getColumnsData();

        final List<Map<String, String>> columnsData =
                DbSchemaGeneratorUtil.complementShortPropertiesByDefaults(shortColumnsData);
        columnsData.add(0, DbSchemaGeneratorUtil.getTableIdentityColumnData(getEntityIdColumn()));

        return columnsData;
    }

    /**
     * Autocomplete entity name dependent fields.
     */
    private void autoCompleteIdentifiers() {
        final String entityNameValue = entityName.getText().trim();
        if (entityNameValue.isEmpty()) {
            return;
        }
        final String entityName = CamelCaseToSnakeCase.getInstance().convert(entityNameValue);
        final String entityNameLabel = Arrays.stream(entityName.split("_")).map(
                string -> string.substring(0, 1).toUpperCase(Locale.getDefault())
                        + string.substring(1)
        ).collect(Collectors.joining(" "));

        dbTableName.setText(entityName);
        entityId.setText(entityName.concat("_id"));
        route.setText(entityName);
        formLabel.setText(entityNameLabel.concat(" Form"));
        formName.setText(entityName.concat("_form"));
        gridName.setText(entityName.concat("_listing"));
        acl.setText(moduleName.concat("::management"));
        aclTitle.setText(entityNameLabel.concat(" Management"));
        menuIdentifier.setText(moduleName.concat("::management"));
        menuTitle.setText(entityNameLabel.concat(" Management"));
    }

    /**
     * Resolve ui components panel state.
     */
    private void toggleUiComponentsPanel() {
        if (createUiComponent.isSelected()) {
            tabbedPane1.add(uiComponentsPanel, 1);
            tabbedPane1.setTitleAt(1, UI_COMPONENTS_TAB_NAME);
        } else {
            tabbedPane1.remove(1);
        }
    }

    /**
     * Get new entity dialog data object.
     *
     * @return NewEntityDialogData
     */
    private NewEntityDialogData getNewEntityDialogData() {
        return new NewEntityDialogData(
                entityName.getText().trim(),
                dbTableName.getText().trim(),
                entityId.getText().trim(),
                getTableEngine(),
                getTableResource(),
                createUiComponent.isSelected(),
                createInterface.isSelected(),
                createWebApi.isSelected(),
                route.getText().trim(),
                formLabel.getText().trim(),
                formName.getText().trim(),
                gridName.getText().trim(),
                addToolBar.isSelected(),
                addBookmarksCheckBox.isSelected(),
                addColumnsControlCheckBox.isSelected(),
                addListingFiltersCheckBox.isSelected(),
                addListingPagingCheckBox.isSelected(),
                addFullTextSearchCheckBox.isSelected(),
                getParentAcl(),
                acl.getText().trim(),
                aclTitle.getText().trim(),
                getParentMenu(),
                Integer.parseInt(sortOrder.getText().trim()),
                menuIdentifier.getText().trim(),
                menuTitle.getText().trim(),
                ClassPropertyFormatterUtil.joinProperties(properties)
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

    /**
     * Get entity id column name.
     *
     * @return String
     */
    private String getEntityIdColumn() {
        return entityId.getText().trim();
    }

    /**
     * Get table engine.
     *
     * @return String
     */
    private String getTableEngine() {
        return tableEngine.getSelectedItem() == null ? ""
                : tableEngine.getSelectedItem().toString().trim();
    }

    /**
     * Get table resource.
     *
     * @return String
     */
    private String getTableResource() {
        return tableResource.getSelectedItem() == null ? ""
                : tableResource.getSelectedItem().toString().trim();
    }

    /**
     * Get parent acl resource id.
     *
     * @return String
     */
    private String getParentAcl() {
        return parentAcl.getSelectedItem() == null ? ""
                : parentAcl.getSelectedItem().toString().trim();
    }

    /**
     * Get parent menu id.
     *
     * @return String
     */
    private String getParentMenu() {
        return parentMenu.getSelectedItem() == null ? ""
                : parentMenu.getSelectedItem().toString().trim();
    }
}
