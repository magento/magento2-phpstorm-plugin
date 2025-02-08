/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.NewUiComponentFormAction;
import com.magento.idea.magento2plugin.actions.generation.NewUiComponentGridAction;
import com.magento.idea.magento2plugin.actions.generation.data.AclXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.data.DataProviderDeclarationData;
import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.MenuXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.RoutesXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridToolbarData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AclResourceIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpNamespaceNameRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.RouteIdRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.StartWithNumberOrCapitalLetterRule;
import com.magento.idea.magento2plugin.actions.generation.generator.AclXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.DataProviderDeclarationGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.LayoutXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.MenuXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleControllerClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.RoutesXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.UiComponentDataProviderGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.UiComponentGridXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.FirstLetterToLowercaseUtil;
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesListUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import com.magento.idea.magento2plugin.util.magento.GetResourceCollections;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ExcessiveImports",
        "PMD.UnusedPrivateMethod",
        "PMD.TooManyMethods",
        "PMD.GodClass"
})
public class NewUiComponentGridDialog extends AbstractDialog {

    private static final String ACTION_NAME = "Action Name";
    private static final String DATA_PROVIDER_CLASS_NAME = "Data Provider Class Name";
    private static final String DATA_PROVIDER_DIRECTORY = "Data Provider Directory";
    private static final String NAME = "Name";

