/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.eavattribute;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.AbstractDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.AttributeSourcePanelComponentListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.AttributeSourceRelationsItemListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.DataPatchNameAdapter;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.EavAttributeInputItemListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.OptionsPanelVisibilityChangeListener;
import com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog.SourceModelNameAdapter;
import com.magento.idea.magento2plugin.actions.generation.generator.EavAttributeSetupPatchGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.SourceModelGenerator;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeType;
import com.magento.idea.magento2plugin.ui.table.TableGroupWrapper;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SuppressWarnings({"PMD.TooManyMethods"})
public abstract class EavAttributeDialog extends AbstractDialog {

    protected String moduleName;
    protected Project project;
    protected String actionName;
    protected TableGroupWrapper entityPropertiesTableGroupWrapper;
    protected SourceModelData sourceModelData;

    protected abstract EavEntityDataInterface getEavEntityData();

    protected abstract JPanel getContentPanel();

    protected abstract JButton getButtonOk();

    protected abstract JButton getButtonCancel();

    protected abstract JComboBox<ComboBoxItemData> getAttributeTypeCompoBox();

    protected abstract JComboBox<ComboBoxItemData> getAttributeInputComboBox();

    protected abstract JTable getOptionsTable();

    protected abstract JButton getNewOptionButton();

    protected abstract JComboBox<ComboBoxItemData> getAttributeSourceComboBox();

    protected abstract JTextField getAttributeSourceModelNameTexField();

    protected abstract JTextField getSourceModelDirectoryTexField();

    protected abstract JPanel getAttributeCustomSourceModelPanel();

    protected abstract JPanel getAttributeOptionsPanel();

    protected abstract JTextField getAttributeCodeTextField();

    protected abstract JTextField getDataPatchNameTextField();

    protected abstract JTextField getSourceModelNameTexField();

    protected abstract String getEntityName();

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param actionName String
     */
    public EavAttributeDialog(
            final Project project,
            final PsiDirectory directory,
            final String actionName
    ) {
        super();

        this.project = project;
        this.actionName = actionName;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.sourceModelData = new SourceModelData();
    }

    /**
     * Open dialog window.
     */
    public void open() {
        this.initBaseDialogState();
        pack();
        centerDialog(this);
        setVisible(true);
    }

    protected void initBaseDialogState() {
        this.setPanelConfiguration();
        this.fillAttributeTypeComboBox(getAttributeTypeCompoBox());
        this.fillAttributeInputComboBox(getAttributeInputComboBox());

        this.initPropertiesTable(
                new LinkedList<>(Arrays.asList(
                        "Value",
                        "Sort Order"
                )),
                getOptionsTable(),
                getNewOptionButton(),
                getDefaultColumnsValues(),
                getColumnsSources()
        );

        this.addActionListenersForOkButton(getButtonOk());
        this.addActionListenersForOkCancel(getButtonCancel());

        this.addCancelActionForWindow();
        this.addCancelActionForEsc();

        this.setAttributeInputComboBoxAction(getAttributeSourceComboBox());
        this.setSourceComboBoxAction(getAttributeSourceComboBox());

        this.setSourceModelPanelAction(
                getAttributeCustomSourceModelPanel(),
                getSourceModelDirectoryTexField()
        );
        this.addOptionPanelListener(
                getAttributeSourceComboBox(),
                getAttributeInputComboBox(),
                getAttributeOptionsPanel()
        );
        this.setDefaultSources(getAttributeSourceComboBox());
        this.setAutocompleteListenerForDataPathNameField(
                getAttributeCodeTextField(),
                getDataPatchNameTextField()
        );
        this.setAutocompleteListenerForSourceModelNameField(
                getAttributeCodeTextField(),
                getSourceModelNameTexField()
        );
    }

    protected void setPanelConfiguration() {
        setContentPane(this.getContentPanel());
        setModal(this.isModalWindow());
        getRootPane().setDefaultButton(this.getButtonOk());
    }

