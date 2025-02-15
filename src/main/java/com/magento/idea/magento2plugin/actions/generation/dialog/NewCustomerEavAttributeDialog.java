/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.data.CustomerEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.eavattribute.EavAttributeDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.CustomerEavAttributePatchGenerator;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.magento.idea.magento2plugin.magento.packages.uicomponent.AvailableSourcesByInput;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ExcessiveImports",
        "PMD.TooManyMethods",
        "PMD.UnusedPrivateField"
})
public class NewCustomerEavAttributeDialog extends EavAttributeDialog {

    private static final String ENTITY_NAME = "Customer";
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
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {NotEmptyRule.MESSAGE, "Attribute Sort Order"})
    private JTextField sortOrderTextField;
    private JComboBox<ComboBoxItemData> inputComboBox;
    private JComboBox<ComboBoxItemData> typeComboBox;
    private JComboBox<ComboBoxItemData> sourceComboBox;
    private JCheckBox requiredCheckBox;
    private JCheckBox visibleCheckBox;
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
    private JLabel codeTextFieldErrorMessage;
    private JLabel labelTextFieldErrorMessage;
    private JLabel dataPatchNameTextFieldErrorMessage;
    private JLabel sourceModelDirectoryTextFieldErrorMessage;
    private JLabel sourceModelNameTextFieldErrorMessage;
    private JLabel sortOrderTextFieldErrorMessage;
    private JCheckBox userDefineCheckBox;
    private JCheckBox useInAdminhtmlCustomerCheckBox;
    private JCheckBox useInCustomerAccountCreateCheckBox;
    private JCheckBox useInCustomerAccountEditCheckBox;
    private JCheckBox useInGridCheckBox;
    private JCheckBox filterableInGridCheckBox;
    private JCheckBox visibleInGridCheckBox;
    private JCheckBox systemAttributeCheckBox;
    private JCheckBox useInAdminhtmlCheckoutCheckBox;

    /**
     * Constructor.
     *
     * @param project    Project
     * @param directory  PsiDirectory
     * @param actionName String
     */
    public NewCustomerEavAttributeDialog(
            final Project project,
            final PsiDirectory directory,
            final String actionName
    ) {
        super(project, directory, actionName);
    }

    @Override
    protected JPanel getContentPanel() {
        return contentPanel;
    }

    @Override
    protected JButton getButtonOk() {
        return buttonOK;
    }

    @Override
    protected JButton getButtonCancel() {
        return buttonCancel;
    }

    @Override
    protected JComboBox<ComboBoxItemData> getAttributeTypeCompoBox() {
        return typeComboBox;
    }

    @Override
    protected JComboBox<ComboBoxItemData> getAttributeInputComboBox() {
        return inputComboBox;
    }

    @Override
    protected JTable getOptionsTable() {
        return optionsTable;
    }

    @Override
    protected JButton getNewOptionButton() {
        return addNewOptionButton;
    }

    @Override
    protected JComboBox<ComboBoxItemData> getAttributeSourceComboBox() {
        return sourceComboBox;
    }

    @Override
    protected JTextField getAttributeSourceModelNameTexField() {
        return sourceModelNameTextField;
    }

    @Override
    protected JTextField getSourceModelDirectoryTextField() {
        return sourceModelDirectoryTextField;
    }

    @Override
    protected JPanel getAttributeCustomSourceModelPanel() {
        return customSourceModelPanel;
    }

    @Override
    protected JPanel getAttributeOptionsPanel() {
        return optionsPanel;
    }

    @Override
    protected JTextField getAttributeCodeTextField() {
        return codeTextField;
    }

    @Override
    protected JTextField getDataPatchNameTextField() {
        return dataPatchNameTextField;
    }

    @Override
    protected JTextField getSourceModelNameTextField() {
        return sourceModelNameTextField;
    }

    @Override
    protected JTextField getAttributeLabelTexField() {
        return labelTextField;
    }

    @Override
    protected JTextField getAttributeSortOrderTextField() {
        return sortOrderTextField;
    }

    @Override
    protected JCheckBox getAttributeRequiredCheckBox() {
        return requiredCheckBox;
    }

    @Override
    protected JCheckBox getAttributeVisibleBox() {
        return visibleCheckBox;
    }

    @Override
    protected String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    protected void generateDataPatchFile(final EavEntityDataInterface eavEntityDataInterface) {
        new CustomerEavAttributePatchGenerator(
                eavEntityDataInterface,
                project,
                true
        ).generate(actionName, true);
    }

    @Override
    protected EavEntityDataInterface getEavEntityData() {
        return populateCategoryEntityData(new CustomerEntityData());
    }

    private CustomerEntityData populateCategoryEntityData(
            final CustomerEntityData customerEntityData
    ) {
        customerEntityData.setModuleName(moduleName);
        customerEntityData.setType(getAttributeBackendType());
        customerEntityData.setDataPatchName(getDataPatchName());
        customerEntityData.setCode(getAttributeCode());
        customerEntityData.setLabel(getAttributeLabel());
        customerEntityData.setSortOrder(getAttributeSortOrder());
        customerEntityData.setRequired(isRequiredAttribute());
        customerEntityData.setVisible(isVisibleAttribute());
        customerEntityData.setInput(getAttributeInput());
        customerEntityData.setSource(getAttributeSource(sourceModelData));
        customerEntityData.setOptions(
                getAttributeOptions(entityPropertiesTableGroupWrapper)
        );
        customerEntityData.setOptionsSortOrder(
                getAttributeOptionsSortOrders(entityPropertiesTableGroupWrapper)
        );
        customerEntityData.setUserDefined(userDefineCheckBox.isSelected());
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
        customerEntityData.setVisibleInGrid(visibleInGridCheckBox.isSelected());
        customerEntityData.setUsedInGrid(useInGridCheckBox.isSelected());
        customerEntityData.setFilterableInGrid(filterableInGridCheckBox.isSelected());
        customerEntityData.setSystem(systemAttributeCheckBox.isSelected());

        return customerEntityData;
    }

    @Override
    protected void addOptionPanelListener(
            final JComboBox<ComboBoxItemData> attributeSourceComboBox,
            final JComboBox<ComboBoxItemData> attributeInputComboBox,
            final JPanel attributeOptionsPanel
    ) {
        if (attributeSourceComboBox == null
                || attributeInputComboBox == null
                || attributeOptionsPanel == null
        ) {
            return;
        }

        attributeSourceComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent itemEvent) {
                final ComboBoxItemData selectedInputItem =
                        (ComboBoxItemData) attributeInputComboBox.getSelectedItem();
                final String selectedInput = selectedInputItem == null
                        ? "" : selectedInputItem.toString();
                final boolean isAllowedInput =
                        AttributeInput.SELECT.getInput().equals(selectedInput)
                                || AttributeInput.MULTISELECT.getInput().equals(selectedInput);

                attributeOptionsPanel.setVisible(isAllowedInput);
            }
        });
    }

    @Override
    protected void setAttributeInputComboBoxAction(
            final JComboBox<ComboBoxItemData> sourceComboBox,
            final JComboBox<ComboBoxItemData> inputComboBox
    ) {
        if (sourceComboBox == null || inputComboBox == null) {
            return;
        }

        inputComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent itemEvent) {
                final String selectedInput = itemEvent.getItem().toString();

                final List<ComboBoxItemData> availableSources =
                        new AvailableSourcesByInput(selectedInput).getItems();
                sourceComboBox.removeAllItems();

                if (!selectedInput.equals(AttributeInput.SELECT.getInput())
                        || !selectedInput.equals(AttributeInput.MULTISELECT.getInput())) {
                    final ComboBoxItemData defaultSourceItem = new ComboBoxItemData(
                            AttributeSourceModel.NULLABLE_SOURCE.name(),
                            AttributeSourceModel.NULLABLE_SOURCE.getSource()
                    );

                    sourceComboBox.addItem(defaultSourceItem);
                    sourceComboBox.setSelectedItem(defaultSourceItem);
                }

                if (availableSources.isEmpty()) {
                    return;
                }

                for (final ComboBoxItemData comboBoxItemData : availableSources) {
                    sourceComboBox.addItem(comboBoxItemData);

                    if (comboBoxItemData.getText().equals(AttributeSourceModel.TABLE.getSource())) {
                        sourceComboBox.setSelectedItem(comboBoxItemData);
                    }
                }
            }
        });
    }

    private void createUIComponents() { //NOPMD - suppressed UnusedPrivateMethod
        // TODO: place custom component creation code here
    }
}
