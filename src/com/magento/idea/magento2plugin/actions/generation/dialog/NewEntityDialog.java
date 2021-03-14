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
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.AclXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.CollectionModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DataModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DataModelInterfaceDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DataProviderDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DbSchemaXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.DeleteEntityByIdCommandDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.EntityDataMapperDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.EntityListActionDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormDeleteControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormEditControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormGenericButtonBlockDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormLayoutDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormSaveControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.FormViewControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.GetListQueryDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.GridActionColumnDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.GridLayoutXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.MenuXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.ModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.NewControllerDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.NewEntityLayoutDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.PreferenceDiXmlFileDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.ResourceModelDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.RoutesXmlDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.SaveEntityCommandDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.UiComponentFormLayoutDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.UiComponentGridDtoConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AclResourceIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.MenuIdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.GeneratorPoolHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.AclXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.CollectionModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataModelInterfaceGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataModelPreferenceGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DataProviderGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DbSchemaWhitelistGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DbSchemaXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.DeleteByIdCommandGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.EntityDataMapperGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.EntityListActionGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormDeleteControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormEditControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormGenericButtonBlockGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormLayoutGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormSaveControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.FormViewControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.GetListQueryGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.GridActionColumnGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.GridLayoutXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.MenuXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.ModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.NewControllerGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.NewEntityLayoutGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.ResourceModelGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.RoutesXmlGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.SaveCommandGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.UiComponentFormLayoutGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.pool.handler.UiComponentGridGeneratorHandler;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DbSchemaGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.CollectionModelFile;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.magento.files.EntityDataMapperFile;
import com.magento.idea.magento2plugin.magento.files.FormGenericButtonBlockFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.files.actions.DeleteActionFile;
import com.magento.idea.magento2plugin.magento.files.actions.EditActionFile;
import com.magento.idea.magento2plugin.magento.files.actions.IndexActionFile;
import com.magento.idea.magento2plugin.magento.files.actions.NewActionFile;
import com.magento.idea.magento2plugin.magento.files.actions.SaveActionFile;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import com.magento.idea.magento2plugin.magento.packages.database.TableEngines;
import com.magento.idea.magento2plugin.magento.packages.database.TableResources;
import com.magento.idea.magento2plugin.magento.packages.uiComponent.FormElementType;
import com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.ui.table.TableGroupWrapper;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;
import com.magento.idea.magento2plugin.util.FirstLetterToLowercaseUtil;
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesListUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.UnusedPrivateField",
        "PMD.CouplingBetweenObjects",
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
    private JTextField entityName;
    private JLabel entityNameLabel;
    private JLabel dbTableNameLabel;
    private JTextField dbTableName;
    private JTextField entityId;
    private JLabel entityIdColumnNameLabel;
    private JTextField route;
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
    private static final String GRID_NAME = "Grit Name";
    private static final String IDENTIFIER = "Identifier";
    private static final String SORT_ORDER = "Sort Order";
    private static final String UI_COMPONENTS_TAB_NAME = "Admin UI Components";

    private static final String MODEL_SUFFIX = "Model";
    private static final String RESOURCE_MODEL_SUFFIX = "Resource";
    private static final String COLLECTION_MODEL_SUFFIX = "Collection";
    private static final String DTO_MODEL_SUFFIX = "Data";
    private static final String DTO_INTERFACE_SUFFIX = "Interface";
    private static final String DATA_PROVIDER_SUFFIX = "DataProvider";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, FORM_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE})
    private JTextField formName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, GRID_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE})
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
    private JTextPane exampleIdentifier;
    private JTextPane exampleAclId;
    private JTextPane exampleFormName;
    private JTextPane exampleGridName;
    private JPanel uiComponentsPanel;
    private JTextField observerName;

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
        setModal(true);
        setTitle(NewEntityAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
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
     * Perform code generation using input data.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    private void onOK() {
        if (!validateFormFields()) {
            return;
        }
        formatProperties();

        final NewEntityDialogData dialogData = getNewEntityDialogData();
        final EntityManagerContextData context = getEntityManagerContextData(dialogData);

        final GeneratorPoolHandler generatorPoolHandler = new GeneratorPoolHandler(context);

        generatorPoolHandler
                .addNext(
                        ModelGeneratorHandler.class,
                        new ModelDtoConverter(context, dialogData)
                )
                .addNext(
                        ResourceModelGeneratorHandler.class,
                        new ResourceModelDtoConverter(context, dialogData)
                )
                .addNext(
                        CollectionModelGeneratorHandler.class,
                        new CollectionModelDtoConverter(context, dialogData)
                )
                .addNext(
                        DataModelGeneratorHandler.class,
                        new DataModelDtoConverter(context, dialogData)
                )
                .addNext(
                        DataModelInterfaceGeneratorHandler.class,
                        new DataModelInterfaceDtoConverter(context, dialogData),
                        dialogData::hasDtoInterface
                )
                .addNext(
                        DataModelPreferenceGeneratorHandler.class,
                        new PreferenceDiXmlFileDtoConverter(context, dialogData),
                        dialogData::hasDtoInterface
                )
                .addNext(
                        RoutesXmlGeneratorHandler.class,
                        new RoutesXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        AclXmlGeneratorHandler.class,
                        new AclXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        MenuXmlGeneratorHandler.class,
                        new MenuXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        EntityListActionGeneratorHandler.class,
                        new EntityListActionDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        GridLayoutXmlGeneratorHandler.class,
                        new GridLayoutXmlDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        EntityDataMapperGeneratorHandler.class,
                        new EntityDataMapperDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        GetListQueryGeneratorHandler.class,
                        new GetListQueryDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        DataProviderGeneratorHandler.class,
                        new DataProviderDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        GridActionColumnGeneratorHandler.class,
                        new GridActionColumnDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        UiComponentGridGeneratorHandler.class,
                        new UiComponentGridDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormViewControllerGeneratorHandler.class,
                        new FormViewControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormLayoutGeneratorHandler.class,
                        new FormLayoutDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        NewEntityLayoutGeneratorHandler.class,
                        new NewEntityLayoutDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        SaveCommandGeneratorHandler.class,
                        new SaveEntityCommandDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        DeleteByIdCommandGeneratorHandler.class,
                        new DeleteEntityByIdCommandDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormSaveControllerGeneratorHandler.class,
                        new FormSaveControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormDeleteControllerGeneratorHandler.class,
                        new FormDeleteControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormEditControllerGeneratorHandler.class,
                        new FormEditControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        FormGenericButtonBlockGeneratorHandler.class,
                        new FormGenericButtonBlockDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        NewControllerGeneratorHandler.class,
                        new NewControllerDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        UiComponentFormLayoutGeneratorHandler.class,
                        new UiComponentFormLayoutDtoConverter(context, dialogData),
                        dialogData::hasAdminUiComponents
                )
                .addNext(
                        DbSchemaXmlGeneratorHandler.class,
                        new DbSchemaXmlDtoConverter(context, dialogData)
                )
                .addNext(
                        DbSchemaWhitelistGeneratorHandler.class,
                        new DbSchemaXmlDtoConverter(context, dialogData)
                );

        generatorPoolHandler.run();

        this.setVisible(false);
    }

    /**
     * Get entity manager context data.
     *
     * @param dialogData NewEntityDialogData
     *
     * @return EntityManagerContextData
     */
    private EntityManagerContextData getEntityManagerContextData(
            final @NotNull NewEntityDialogData dialogData
    ) {
        final String entityName = dialogData.getEntityName();
        final String modelClassName = entityName.concat(MODEL_SUFFIX);
        final String resourceClassName = entityName.concat(RESOURCE_MODEL_SUFFIX);
        final String collectionClassName = entityName.concat(COLLECTION_MODEL_SUFFIX);
        final String dataProviderClassName = entityName.concat(DATA_PROVIDER_SUFFIX);
        final String dtoClassName = entityName.concat(DTO_MODEL_SUFFIX);
        final String dtoInterfaceClassName = entityName.concat(DTO_INTERFACE_SUFFIX);

        final String actionsPathPrefix = dialogData.getRoute() + File.separator
                + FirstLetterToLowercaseUtil.convert(entityName) + File.separator;
        final NamespaceBuilder dtoModelNamespace =
                new DataModelFile(dtoClassName).getNamespaceBuilder(moduleName);
        final NamespaceBuilder dtoInterfaceNamespace =
                new DataModelInterfaceFile(dtoInterfaceClassName).getNamespaceBuilder(moduleName);

        final NamespaceBuilder formViewNamespaceBuilder =
                new NamespaceBuilder(
                        moduleName,
                        "Edit",
                        ControllerBackendPhp.DEFAULT_DIR + File.separator + entityName
                );

        return new EntityManagerContextData(
                project,
                moduleName,
                ACTION_NAME,
                actionsPathPrefix.concat("index"),
                actionsPathPrefix.concat("edit"),
                actionsPathPrefix.concat("new"),
                actionsPathPrefix.concat("delete"),
                new ModelFile(modelClassName).getNamespaceBuilder(moduleName),
                new ResourceModelFile(resourceClassName).getNamespaceBuilder(moduleName),
                new CollectionModelFile(collectionClassName)
                        .getNamespaceBuilder(moduleName, entityName),
                dtoModelNamespace,
                dtoInterfaceNamespace,
                createInterface.isSelected() ? dtoInterfaceNamespace : dtoModelNamespace,
                UiComponentDataProviderFile
                        .getInstance(dataProviderClassName).getNamespaceBuilder(moduleName),
                new IndexActionFile(entityName).getNamespaceBuilder(moduleName),
                new EntityDataMapperFile(entityName).getNamespaceBuilder(moduleName),
                SaveEntityCommandFile.getNamespaceBuilder(moduleName, entityName),
                DeleteEntityByIdCommandFile.getNamespaceBuilder(moduleName, entityName),
                formViewNamespaceBuilder,
                NewActionFile.getNamespaceBuilder(moduleName, entityName),
                SaveActionFile.getNamespaceBuilder(moduleName, entityName),
                DeleteActionFile.getNamespaceBuilder(moduleName, entityName),
                new EditActionFile(entityName).getNamespaceBuilder(moduleName),
                FormGenericButtonBlockFile.getNamespaceBuilder(moduleName),
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
    public String getFormName() {
        return formName.getText().trim();
    }

    /**
     * Returns form fieldSets.
     *
     * @return List[UiComponentFormFieldsetData]
     */
    public List<UiComponentFormFieldsetData> getFieldSets() {
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
    protected List<UiComponentFormButtonData> getButtons() {
        final List<UiComponentFormButtonData> buttons = new ArrayList<>();
        final String directory = "Block/Form";

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
    public List<UiComponentFormFieldData> getFields() {
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