    private final Project project;
    private final String moduleName;
    private List<String> collectionOptions;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    private JCheckBox addToolBar;
    private JCheckBox addBookmarksCheckBox;
    private JCheckBox addColumnsControlCheckBox;
    private JCheckBox addFullTextSearchCheckBox;
    private JCheckBox addListingFiltersCheckBox;
    private JCheckBox addListingPagingCheckBox;
    private FilteredComboBox collection;
    private FilteredComboBox dataProviderType;
    private FilteredComboBox areaSelect;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE, NAME})
    private JTextField uiComponentName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER, message = {IdentifierRule.MESSAGE, NAME})
    private JTextField idField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DATA_PROVIDER_CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, DATA_PROVIDER_CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, DATA_PROVIDER_CLASS_NAME})
    private JTextField providerClassName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DATA_PROVIDER_DIRECTORY})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, DATA_PROVIDER_DIRECTORY})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {AlphanumericRule.MESSAGE, DATA_PROVIDER_DIRECTORY})
    private JTextField dataProviderParentDirectory;

    @FieldValidation(rule = RuleRegistry.ACL_RESOURCE_ID, message = {AclResourceIdRule.MESSAGE})
    private JTextField acl;

    @FieldValidation(rule = RuleRegistry.ACL_RESOURCE_ID, message = {AclResourceIdRule.MESSAGE})
    private FilteredComboBox parentAcl;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, "Acl Title"})
    private JTextField aclTitle;

    @FieldValidation(rule = RuleRegistry.ROUTE_ID, message = {RouteIdRule.MESSAGE})
    private JTextField route;

    @FieldValidation(rule = RuleRegistry.PHP_NAMESPACE_NAME,
            message = {PhpNamespaceNameRule.MESSAGE, "Controller Name"})
    private JTextField controllerName;

    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {StartWithNumberOrCapitalLetterRule.MESSAGE, ACTION_NAME})
    private JTextField actionName;

    @FieldValidation(rule = RuleRegistry.NUMERIC,
            message = {NumericRule.MESSAGE, "Sort Order"})
    private JTextField sortOrder;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Menu Identifier"})
    private JTextField menuIdentifier;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Menu Title"})
    private JTextField menuTitle;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Parent Menu"})
    private FilteredComboBox parentMenu;

    private JTextField tableName;

    private JLabel aclLabel;
    private JLabel routeLabel;//NOPMD
    private JLabel controllerLabel;//NOPMD
    private JLabel actionLabel;//NOPMD
    private JLabel parentMenuItemLabel;//NOPMD
    private JLabel sortOrderLabel;//NOPMD
    private JLabel menuIdentifierLabel;//NOPMD
    private JLabel menuTitleLabel;//NOPMD
    private JLabel formMenuLabel;//NOPMD
    private JLabel aclGeneralLabel;//NOPMD
    private JLabel parentAclID;//NOPMD
    private JLabel aclTitleLabel;//NOPMD
    private JLabel controllerGeneralLabel;//NOPMD
    private JLabel dataProviderGeneralLabel;//NOPMD
    private JLabel general;//NOPMD
    private JLabel collectionLabel;//NOPMD
    private JLabel dataProviderParentDirectoryLabel;
    private JLabel tableNameLabel;
    private JLabel uiComponentNameErrorMessage;//NOPMD
    private JLabel idFieldErrorMessage;//NOPMD
    private JLabel providerClassNameErrorMessage;//NOPMD
    private JLabel dataProviderParentDirectoryErrorMessage;//NOPMD
    private JLabel aclErrorMessage;//NOPMD
    private JLabel parentAclErrorMessage;//NOPMD
    private JLabel aclTitleErrorMessage;//NOPMD
    private JLabel routeErrorMessage;//NOPMD
    private JLabel controllerNameErrorMessage;//NOPMD
    private JLabel actionNameErrorMessage;//NOPMD
    private JLabel sortOrderErrorMessage;//NOPMD
    private JLabel menuIdentifierErrorMessage;//NOPMD
    private JLabel menuTitleErrorMessage;//NOPMD
    private JLabel parentMenuErrorMessage;//NOPMD

    /**
     * New UI component grid dialog constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewUiComponentGridDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();
        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPanel);
        setModal(false);
        setTitle(NewUiComponentGridAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        addActionListeners();
        setDefaultValues();

        buttonOK.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        final String componentIdentifierSuffix = "::listing";
        menuIdentifier.setText(getModuleName() + componentIdentifierSuffix);
        acl.setText(getModuleName() + componentIdentifierSuffix);

        dataProviderParentDirectory.setVisible(false);
        dataProviderParentDirectoryLabel.setVisible(false);

        addComponentListener(
                new FocusOnAFieldListener(() -> uiComponentName.requestFocusInWindow())
        );
    }

    /**
     * Open new UI component grid dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewUiComponentGridDialog dialog = new NewUiComponentGridDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Get grid data provider data.
     *
     * @return UiComponentGridDataProviderData
     */
    public UiComponentDataProviderData getGridDataProviderData() {
        return new UiComponentDataProviderData(
                getDataProviderClassName(),
                getDataProviderDirectory()
        );
    }

    /**
     * Get grid toolbar data.
     *
     * @return UiComponentGridToolbarData
     */
    public UiComponentGridToolbarData getUiComponentGridToolbarData() {
        return new UiComponentGridToolbarData(
                getAddToolBar(),
                getAddBookmarksCheckBox(),
                getAddColumnsControlCheckBox(),
                getAddFullTextSearchCheckBox(),
                getAddListingFiltersCheckBox(),
                getAddListingPagingCheckBox()
        );
    }

    /**
     * Get grid UI component data.
     *
     * @return UiComponentGridData
     */
    public UiComponentGridData getUiComponentGridData() {
        return new UiComponentGridData(
                getModuleName(),
                getArea(),
                getUiComponentName(),
                getEntityIdFieldName(),
                getAcl(),
                getDataProviderClassName(),
                getDataProviderDirectory(),
                getUiComponentGridToolbarData()
        );
    }

    @Override
    protected void onCancel() {
        dispose();
    }

    private void onOK() {
        if (validateFormFields()) {
            generateViewControllerFile();
            generateLayoutFile();
            generateMenuFile();
            generateAclXmlFile();
            generateRoutesXmlFile();
            generateDataProviderClass();
            generateDataProviderDeclaration();
            generateUiComponentFile();
            exit();
        }
    }

    private void setDefaultValues() {
        dataProviderParentDirectory.setText("Ui/Component/Listing");
    }

    private void addActionListeners() {
        addToolBar.addActionListener(event -> onAddToolBarChange());
        areaSelect.addActionListener(event -> onAreaChange());
        dataProviderType.addActionListener(event -> onDataProviderTypeChange());
    }

    /**
     * Generate routes file.
     */
    private void generateRoutesXmlFile() {
        new RoutesXmlGenerator(new RoutesXmlData(
            getArea(),
            getRoute(),
            getModuleName()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    /**
     * Generate Ui Component class.
     */
    private void generateUiComponentFile() {
        final UiComponentGridXmlGenerator gridXmlGenerator = new UiComponentGridXmlGenerator(
                getUiComponentGridData(),
                project
        );
        gridXmlGenerator.generate(NewUiComponentGridAction.ACTION_NAME, true);
    }

    /**
     * Generate data provider class.
     */
    private void generateDataProviderClass() {
        if (UiComponentDataProviderFile.CUSTOM_TYPE.equals(getDataProviderType())) {
            final UiComponentDataProviderGenerator dataProviderGenerator;
            dataProviderGenerator = new UiComponentDataProviderGenerator(
                getGridDataProviderData(),
                getModuleName(),
                project
            );
            dataProviderGenerator.generate(NewUiComponentGridAction.ACTION_NAME);
        }
    }

    /**
     * Generate data provider declaration.
     */
    private void generateDataProviderDeclaration() {
        if (UiComponentDataProviderFile.COLLECTION_TYPE.equals(getDataProviderType())) {
            final DataProviderDeclarationGenerator dataProviderGenerator;
            dataProviderGenerator = new DataProviderDeclarationGenerator(
                new DataProviderDeclarationData(
                    getModuleName(),
                    getDataProviderClassName(),
                    getCollection(),
                    getUiComponentName() + "_data_source",
                    getTableName()
                ), project);
            dataProviderGenerator.generate(NewUiComponentGridAction.ACTION_NAME);
        }
    }

    /**
     * Generate view controller file.
     */
    private void generateViewControllerFile() {
        final NamespaceBuilder namespace = new NamespaceBuilder(
                getModuleName(),
                getActionName(),
                getControllerDirectory()
        );
        new ModuleControllerClassGenerator(new ControllerFileData(
                getControllerDirectory(),
                getActionName(),
                getModuleName(),
                getArea(),
                HttpMethod.GET.toString(),
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
            getControllerName(),
            getActionName(),
            getUiComponentName()
        ), project).generate(NewUiComponentFormAction.ACTION_NAME, false);
    }

    /**
     * Generate menu xml file.
     */
    private void generateMenuFile() {
        new MenuXmlGenerator(new MenuXmlData(
            getParentMenuItem(),
            getSortOrder(),
            getModuleName(),
            getMenuIdentifier(),
            getMenuTitle(),
            getAcl(),
            getMenuAction()
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

    private String getModuleName() {
        return moduleName;
    }

    private void onAddToolBarChange() {
        final boolean enabled = getAddToolBar();

        addBookmarksCheckBox.setEnabled(enabled);
        addColumnsControlCheckBox.setEnabled(enabled);
        addFullTextSearchCheckBox.setEnabled(enabled);
        addListingFiltersCheckBox.setEnabled(enabled);
        addListingPagingCheckBox.setEnabled(enabled);
    }

    private void onAreaChange() {
        final boolean visible = getArea().equals(Areas.adminhtml.toString());
        acl.setVisible(visible);
        aclLabel.setVisible(visible);
    }

    private void onDataProviderTypeChange() {
        final boolean visible = UiComponentDataProviderFile.COLLECTION_TYPE.equals(
                getDataProviderType()
        );

        collection.setVisible(visible);
        collectionLabel.setVisible(visible);
        tableName.setVisible(visible);
        tableNameLabel.setVisible(visible);
        dataProviderParentDirectory.setVisible(!visible);
        dataProviderParentDirectoryLabel.setVisible(!visible);
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        this.collection = new FilteredComboBox(getCollectionOptions());
        this.dataProviderType = new FilteredComboBox(getProviderTypeOptions());
        this.areaSelect = new FilteredComboBox(getAreaOptions());
        areaSelect.setEnabled(false);
        this.parentMenu = new FilteredComboBox(getMenuReferences());
        this.parentAcl = new FilteredComboBox(getAclResourcesList());

        if (getAclResourcesList().contains(ModuleMenuXml.defaultAcl)) {
            parentAcl.setSelectedItem(ModuleMenuXml.defaultAcl);
        }
    }

    @NotNull
    private List<String> getMenuReferences() {
        final Collection<String> menuReferences
                = FileBasedIndex.getInstance().getAllKeys(MenuIndex.KEY, project);
        final ArrayList<String> menuReferencesList = new ArrayList<>(menuReferences);
        Collections.sort(menuReferencesList);
        return menuReferencesList;
    }

    private List<String> getCollectionOptions() {
        if (this.collectionOptions == null) {
            this.collectionOptions = new ArrayList<>();
            final GetResourceCollections getResourceCollections;
            getResourceCollections = GetResourceCollections.getInstance(
                    this.project
            );

            for (final PhpClass collectionClass: getResourceCollections.execute()) {
                this.collectionOptions.add(collectionClass.getFQN());
            }
        }

        return this.collectionOptions;
    }

    private List<String> getProviderTypeOptions() {
        return new ArrayList<>(
                Arrays.asList(
                        UiComponentDataProviderFile.COLLECTION_TYPE,
                        UiComponentDataProviderFile.CUSTOM_TYPE
                )
        );
    }

    private List<String> getAreaOptions() {
        return new ArrayList<>(
                Arrays.asList(
                        Areas.adminhtml.toString(),
                        Areas.base.toString(),
                        Areas.frontend.toString()
                )
        );
    }

    private String getDataProviderNamespace() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        final String directoryPart = getDataProviderDirectory().replace(
                File.separator,
                Package.fqnSeparator
        );

        return String.format(
                "%s%s%s%s%s",
                parts[0],
                Package.fqnSeparator,
                parts[1],
                Package.fqnSeparator,
                directoryPart
        );
    }

    private String getDataProviderClassFqn() {
        if (!UiComponentDataProviderFile.CUSTOM_TYPE.equals(getDataProviderType())) {
            return UiComponentDataProviderFile.DEFAULT_DATA_PROVIDER;
        }
        return String.format(
                "%s%s%s",
                getDataProviderNamespace(),
                Package.fqnSeparator,
                getDataProviderClassName()
        );
    }

    private Boolean getAddToolBar() {
        return addToolBar.isSelected();
    }

    private Boolean getAddColumnsControlCheckBox() {
        return addColumnsControlCheckBox.isSelected();
    }

    private Boolean getAddFullTextSearchCheckBox() {
        return addFullTextSearchCheckBox.isSelected();
    }

    private Boolean getAddListingFiltersCheckBox() {
        return addListingFiltersCheckBox.isSelected();
    }

    private Boolean getAddListingPagingCheckBox() {
        return addListingPagingCheckBox.isSelected();
    }

    private Boolean getAddBookmarksCheckBox() {
        return addBookmarksCheckBox.isSelected();
    }

    private String getDataProviderType() {
        return dataProviderType.getSelectedItem().toString();
    }

    private String getArea() {
        return areaSelect.getSelectedItem().toString();
    }

    private String getUiComponentName() {
        return uiComponentName.getText().toString();
    }

    private String getAcl() {
        return acl.getText().toString();
    }

    private String getEntityIdFieldName() {
        return idField.getText().toString();
    }

    private String getCollection() {
        final String collectionFqn = collection.getSelectedItem().toString().trim();

        if (collectionFqn.length() == 0) {
            return "";
        }

        return collectionFqn.substring(1);
    }

    /**
     * Get data provider class name.
     *
     * @return String
     */
    private String getDataProviderClassName() {
        return providerClassName.getText().trim();
    }

    private String getDataProviderDirectory() {
        return dataProviderParentDirectory.getText().trim();
    }

    public String getActionName() {
        return actionName.getText().trim();
    }

    private String getControllerDirectory() {
        final String directory = ControllerBackendPhp.DEFAULT_DIR;

        return directory + File.separator + getControllerName();
    }

    public String getControllerName() {
        return controllerName.getText().trim();
    }

    public String getRoute() {
        return route.getText().trim();
    }

    private String getParentMenuItem() {
        return parentMenu.getSelectedItem().toString();
    }

    public String getSortOrder() {
        return sortOrder.getText().trim();
    }

    public String getMenuIdentifier() {
        return menuIdentifier.getText().trim();
    }

    private String getMenuAction() {
        return getRoute()
            + File.separator
            + FirstLetterToLowercaseUtil.convert(getControllerName())
            + File.separator
            + FirstLetterToLowercaseUtil.convert(getActionName());
    }

    public String getMenuTitle() {
        return menuTitle.getText().trim();
    }

    private List<String> getAclResourcesList() {
        return GetAclResourcesListUtil.execute(project);
    }

    public String getParentAcl() {
        return parentAcl.getSelectedItem().toString().trim();
    }

    public String getAclTitle() {
        return aclTitle.getText().trim();
    }

    public String getTableName() {
        return tableName.getText().trim();
    }
}
