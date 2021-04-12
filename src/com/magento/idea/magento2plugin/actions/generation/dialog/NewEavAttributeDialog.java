/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.DocumentAdapter;
import com.magento.idea.magento2plugin.actions.generation.NewEavAttributeAction;
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.EavAttributeInputItemListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.EavAttributeSetupPatchGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.SourceModelGenerator;
import com.magento.idea.magento2plugin.magento.files.SourceModelFile;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScope;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeType;
import com.magento.idea.magento2plugin.magento.packages.eav.EavEntity;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ExcessiveImports",
        "PMD.TooManyMethods",
        "PMD.UnusedPrivateField"
})
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
    private JComboBox<ComboBoxItemData> sourceComboBox;
    private JCheckBox requiredCheckBox;
    private JCheckBox usedInGridGridCheckBox;
    private JCheckBox visibleInGridCheckBox;
    private JCheckBox filterableInGridCheckBox;
    private JCheckBox visibleCheckBox;
    private JCheckBox htmlAllowedOnCheckBox;
    private JCheckBox visibleOnFrontCheckBox;
    private JPanel sourcePanel;
    private JPanel customSourceModelPanel;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Source Model Directory"})
    private JTextField sourceModelDirectoryTexField;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, "Source Model Name"})
    private JTextField sourceModelNameTexField;
    private final Project project;
    private final SourceModelData sourceModelData;

    /**
     * Constructor.
     *
     * @param project   Project
     * @param directory PsiDirectory
     */
    public NewEavAttributeDialog(final Project project, final PsiDirectory directory) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.sourceModelData = new SourceModelData();

        setPanelConfiguration();
        addActionListenersForButtons();
        addCancelActionForWindow();
        addCancelActionForEsc();
        setAutocompleteListenerForAttributeCodeField();
        fillEntityComboBoxes();
        addDependBetweenInputAndSourceModel();
        setDefaultSources();
    }

    @SuppressWarnings("PMD.AccessorMethodGeneration")
    private void addDependBetweenInputAndSourceModel() {
        inputComboBox.addItemListener(
                new EavAttributeInputItemListener(sourceComboBox)
        );

        sourceComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent itemEvent) {
                final String selectedSource = itemEvent.getItem().toString();

                if (selectedSource.equals(AttributeSourceModel.GENERATE_SOURCE.getSource())) {
                    customSourceModelPanel.setVisible(true);
                    sourceModelData.setModuleName(moduleName);

                    if (sourceModelDirectoryTexField.getText().trim().isEmpty()) {
                        sourceModelDirectoryTexField.setText(sourceModelData.getDirectory());
                    }
                } else {
                    customSourceModelPanel.setVisible(false);
                }
            }
        });

        sourceModelDirectoryTexField.setText(sourceModelData.getDirectory());
    }

    private void setDefaultSources() {
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
                final String attributeCode = codeTextField.getText();
                updateDataPatchFileName(attributeCode);
                updateSourceModelName(attributeCode);
            }
        });
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void fillEntityComboBoxes() {
        for (final EavEntity entity : EavEntity.values()) {
            entityType.addItem(
                    new ComboBoxItemData(entity.name(), entity.name())
            );
        }

        for (final AttributeType typeValue : AttributeType.values()) {
            typeComboBox.addItem(
                    new ComboBoxItemData(typeValue.getType(), typeValue.getType())
            );
        }

        for (final AttributeInput inputValue : AttributeInput.values()) {
            inputComboBox.addItem(
                    new ComboBoxItemData(inputValue.getInput(), inputValue.getInput())
            );
        }

        for (final AttributeScope globalValue : AttributeScope.values()) {
            scopeComboBox.addItem(
                    new ComboBoxItemData(globalValue.getScope(), globalValue.name())
            );
        }
    }

    private void updateDataPatchFileName(final String attributeCode) {
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

    private void updateSourceModelName(final String attributeCode) {

        if (attributeCode.isEmpty()) {
            sourceModelNameTexField.setText("");

            return;
        }

        final String[] attributeCodeParts = attributeCode.split("_");
        final StringBuilder sourceModelClassName = new StringBuilder();

        for (final String codePart : attributeCodeParts) {
            sourceModelClassName.append(StringUtils.capitalise(codePart));
        }

        sourceModelNameTexField.setText(sourceModelClassName.toString());
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

        generateSourceModelFile();
        generateDataPatchFile();

        setVisible(false);
    }

    private void generateSourceModelFile() {
        final ComboBoxItemData selectedSource = (ComboBoxItemData) sourceComboBox.getSelectedItem();

        if (selectedSource == null
                || !selectedSource.getText().equals(
                        AttributeSourceModel.GENERATE_SOURCE.getSource()
        )) {
            return;
        }

        sourceModelData.setModuleName(moduleName);
        sourceModelData.setClassName(sourceModelNameTexField.getText().trim());
        sourceModelData.setDirectory(sourceModelDirectoryTexField.getText().trim());

        new SourceModelGenerator(sourceModelData, project, true)
                .generate(NewEavAttributeAction.ACTION_NAME, false);
    }

    private void generateDataPatchFile() {
        new EavAttributeSetupPatchGenerator(
                populateProductEntityData(new ProductEntityData()),
                project,
                true
        ).generate(NewEavAttributeAction.ACTION_NAME, true);
    }

    private ProductEntityData populateProductEntityData(final ProductEntityData productEntityData) {
        productEntityData.setModuleName(moduleName);

        productEntityData.setDataPatchName(dataPatchNameTextField.getText().trim());
        productEntityData.setGroup(groupTextField.getText().trim());
        productEntityData.setCode(codeTextField.getText().trim());
        productEntityData.setLabel(labelTextField.getText().trim());
        productEntityData.setSortOrder(Integer.parseInt(sortOrderTextField.getText().trim()));
        productEntityData.setRequired(requiredCheckBox.isSelected());
        productEntityData.setUsedInGrid(usedInGridGridCheckBox.isSelected());
        productEntityData.setVisibleInGrid(visibleInGridCheckBox.isSelected());
        productEntityData.setFilterableInGrid(filterableInGridCheckBox.isSelected());
        productEntityData.setVisible(visibleCheckBox.isSelected());
        productEntityData.setHtmlAllowedOnFront(htmlAllowedOnCheckBox.isSelected());
        productEntityData.setVisibleOnFront(visibleOnFrontCheckBox.isSelected());
        productEntityData.setType(getAttributeType());
        productEntityData.setInput(getAttributeInput());
        productEntityData.setScope(getAttributeScope());
        productEntityData.setSource(getAttributeSource());

        return productEntityData;
    }

    private String getAttributeSource() {
        final ComboBoxItemData selectedItem = (ComboBoxItemData) sourceComboBox.getSelectedItem();

        if (selectedItem == null
                || selectedItem.getText().equals(
                        AttributeSourceModel.NULLABLE_SOURCE.getSource()
        )) {
            return null;
        }

        if (selectedItem.getText().equals(AttributeSourceModel.GENERATE_SOURCE.getSource())
                && sourceModelData != null) {

            return "\\" + new SourceModelFile(
                    sourceModelData.getModuleName(),
                    sourceModelData.getClassName(),
                    sourceModelData.getDirectory()
            ).getClassFqn();
        }

        return sourceComboBox.getSelectedItem().toString();
    }

    private String getAttributeScope() {
        final ComboBoxItemData selectedScope = (ComboBoxItemData) scopeComboBox.getSelectedItem();

        if (selectedScope != null) {
            selectedScope.getKey().trim();
        }

        return AttributeScope.GLOBAL.getScope();
    }

    private String getAttributeInput() {
        final ComboBoxItemData selectedAttributeInput =
                (ComboBoxItemData) inputComboBox.getSelectedItem();

        if (selectedAttributeInput != null) {
            return selectedAttributeInput.getText().trim();
        }

        return "";
    }

    private String getAttributeType() {
        final ComboBoxItemData selectedItem = (ComboBoxItemData) typeComboBox.getSelectedItem();

        if (selectedItem != null) {
            return selectedItem.getText();
        }

        return "";
    }
}