    protected boolean isModalWindow() {
        return true;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void fillAttributeTypeComboBox(final JComboBox<ComboBoxItemData> typeComboBox) {
        if (typeComboBox == null) {
            return;
        }

        for (final AttributeType typeValue : AttributeType.values()) {
            typeComboBox.addItem(
                    new ComboBoxItemData(typeValue.getType(), typeValue.getType())
            );
        }
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void fillAttributeInputComboBox(final JComboBox<ComboBoxItemData> inputComboBox) {
        if (inputComboBox == null) {
            return;
        }

        for (final AttributeInput inputValue : AttributeInput.values()) {
            inputComboBox.addItem(
                    new ComboBoxItemData(inputValue.getInput(), inputValue.getInput())
            );
        }
    }

    protected void initPropertiesTable(
            final List<String> columns,
            final JTable optionsTable,
            final JButton newOptionButton,
            final Map<String, String> defaultColumnsValues,
            final Map<String, List<String>> columnsSources

    ) {
        // Initialize entity properties Table Group
        entityPropertiesTableGroupWrapper = new TableGroupWrapper(
                optionsTable,
                newOptionButton,
                columns,
                defaultColumnsValues,
                columnsSources
        );
        entityPropertiesTableGroupWrapper.initTableGroup();
    }

    protected Map<String, String> getDefaultColumnsValues() {
        return new HashMap<>();
    }

    protected Map<String, List<String>> getColumnsSources() {
        return new HashMap<>();
    }

    protected void addActionListenersForOkButton(final JButton okButton) {
        okButton.addActionListener(e -> onOk());
    }

    protected void addActionListenersForOkCancel(final JButton cancelButton) {
        cancelButton.addActionListener(e -> onCancel());
    }

    protected void onOk() {
        if (!validateFormFields()) {
            return;
        }

        generateSourceModelFile();
        generateDataPatchFile();

        setVisible(false);
    }

    protected void generateSourceModelFile() {
        final ComboBoxItemData selectedSource =
                (ComboBoxItemData) getAttributeSourceComboBox().getSelectedItem();

        if (selectedSource == null
                || !selectedSource.getText().equals(
                AttributeSourceModel.GENERATE_SOURCE.getSource()
        )) {
            return;
        }

        sourceModelData.setModuleName(moduleName);
        sourceModelData.setClassName(getAttributeSourceModelNameTexField().getText().trim());
        sourceModelData.setDirectory(getSourceModelDirectoryTexField().getText().trim());

        new SourceModelGenerator(sourceModelData, project, true)
                .generate(actionName, false);
    }

    protected void generateDataPatchFile() {
        new EavAttributeSetupPatchGenerator(
                getEavEntityData(),
                project,
                true
        ).generate(actionName, true);
    }

    protected void addCancelActionForWindow() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });
    }

    protected void addCancelActionForEsc() {
        getContentPanel().registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    @SuppressWarnings("PMD.AccessorMethodGeneration")
    protected void setAttributeInputComboBoxAction(
            final JComboBox<ComboBoxItemData> sourceComboBox
    ) {
        if (sourceComboBox == null) {
            return;
        }

        getAttributeInputComboBox().addItemListener(
                new EavAttributeInputItemListener(sourceComboBox)
        );
    }

    @SuppressWarnings("PMD.AccessorMethodGeneration")
    protected void setSourceComboBoxAction(final JComboBox<ComboBoxItemData> sourceComboBox) {
        if (sourceComboBox == null) {
            return;
        }

        sourceComboBox.addItemListener(
                new AttributeSourceRelationsItemListener(getAttributeCustomSourceModelPanel())
        );
    }

    @SuppressWarnings("PMD.AccessorMethodGeneration")
    protected void setSourceModelPanelAction(
            final JPanel attributeCustomSourceModelPanel,
            final JTextField sourceModelDirectoryTexField
    ) {
        if (attributeCustomSourceModelPanel == null || sourceModelDirectoryTexField == null) {
            return;
        }

        attributeCustomSourceModelPanel.addComponentListener(
                new AttributeSourcePanelComponentListener(sourceModelDirectoryTexField)
        );
    }

    @SuppressWarnings("PMD.AccessorMethodGeneration")
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

        attributeSourceComboBox.addItemListener(
                new OptionsPanelVisibilityChangeListener(
                        attributeOptionsPanel,
                        attributeInputComboBox
                )
        );
    }

    protected void setDefaultSources(final JComboBox<ComboBoxItemData> sourceComboBox) {
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

    @SuppressWarnings("PMD.AccessorMethodGeneration")
    protected void setAutocompleteListenerForDataPathNameField(
            final JTextField mainTextField,
            final JTextField dependentTextField

    ) {
        if (mainTextField == null || dependentTextField == null) {
            return;
        }

        mainTextField.getDocument()
                .addDocumentListener(new DataPatchNameAdapter(dependentTextField, getEntityName()));
    }

    @SuppressWarnings("PMD.AccessorMethodGeneration")
    protected void setAutocompleteListenerForSourceModelNameField(
            final JTextField mainTextField,
            final JTextField dependentTextField
    ) {
        if (mainTextField == null || dependentTextField == null) {
            return;
        }

        mainTextField.getDocument()
                .addDocumentListener(new SourceModelNameAdapter(dependentTextField));
    }
}
