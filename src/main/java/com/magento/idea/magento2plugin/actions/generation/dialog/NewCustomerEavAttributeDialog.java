/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.data.CustomerEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.AttributeCodeAdapter;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.AttributeSourcePanelComponentListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.AttributeSourceRelationsItemListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.DataPatchNameAdapter;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.EavAttributeInputItemListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.OptionsPanelVisibilityChangeListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.SourceModelNameAdapter;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.eavdialog.AttributeUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.CustomerEavAttributePatchGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.SourceModelGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetAttributeOptionPropertiesUtil;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeType;
import com.magento.idea.magento2plugin.ui.table.TableGroupWrapper;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ExcessiveImports",
        "PMD.TooManyMethods",
        "PMD.UnusedPrivateField"
})
public class NewCustomerEavAttributeDialog extends AbstractDialog {

    private static final String ENTITY_NAME = "Customer";
    private String moduleName;
    private Project project;
    private String actionName;
    private TableGroupWrapper entityPropertiesTableGroupWrapper;
    private SourceModelData sourceModelData;

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Attribute Code"})
    @FieldValidation(rule = RuleRegistry.LOWERCASE,
            message = {Lowercase.MESSAGE, "Attribute Code"})
    private JTextField codeTextField;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Attribute Label"})
    private JTextField labelTextField;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Data Patch Name"})
    private JTextField dataPatchNameTextField;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Attribute Sort Order"})
    private JTextField sortOrderTextField;
    private JComboBox<ComboBoxItemData> inputComboBox;
    private JComboBox<ComboBoxItemData> typeComboBox;
    private JComboBox<ComboBoxItemData> sourceComboBox;
    private JCheckBox requiredCheckBox;
    private JCheckBox visibleCheckBox;
    private JCheckBox userDefineCheckBox;
    private JCheckBox useInGridCheckBox;
    private JCheckBox visibleInGridCheckBox;
    private JCheckBox filterableInGridCheckBox;
    private JCheckBox systemAttributeCheckBox;
    private JCheckBox useInAdminhtmlCustomerCheckBox;
    private JCheckBox useInAdminhtmlCheckoutCheckBox;
    private JCheckBox useInCustomerAccountCreateCheckBox;
    private JCheckBox useInCustomerAccountEditCheckBox;
    private JPanel sourcePanel;
    private JPanel customSourceModelPanel;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Source Model Directory"})
    private JTextField sourceModelDirectoryTextField;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Source Model Name"})
    private JTextField sourceModelNameTextField;
    private JTable optionsTable;
    private JButton addNewOptionButton;
    private JPanel optionsPanel;
    private JLabel labelTextFieldErrorMessage;
    private JLabel codeTextFieldErrorMessage;
    private JLabel dataPatchNameTextFieldErrorMessage;
    private JLabel sourceModelDirectoryTextFieldErrorMessage;
    private JLabel sourceModelNameTextFieldErrorMessage;
    private JLabel sortOrderTextFieldErrorMessage;

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewCustomerEavAttributeDialog(
            final Project project,
            final PsiDirectory directory,
            final String actionName
    ) {
        super(project);

        this.project = project;
        this.actionName = actionName;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.sourceModelData = new SourceModelData();

        setTitle(actionName);
        init();
    }

    /**
     * Open dialog window.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param actionName String
     */
    public static void open(
            final Project project,
            final PsiDirectory directory,
            final String actionName
    ) {
        final NewCustomerEavAttributeDialog dialog = new NewCustomerEavAttributeDialog(
                project,
                directory,
                actionName
        );
        dialog.initDialogState();
        dialog.showDialog();
    }

    /**
     * Initialize dialog state.
     */
    protected void initDialogState() {
        fillAttributeTypeComboBox();
        fillAttributeInputComboBox();
        initPropertiesTable();
        setAttributeInputComboBoxAction();
        setSourceComboBoxAction();
        setSourceModelPanelAction();
        addOptionPanelListener();
        setDefaultSources();
        setAutocompleteListenerForAttributeCodeField();
        setAutocompleteListenerForDataPathNameField();
        setAutocompleteListenerForSourceModelNameField();
    }

