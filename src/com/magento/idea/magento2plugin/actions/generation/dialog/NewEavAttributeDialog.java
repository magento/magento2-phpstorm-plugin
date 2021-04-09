/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.DocumentAdapter;
import com.magento.idea.magento2plugin.actions.generation.NewEavAttributeAction;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.EavAttributeSetupPatchGenerator;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInputs;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScopes;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeTypes;
import com.magento.idea.magento2plugin.magento.packages.eav.EavEntities;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports"})
public class NewEavAttributeDialog extends AbstractDialog {
    private final String moduleName;
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
    private JComboBox<ComboBoxItemData> entityType;
    private JComboBox<ComboBoxItemData> inputComboBox;
    private JComboBox<ComboBoxItemData> typeComboBox;
    private JComboBox<ComboBoxItemData> scopeComboBox;
    private JCheckBox requiredCheckBox;
    private JCheckBox usedInGridGridCheckBox;
    private JCheckBox visibleInGridCheckBox;
    private JCheckBox filterableInGridCheckBox;
    private JCheckBox visibleCheckBox;
    private JCheckBox htmlAllowedOnCheckBox;
    private JCheckBox visibleOnFrontCheckBox;
    private final Project project;

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewEavAttributeDialog(final Project project, final PsiDirectory directory) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setPanelConfiguration();
        addActionListenersForButtons();
        addCancelActionForWindow();
        addCancelActionForEsc();
        setAutocompleteListenerForAttributeCodeField();
        fillEntityTypeComboBox();
    }

    private void setPanelConfiguration() {
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
    }

    private void addActionListenersForButtons() {
        buttonOK.addActionListener(e -> onOk());
        buttonCancel.addActionListener(e -> onCancel());
    }

    private void addCancelActionForWindow() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });
    }

    private void addCancelActionForEsc() {
        contentPanel.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    @SuppressWarnings("PMD.AccessorMethodGeneration")
    private void setAutocompleteListenerForAttributeCodeField() {
        this.codeTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                updateDataPatchFileName();
            }
        });
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void fillEntityTypeComboBox() {
        for (final EavEntities entity : EavEntities.values()) {
            entityType.addItem(
                    new ComboBoxItemData(entity.name(), entity.name())
            );
        }

        for (final AttributeTypes typeValue : AttributeTypes.values()) {
            typeComboBox.addItem(
                    new ComboBoxItemData(typeValue.getType(), typeValue.getType())
            );
        }

        for (final AttributeInputs inputValue : AttributeInputs.values()) {
            inputComboBox.addItem(
                    new ComboBoxItemData(inputValue.getInput(), inputValue.getInput())
            );
        }

        for (final AttributeScopes globalValue : AttributeScopes.values()) {
            scopeComboBox.addItem(
                    new ComboBoxItemData(globalValue.getScope(), globalValue.name())
            );
        }
    }

    private void updateDataPatchFileName() {
        final String attributeCode = this.codeTextField.getText();

        if (attributeCode.isEmpty()) {
            dataPatchNameTextField.setText("");

            return;
        }

        final String dataPatchSuffix = "Add";
        final String dataPatchPrefix = "Attribute";

        final String[] attributeCodeParts = attributeCode.split("_");
        String fileName = "";

        for (final String fileNamePart : attributeCodeParts) {
            fileName = String.join("", fileName, StringUtils.capitalise(fileNamePart));
        }

        dataPatchNameTextField.setText(dataPatchSuffix + fileName + dataPatchPrefix);
    }

    /**
     * Open dialog.
     *
     * @param project   Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewEavAttributeDialog dialog = new NewEavAttributeDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOk() {
        if (!validateFormFields()) {
            return;
        }

        generateFile();
        setVisible(false);
    }

    private void generateFile() {
        new EavAttributeSetupPatchGenerator(
                getEntityData(),
                project
        ).generate(NewEavAttributeAction.ACTION_NAME, true);
    }

    private EavEntityDataInterface getEntityData() {
        EavEntityDataInterface entityData = null;
        if (getSelectedEntityType().equals(EavEntities.PRODUCT.name())) {
            entityData = populateProductEntityData(new ProductEntityData());
        }

        return entityData;
    }

    private ProductEntityData populateProductEntityData(final ProductEntityData productEntityData) {
        productEntityData.setModuleName(getModuleName());

        productEntityData.setDataPatchName(getDataPatchName());
        productEntityData.setGroup(getAttributeGroup());
        productEntityData.setCode(getAttributeCode());
        productEntityData.setType(getAttributeType());
        productEntityData.setLabel(getAttributeLabel());
        productEntityData.setInput(getAttributeInput());
        productEntityData.setScope(getAttributeScope());
        productEntityData.setSortOrder(getAttributeSortOrder());
        productEntityData.setRequired(isAttributeRequired());
        productEntityData.setUsedInGrid(isAttributeUsedInGrid());
        productEntityData.setVisibleInGrid(isAttributeVisibleOnGrid());
        productEntityData.setFilterableInGrid(isAttributeFilterableInGrid());
        productEntityData.setVisible(isAttributeVisible());
        productEntityData.setHtmlAllowedOnFront(isAttributeHtmlAllowedOnFront());
        productEntityData.setVisibleOnFront(isAttributeVisibleOnFront());

        return productEntityData;
    }

    private boolean isAttributeVisibleOnFront() {
        return visibleOnFrontCheckBox.isSelected();
    }

    private boolean isAttributeHtmlAllowedOnFront() {
        return htmlAllowedOnCheckBox.isSelected();
    }

    private boolean isAttributeVisible() {
        return visibleCheckBox.isSelected();
    }

    private boolean isAttributeFilterableInGrid() {
        return filterableInGridCheckBox.isSelected();
    }

    private boolean isAttributeVisibleOnGrid() {
        return visibleInGridCheckBox.isSelected();
    }

    private boolean isAttributeUsedInGrid() {
        return usedInGridGridCheckBox.isSelected();
    }

    private boolean isAttributeRequired() {
        return requiredCheckBox.isSelected();
    }

    private int getAttributeSortOrder() {
        return Integer.parseInt(sortOrderTextField.getText().trim());
    }

    private String getAttributeScope() {
        final ComboBoxItemData selectedScope = (ComboBoxItemData) scopeComboBox.getSelectedItem();

        return selectedScope.getKey().trim();
    }

    private String getAttributeCode() {
        return codeTextField.getText().trim();
    }

    private String getSelectedEntityType() {
        return entityType.getSelectedItem().toString().trim();
    }

    private String getAttributeLabel() {
        return labelTextField.getText().trim();
    }

    private String getAttributeInput() {
        return inputComboBox.getSelectedItem().toString().trim();
    }

    private String getAttributeGroup() {
        return groupTextField.getText().trim();
    }

    private String getDataPatchName() {
        return dataPatchNameTextField.getText().trim();
    }

    private String getAttributeType() {
        return typeComboBox.getSelectedItem().toString().trim();
    }

    private String getModuleName() {
        return moduleName;
    }
}
