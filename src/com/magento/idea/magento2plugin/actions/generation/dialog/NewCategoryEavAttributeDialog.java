/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.data.CategoryEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.CategoryFormXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.eavattribute.EavAttributeDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.eavdialog.AttributeUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.CategoryFormXmlGenerator;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScope;
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
public class NewCategoryEavAttributeDialog extends EavAttributeDialog {

    private static final String ENTITY_NAME = "Category";
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
            message = {NotEmptyRule.MESSAGE, "Attribute Group"})
    private JTextField groupTextField;
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
    private JComboBox<ComboBoxItemData> scopeComboBox;
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
    private JLabel groupTextFieldErrorMessage;
    private JLabel sourceModelDirectoryTextFieldErrorMessage;
    private JLabel sourceModelNameTextFieldErrorMessage;
    private JLabel sortOrderTextFieldErrorMessage;

    /**
     * Constructor.
     *
     * @param project    Project
     * @param directory  PsiDirectory
     * @param actionName String
     */
    public NewCategoryEavAttributeDialog(
            final Project project,
            final PsiDirectory directory,
            final String actionName
    ) {
        super(project, directory, actionName);
    }

    @Override
    protected void initBaseDialogState() {
        super.initBaseDialogState();
        fillAttributeScopeComboBoxes();
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void fillAttributeScopeComboBoxes() {
        for (final AttributeScope globalValue : AttributeScope.values()) {
            scopeComboBox.addItem(
                    new ComboBoxItemData(globalValue.getScope(), globalValue.name())
            );
        }
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
    protected EavEntityDataInterface getEavEntityData() {
        return populateCategoryEntityData(new CategoryEntityData());
    }

    private CategoryEntityData populateCategoryEntityData(
            final CategoryEntityData categoryEntityData
    ) {
        categoryEntityData.setModuleName(moduleName);

        categoryEntityData.setDataPatchName(getDataPatchName());
        categoryEntityData.setCode(getAttributeCode());
        categoryEntityData.setLabel(getAttributeLabel());
        categoryEntityData.setSortOrder(getAttributeSortOrder());
        categoryEntityData.setRequired(isRequiredAttribute());
        categoryEntityData.setVisible(isVisibleAttribute());
        categoryEntityData.setType(getAttributeBackendType());
        categoryEntityData.setInput(getAttributeInput());
        categoryEntityData.setScope(
                AttributeUtil.getScopeClassBySelectedItem(
                        (ComboBoxItemData) scopeComboBox.getSelectedItem()
                )
        );
        categoryEntityData.setSource(getAttributeSource(sourceModelData));
        categoryEntityData.setOptions(
                getAttributeOptions(entityPropertiesTableGroupWrapper)
        );
        categoryEntityData.setOptionsSortOrder(
                getAttributeOptionsSortOrders(entityPropertiesTableGroupWrapper)
        );

        categoryEntityData.setGroup(groupTextField.getText().trim());

        return categoryEntityData;
    }

    @Override
    protected void generateExtraFilesAfterDataPatchGeneration(
            final EavEntityDataInterface eavEntityDataInterface
    ) {
        super.generateExtraFilesAfterDataPatchGeneration(eavEntityDataInterface);
        generateCategoryAdminForm((CategoryEntityData) eavEntityDataInterface);
    }

    private void generateCategoryAdminForm(final CategoryEntityData categoryEntityData) {
        final CategoryFormXmlData categoryFormXmlData = new CategoryFormXmlData(
                categoryEntityData.getGroup(),
                categoryEntityData.getCode(),
                categoryEntityData.getInput(),
                categoryEntityData.getSortOrder()
        );

        new CategoryFormXmlGenerator(
                categoryFormXmlData,
                project,
                moduleName
        ).generate(actionName, false);
    }
}