    /**
     * Fill attribute type combo box.
     */
    protected void fillAttributeTypeComboBox() {
        if (typeComboBox == null) {
            return;
        }

        for (final AttributeType typeValue : AttributeType.values()) {
            typeComboBox.addItem(
                    new ComboBoxItemData(typeValue.getType(), typeValue.getType())
            );
        }
    }

    /**
     * Fill attribute input combo box.
     */
    protected void fillAttributeInputComboBox() {
        if (inputComboBox == null) {
            return;
        }

        for (final AttributeInput inputValue : AttributeInput.values()) {
            inputComboBox.addItem(
                    new ComboBoxItemData(inputValue.getInput(), inputValue.getInput())
            );
        }
    }

    /**
     * Initialize properties table.
     */
    protected void initPropertiesTable() {
        // Initialize entity properties Table Group
        entityPropertiesTableGroupWrapper = new TableGroupWrapper(
                optionsTable,
                addNewOptionButton,
                new LinkedList<>(Arrays.asList(
                        "Value",
                        "Sort Order"
                )),
                getDefaultColumnsValues(),
                getColumnsSources()
        );
        entityPropertiesTableGroupWrapper.initTableGroup();
    }

    /**
     * Get default columns values.
     *
     * @return Map of String to String
     */
    protected Map<String, String> getDefaultColumnsValues() {
        return new HashMap<>();
    }

    /**
     * Get columns sources.
     *
     * @return Map of String to List of String
     */
    protected Map<String, List<String>> getColumnsSources() {
        return new HashMap<>();
    }

    /**
     * Set attribute input combo box action.
     */
    protected void setAttributeInputComboBoxAction() {
        if (sourceComboBox == null || inputComboBox == null) {
            return;
        }

        inputComboBox.addItemListener(
                new EavAttributeInputItemListener(sourceComboBox)
        );
    }

    /**
     * Set source combo box action.
     */
    protected void setSourceComboBoxAction() {
        if (sourceComboBox == null) {
            return;
        }

        sourceComboBox.addItemListener(
                new AttributeSourceRelationsItemListener(customSourceModelPanel)
        );
    }

    /**
     * Set source model panel action.
     */
    protected void setSourceModelPanelAction() {
        if (customSourceModelPanel == null || sourceModelDirectoryTextField == null) {
            return;
        }

        customSourceModelPanel.addComponentListener(
                new AttributeSourcePanelComponentListener(sourceModelDirectoryTextField)
        );
    }

    /**
     * Add option panel listener.
     */
    protected void addOptionPanelListener() {
        if (sourceComboBox == null
                || inputComboBox == null
                || optionsPanel == null
        ) {
            return;
        }

        sourceComboBox.addItemListener(
                new OptionsPanelVisibilityChangeListener(
                        optionsPanel,
                        inputComboBox
                )
        );
    }

    /**
     * Set default sources.
     */
    protected void setDefaultSources() {
        if (sourceComboBox == null) {
            return;
        }

        final ComboBoxItemData generateSourceItem = new ComboBoxItemData(
                AttributeSourceModel.GENERATE_SOURCE.getSource(),
                AttributeSourceModel.GENERATE_SOURCE.getSource()
        );
        final ComboBoxItemData defaultSourceItem = new ComboBoxItemData(
                AttributeSourceModel.NULLABLE_SOURCE.name(),
                AttributeSourceModel.NULLABLE_SOURCE.getSource()
        );

        sourceComboBox.addItem(defaultSourceItem);
        sourceComboBox.addItem(generateSourceItem);

        sourceComboBox.setSelectedItem(defaultSourceItem);
    }

    /**
     * Set autocomplete listener for attribute code field.
     */
    protected void setAutocompleteListenerForAttributeCodeField() {
        if (labelTextField == null || codeTextField == null) {
            return;
        }

        labelTextField.getDocument()
                .addDocumentListener(new AttributeCodeAdapter(codeTextField));
    }

    /**
     * Set autocomplete listener for data path name field.
     */
    protected void setAutocompleteListenerForDataPathNameField() {
        if (codeTextField == null || dataPatchNameTextField == null) {
            return;
        }

        codeTextField.getDocument()
                .addDocumentListener(
                        new DataPatchNameAdapter(
                                dataPatchNameTextField,
                                getEntityName()
                        )
                );
    }

    /**
     * Set autocomplete listener for source model name field.
     */
    protected void setAutocompleteListenerForSourceModelNameField() {
        if (codeTextField == null || sourceModelNameTextField == null) {
            return;
        }

        codeTextField.getDocument()
                .addDocumentListener(new SourceModelNameAdapter(sourceModelNameTextField));
    }

    /**
     * Get data patch name.
     *
     * @return String
     */
    protected String getDataPatchName() {
        return dataPatchNameTextField == null
                ? "" : dataPatchNameTextField.getText().trim();
    }

    /**
     * Get attribute code.
     *
     * @return String
     */
    protected String getAttributeCode() {
        return codeTextField == null
                ? "" : codeTextField.getText().trim();
    }

    /**
     * Get attribute label.
     *
     * @return String
     */
    protected String getAttributeLabel() {
        return labelTextField == null
                ? "" : labelTextField.getText().trim();
    }

    /**
     * Get attribute sort order.
     *
     * @return int
     */
    protected int getAttributeSortOrder() {
        return sortOrderTextField == null
                ? 0 : Integer.parseInt(sortOrderTextField.getText().trim());
    }

    /**
     * Is required attribute.
     *
     * @return boolean
     */
    protected boolean isRequiredAttribute() {
        return requiredCheckBox != null && requiredCheckBox.isSelected();
    }

    /**
     * Is visible attribute.
     *
     * @return boolean
     */
    protected boolean isVisibleAttribute() {
        return visibleCheckBox != null && visibleCheckBox.isSelected();
    }

    /**
     * Get attribute backend type.
     *
     * @return String
     */
    protected String getAttributeBackendType() {
        return AttributeUtil.getBackendTypeBySelectedItem(
                (ComboBoxItemData) typeComboBox.getSelectedItem()
        );
    }

    /**
     * Get attribute input.
     *
     * @return String
     */
    protected String getAttributeInput() {
        return AttributeUtil.getInputTypeBySelectedItem(
                (ComboBoxItemData) inputComboBox.getSelectedItem()
        );
    }

    /**
     * Get attribute source.
     *
     * @param sourceModelData SourceModelData
     * @return String
     */
    protected String getAttributeSource(final SourceModelData sourceModelData) {
        return AttributeUtil.getSourceClassBySelectedItem(
                (ComboBoxItemData) sourceComboBox.getSelectedItem(),
                sourceModelData
        );
    }

    /**
     * Get attribute options.
     *
     * @param entityPropertiesTableGroupWrapper TableGroupWrapper
     * @return Map of Integer to String
     */
    protected Map<Integer, String> getAttributeOptions(
            final TableGroupWrapper entityPropertiesTableGroupWrapper
    ) {
        return GetAttributeOptionPropertiesUtil.getValues(
                entityPropertiesTableGroupWrapper.getColumnsData()
        );
    }

    /**
     * Get attribute options sort orders.
     *
     * @param entityPropertiesTableGroupWrapper TableGroupWrapper
     * @return Map of Integer to String
     */
    protected Map<Integer, String> getAttributeOptionsSortOrders(
            final TableGroupWrapper entityPropertiesTableGroupWrapper
    ) {
        return GetAttributeOptionPropertiesUtil.getSortOrders(
                entityPropertiesTableGroupWrapper.getColumnsData()
        );
    }

    /**
     * Stop options table editing.
     */
    private void stopOptionsTableEditing() {
        if (optionsTable != null && optionsTable.isEditing()) {
            optionsTable.getCellEditor().stopCellEditing();
        }
    }

    /**
     * Generate source model file.
     */
    protected void generateSourceModelFile() {
        final ComboBoxItemData selectedSource =
                (ComboBoxItemData) sourceComboBox.getSelectedItem();

        if (selectedSource == null
                || !selectedSource.getText().equals(
                AttributeSourceModel.GENERATE_SOURCE.getSource()
        )) {
            return;
        }

        sourceModelData.setModuleName(moduleName);
        sourceModelData.setClassName(sourceModelNameTextField.getText().trim());
        sourceModelData.setDirectory(sourceModelDirectoryTextField.getText().trim());

        new SourceModelGenerator(sourceModelData, project, true)
                .generate(actionName, false);
    }

    /**
     * Generate data patch file.
     *
     * @param eavEntityDataInterface EavEntityDataInterface
     */
    protected void generateDataPatchFile(final EavEntityDataInterface eavEntityDataInterface) {
        new CustomerEavAttributePatchGenerator(
                eavEntityDataInterface,
                project,
                true
        ).generate(actionName, true);
    }

    /**
     * Generate extra files before data patch generation.
     */
    protected void generateExtraFilesBeforeDataPatchGeneration() {
        generateSourceModelFile();
    }

    /**
     * Generate extra files after data patch generation.
     *
     * @param eavEntityDataInterface EavEntityDataInterface
     */
    protected void generateExtraFilesAfterDataPatchGeneration(
            final EavEntityDataInterface eavEntityDataInterface
    ) {}

    /**
     * Create center panel.
     *
     * @return JComponent
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    /**
     * Get entity name.
     *
     * @return String
     */
    protected String getEntityName() {
        return ENTITY_NAME;
    }

    /**
     * Get EAV entity data.
     *
     * @return EavEntityDataInterface
     */
    protected EavEntityDataInterface getEavEntityData() {
        return populateCustomerEntityData(new CustomerEntityData());
    }

    /**
     * Populate customer entity data.
     *
     * @param customerEntityData CustomerEntityData
     * @return CustomerEntityData
     */
    private CustomerEntityData populateCustomerEntityData(
            final CustomerEntityData customerEntityData
    ) {
        customerEntityData.setModuleName(moduleName);

        customerEntityData.setDataPatchName(getDataPatchName());
        customerEntityData.setCode(getAttributeCode());
        customerEntityData.setLabel(getAttributeLabel());
        customerEntityData.setSortOrder(getAttributeSortOrder());
        customerEntityData.setRequired(isRequiredAttribute());
        customerEntityData.setVisible(isVisibleAttribute());
        customerEntityData.setType(getAttributeBackendType());
        customerEntityData.setInput(getAttributeInput());
        customerEntityData.setSource(getAttributeSource(sourceModelData));
        customerEntityData.setOptions(
                getAttributeOptions(entityPropertiesTableGroupWrapper)
        );
        customerEntityData.setOptionsSortOrder(
                getAttributeOptionsSortOrders(entityPropertiesTableGroupWrapper)
        );
        customerEntityData.setUserDefined(
                userDefineCheckBox.isSelected()
        );
        customerEntityData.setUsedInGrid(
                useInGridCheckBox.isSelected()
        );
        customerEntityData.setVisibleInGrid(
                visibleInGridCheckBox.isSelected()
        );
        customerEntityData.setFilterableInGrid(filterableInGridCheckBox.isSelected());
        customerEntityData.setSystem(systemAttributeCheckBox.isSelected());
        customerEntityData.setUseInAdminhtmlCustomerForm(
                useInAdminhtmlCustomerCheckBox.isSelected()
        );
        customerEntityData.setUseInAdminhtmlCheckoutForm(
                useInAdminhtmlCheckoutCheckBox.isSelected()
        );
        customerEntityData.setUseInCustomerAccountCreateForm(
                useInCustomerAccountCreateCheckBox.isSelected()
        );
        customerEntityData.setUseInCustomerAccountEditForm(
                useInCustomerAccountEditCheckBox.isSelected()
        );

        return customerEntityData;
    }

    /**
     * On write action OK.
     */
    @Override
    protected void onWriteActionOK() {
        stopOptionsTableEditing();
        generateExtraFilesBeforeDataPatchGeneration();
        final EavEntityDataInterface eavEntityDataInterface = getEavEntityData();
        generateDataPatchFile(eavEntityDataInterface);
        generateExtraFilesAfterDataPatchGeneration(eavEntityDataInterface);

        exit();
    }
}
