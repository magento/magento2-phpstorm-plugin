/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorTextField;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpCompletionUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.magento.idea.magento2plugin.actions.generation.InjectConstructorArgumentAction;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.data.xml.DiArgumentData;
import com.magento.idea.magento2plugin.actions.generation.data.xml.DiArrayValueData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.ExtendedNumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.code.ArgumentInjectionGenerator;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.DiArgumentType;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.UnusedPrivateField",
        "PMD.ExcessiveImports",
        "PMD.AvoidInstantiatingObjectsInLoops"
})
public class NewArgumentInjectionDialog extends AbstractDialog {

    private static final String TARGET_AREA = "Target Area";
    private static final String TARGET_MODULE = "Target Module";
    private static final String ARGUMENT_TYPE = "Argument Type";
    private static final String ARGUMENT_VALUE = "Argument Value";
    private static final String TYPE_VALUE = "Class/Interface";
    private static final String CONST_VALUE = "Target Constant";

    private final @NotNull Project project;
    private final PhpClass targetClass;
    private final Parameter targetParameter;

    private JPanel contentPane;
    private JButton buttonCancel;
    private JButton buttonOK;
    private JTextField targetClassField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, TARGET_MODULE})
    private FilteredComboBox targetModule;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, TARGET_AREA})
    private JComboBox<ComboBoxItemData> targetArea;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, ARGUMENT_TYPE})
    private JComboBox<ComboBoxItemData> argumentType;

    private JTextField targetArgument;

    private JPanel objectValuePane;
    private JRadioButton noneRB;
    private JRadioButton proxyRB;
    private JRadioButton factoryRB;
    private EditorTextField objectValue;
    private String customObjectValue;

    private JPanel stringValuePane;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, ARGUMENT_VALUE})
    private JTextField stringValue;

    private JPanel booleanValuePane;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, ARGUMENT_VALUE})
    private JComboBox<ComboBoxItemData> booleanValue;

    private JPanel numberValuePane;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, ARGUMENT_VALUE})
    @FieldValidation(rule = RuleRegistry.EXTENDED_NUMERIC,
            message = {ExtendedNumericRule.MESSAGE, ARGUMENT_VALUE})
    private JTextField numberValue;

    private JPanel initParameterValuePane;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, TYPE_VALUE})
    private EditorTextField initParameterTypeValue;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, CONST_VALUE})
    private JComboBox<ComboBoxItemData> initParameterConstValue;

    private JPanel constantValuePane;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, TYPE_VALUE})
    private EditorTextField constantTypeValue;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, CONST_VALUE})
    private JComboBox<ComboBoxItemData> constantValue;

    private JPanel arrayValuePane;
    private JComboBox<ComboBoxItemData> subArrayKey;
    private JButton addArrayValueBtn;
    private JTextArea arrayView;

    private final DiArrayValueData arrayValues;

    // labels
    private JLabel argumentTypeLabel;//NOPMD
    private JLabel targetModuleLabel;//NOPMD
    private JLabel targetAreaLabel;//NOPMD
    private JLabel targetClassLabel;//NOPMD
    private JLabel targetArgumentLabel;//NOPMD
    private JLabel customObjectValueLabel;//NOPMD
    private JLabel stringValueLabel;//NOPMD
    private JLabel booleanValueLabel;//NOPMD
    private JLabel numberValueLabel;//NOPMD
    private JLabel initParameterTypeValueLabel;//NOPMD
    private JLabel initParameterConstValueLabel;//NOPMD
    private JLabel constantTypeValueLabel;//NOPMD
    private JLabel constantValueLabel;//NOPMD
    private JLabel subArrayKeyLabel;//NOPMD

    /**
     * New argument injection dialog constructor.
     *
     * @param project Project
     * @param targetClass PhpClass
     * @param parameter Parameter
     */
    @SuppressWarnings({
            "PMD.AccessorMethodGeneration",
            "PMD.ExcessiveMethodLength",
            "PMD.CognitiveComplexity",
            "PMD.CyclomaticComplexity",
    })
    public NewArgumentInjectionDialog(
            final @NotNull Project project,
            final @NotNull PhpClass targetClass,
            final @NotNull Parameter parameter
    ) {
        super();

        this.project = project;
        this.targetClass = targetClass;
        targetParameter = parameter;
        arrayValues = new DiArrayValueData();

        setContentPane(contentPane);
        setModal(true);
        setTitle(InjectConstructorArgumentAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

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
        contentPane.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(new FocusOnAFieldListener(() -> targetModule.requestFocusInWindow()));

        targetClassField.setText(targetClass.getPresentableFQN());
        targetArgument.setText(parameter.getName());

        // make all value panes invisible
        objectValuePane.setVisible(false);
        stringValuePane.setVisible(false);
        booleanValuePane.setVisible(false);
        numberValuePane.setVisible(false);
        initParameterValuePane.setVisible(false);
        constantValuePane.setVisible(false);
        arrayValuePane.setVisible(false);

        subArrayKeyLabel.setVisible(false);
        subArrayKey.setVisible(false);

        objectValue.setEnabled(false);

        argumentType.addItemListener(event -> {
            if (!(event.getItem() instanceof ComboBoxItemData)) {
                return;
            }
            final ComboBoxItemData selectedItem = (ComboBoxItemData) event.getItem();
            changeViewBySelectedArgumentType(selectedItem.getKey());
        });

        final ActionListener objectTypeGroupActionListener = event -> {
            if (!(argumentType.getSelectedItem() instanceof ComboBoxItemData)) {
                return;
            }
            final ComboBoxItemData item = (ComboBoxItemData) argumentType.getSelectedItem();
            changeViewBySelectedArgumentType(item.getKey());
        };
        noneRB.addActionListener(objectTypeGroupActionListener);
        proxyRB.addActionListener(objectTypeGroupActionListener);
        factoryRB.addActionListener(objectTypeGroupActionListener);

        final Disposable disposable = new Disposable() {
            @Override
            public @NonNls String toString() {
                return NewArgumentInjectionDialog.this.toString();
            }

            @Override
            public void dispose() {
                NewArgumentInjectionDialog.this.dispose();
            }
        };

        PhpCompletionUtil.installClassCompletion(
                objectValue,
                "",
                disposable,
                (clazz) -> !clazz.isFinal()
                        && !PhpCompletionUtil.hasNamespace(clazz, "\\___PHPSTORM_HELPERS")
        );

        objectValue.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(final @NotNull DocumentEvent event) {
                if (noneRB.isSelected()) {
                    customObjectValue = objectValue.getText().trim();
                }
            }
        });

        PhpCompletionUtil.installClassCompletion(
                initParameterTypeValue,
                "",
                disposable,
                (clazz) -> !clazz.isFinal()
                        && !PhpCompletionUtil.hasNamespace(clazz, "\\___PHPSTORM_HELPERS")
        );

        initParameterTypeValue.addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void documentChanged(final @NotNull DocumentEvent event) {
                        populateInitParameterTypeConstants();
                    }
                }
        );

        PhpCompletionUtil.installClassCompletion(
                constantTypeValue,
                "",
                disposable,
                (clazz) -> !clazz.isFinal()
                        && !PhpCompletionUtil.hasNamespace(clazz, "\\___PHPSTORM_HELPERS")
        );

        constantTypeValue.addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void documentChanged(final @NotNull DocumentEvent event) {
                        populateConstantTypeConstants();
                    }
                }
        );

        addArrayValueBtn.addActionListener(event -> {
            final DiArrayValueData requestedArrayValues = new DiArrayValueData();
            GatherArrayValuesDialog.open(project, requestedArrayValues);

            if (!requestedArrayValues.getItems().isEmpty()) {
                if (arrayValues.getItems().isEmpty()) {
                    arrayValues.setItems(requestedArrayValues.getItems());
                } else {
                    if (subArrayKey.getSelectedItem() == null) {
                        return;
                    }
                    final String subArrayName = ((ComboBoxItemData) subArrayKey
                            .getSelectedItem()).getKey();

                    if (subArrayName.isEmpty()) {
                        arrayValues.setItems(requestedArrayValues.getItems());
                    } else {
                        final DiArrayValueData.DiArrayItemData parentItem = arrayValues
                                .getItemByPath(
                                        subArrayName
                                );
                        if (parentItem != null) {
                            parentItem.setChildren(requestedArrayValues);
                        }
                    }
                }
                populateSubArrayKeyCombobox();
                final String arrayRepresentation = arrayValues.toString();

                if (!arrayRepresentation.isEmpty()) {
                    arrayView.setText(arrayRepresentation);
                }
            }
        });
        guessTargetType();
    }

    /**
     * Open a new argument injection dialog.
     *
     * @param project Project
     * @param targetClass PhpClass
     * @param parameter Parameter
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PhpClass targetClass,
            final @NotNull Parameter parameter
    ) {
        final NewArgumentInjectionDialog dialog =
                new NewArgumentInjectionDialog(project, targetClass, parameter);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Fire generation process if all fields are valid.
     */
    private void onOK() {
        if (validateFormFields()) {
            final DiArgumentData data = getDialogDataObject();

            if (data == null) {
                return;
            }
            final ArgumentInjectionGenerator generator = new ArgumentInjectionGenerator(
                    data,
                    project
            );

            final PsiFile generatedFile = generator.generate(
                    InjectConstructorArgumentAction.ACTION_NAME,
                    true
            );

            if (generatedFile == null) {
                if (generator.getGenerationErrorMessage() == null) {
                    showErrorMessage(
                            new ValidatorBundle().message(
                                    "validator.file.cantBeCreated",
                                    "DI XML file"
                            )
                    );
                } else {
                    showErrorMessage(
                            new ValidatorBundle().message(
                                    "validator.file.cantBeCreatedWithException",
                                    "DI XML file",
                                    generator.getGenerationErrorMessage()
                            )
                    );
                }
            }
            exit();
        }
    }

    /**
     * Create custom components and fill their entries.
     */
    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        targetModule = new FilteredComboBox(new ModuleIndex(project).getEditableModuleNames());
        targetArea = new ComboBox<>();
        argumentType = new ComboBox<>();
        booleanValue = new ComboBox<>();
        initParameterConstValue = new ComboBox<>();
        constantValue = new ComboBox<>();
        subArrayKey = new ComboBox<>();

        for (final Areas area : Areas.values()) {
            targetArea.addItem(new ComboBoxItemData(area.toString(), area.toString()));
        }

        argumentType.addItem(
                new ComboBoxItemData("", " --- Select Type --- ")
        );

        for (final DiArgumentType type : DiArgumentType.values()) {
            argumentType.addItem(
                    new ComboBoxItemData(type.getArgumentType(), type.getArgumentType())
            );
        }
        objectValue = new EditorTextField("", project, FileTypes.PLAIN_TEXT);

        booleanValue.addItem(new ComboBoxItemData("", " --- Select Value --- "));
        booleanValue.addItem(new ComboBoxItemData("false", "False"));
        booleanValue.addItem(new ComboBoxItemData("true", "True"));
        final String selectConstantText = " --- Select Constant --- ";

        initParameterConstValue.addItem(new ComboBoxItemData("", selectConstantText));
        constantValue.addItem(new ComboBoxItemData("", selectConstantText));

        initParameterTypeValue = new EditorTextField("", project, FileTypes.PLAIN_TEXT);
        constantTypeValue = new EditorTextField("", project, FileTypes.PLAIN_TEXT);
    }

    private void populateInitParameterTypeConstants() {
        final String type = initParameterTypeValue.getText().trim();

        if (type.isEmpty()) {
            return;
        }
        final List<String> constants = getConstantsNames(type);

        if (constants.isEmpty()) {
            return;
        }
        initParameterConstValue.removeAllItems();
        initParameterConstValue.addItem(new ComboBoxItemData("", " --- Select Constant --- "));

        for (final String constantName : constants) {
            initParameterConstValue.addItem(new ComboBoxItemData(constantName, constantName));
        }
    }

    private void populateConstantTypeConstants() {
        final String type = constantTypeValue.getText().trim();

        if (type.isEmpty()) {
            return;
        }
        final List<String> constants = getConstantsNames(type);

        if (constants.isEmpty()) {
            return;
        }
        constantValue.removeAllItems();
        constantValue.addItem(new ComboBoxItemData("", " --- Select Constant --- "));

        for (final String constantName : constants) {
            constantValue.addItem(new ComboBoxItemData(constantName, constantName));
        }
    }

    private List<String> getConstantsNames(final @NotNull String fqn) {
        final List<String> result = new ArrayList<>();
        final PhpIndex phpIndex = PhpIndex.getInstance(project);

        final Collection<PhpClass> interfaces = phpIndex.getInterfacesByFQN(fqn);
        final Collection<PhpClass> classes = phpIndex.getClassesByFQN(fqn);
        PhpClass clazz = null;

        if (!interfaces.isEmpty()) { // NOPMD
            clazz = interfaces.iterator().next();
        } else if (!classes.isEmpty()) {
            clazz = classes.iterator().next();
        }

        if (clazz == null) {
            return result;
        }

        for (final Field field : clazz.getOwnFields()) {
            if (!(field instanceof ClassConstImpl)) {
                continue;
            }
            result.add(field.getName());
        }

        return result;
    }

    private void populateSubArrayKeyCombobox() {
        if (arrayValues.getItems().isEmpty()) {
            subArrayKeyLabel.setVisible(false);
            subArrayKey.setVisible(false);
            return;
        }
        subArrayKey.removeAllItems();
        subArrayKey.addItem(new ComboBoxItemData("", " --- Top Array --- "));
        populateSubArrayKeyCombobox(arrayValues, "");

        if (subArrayKey.getItemCount() > 1) { // NOPMD
            subArrayKeyLabel.setVisible(true);
            subArrayKey.setVisible(true);
        }
    }

    private void populateSubArrayKeyCombobox(final DiArrayValueData data, final String parentKey) {
        for (final DiArrayValueData.DiArrayItemData item : data.getItems()) {
            if (item.getType().equals(DiArgumentType.ARRAY)) {
                final String key = parentKey.isEmpty()
                        ? item.getName()
                        : parentKey + ":" + item.getName();
                final String value = parentKey.isEmpty()
                        ? item.getName()
                        : parentKey.replaceAll(":", " -> ") + " -> " + item.getName();

                subArrayKey.addItem(new ComboBoxItemData(key, value));

                if (item.hasChildren()) {
                    populateSubArrayKeyCombobox(item.getChildren(), key);
                }
            }
        }
    }

    private void changeViewBySelectedArgumentType(final String itemValue) {
        // make target value pane visible
        objectValuePane.setVisible(itemValue.equals(DiArgumentType.OBJECT.getArgumentType()));
        stringValuePane.setVisible(itemValue.equals(DiArgumentType.STRING.getArgumentType()));
        booleanValuePane.setVisible(itemValue.equals(DiArgumentType.BOOLEAN.getArgumentType()));
        numberValuePane.setVisible(itemValue.equals(DiArgumentType.NUMBER.getArgumentType()));
        initParameterValuePane.setVisible(
                itemValue.equals(DiArgumentType.INIT_PARAMETER.getArgumentType())
        );
        constantValuePane.setVisible(itemValue.equals(DiArgumentType.CONST.getArgumentType()));
        arrayValuePane.setVisible(itemValue.equals(DiArgumentType.ARRAY.getArgumentType()));

        if (itemValue.equals(DiArgumentType.OBJECT.getArgumentType())) {
            final String targetType = targetClass.getPresentableFQN();

            if (noneRB.isSelected()) {
                if (customObjectValue != null) {
                    objectValue.setText(customObjectValue);
                }
                objectValue.setEnabled(true);
            } else if (proxyRB.isSelected()) {
                objectValue.setEnabled(false);
                objectValue.setText(targetType.concat("\\Proxy"));
            } else if (factoryRB.isSelected()) {
                objectValue.setEnabled(false);
                objectValue.setText(targetType.concat("Factory"));
            }
        }
    }

    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity"})
    private @NotNull String getArgumentValue() {
        final ComboBoxItemData item = (ComboBoxItemData) argumentType.getSelectedItem();

        if (item == null) {
            return "";
        }

        if (item.getKey().equals(DiArgumentType.OBJECT.getArgumentType())) {
            return objectValue.getText().trim();
        } else if (item.getKey().equals(DiArgumentType.STRING.getArgumentType())) {
            return stringValue.getText().trim();
        } else if (item.getKey().equals(DiArgumentType.BOOLEAN.getArgumentType())) {
            final ComboBoxItemData booleanValueItem =
                    (ComboBoxItemData) booleanValue.getSelectedItem();

            if (booleanValueItem == null) {
                return "";
            }
            return booleanValueItem.getKey();
        } else if (item.getKey().equals(DiArgumentType.NUMBER.getArgumentType())) {
            return numberValue.getText().trim();
        } else if (item.getKey().equals(DiArgumentType.INIT_PARAMETER.getArgumentType())) {
            final ComboBoxItemData initParamValueItem =
                    (ComboBoxItemData) initParameterConstValue.getSelectedItem();

            if (initParamValueItem == null) {
                return "";
            }
            final String initParamType = initParameterTypeValue.getText().trim();

            return initParamType.concat("::").concat(initParamValueItem.getKey());
        } else if (item.getKey().equals(DiArgumentType.CONST.getArgumentType())) {
            final ComboBoxItemData constValueItem =
                    (ComboBoxItemData) constantValue.getSelectedItem();

            if (constValueItem == null) {
                return "";
            }
            final String constType = constantTypeValue.getText().trim();

            return constType.concat("::").concat(constValueItem.getKey());
        } else if (item.getKey().equals(DiArgumentType.NULL.getArgumentType())) {
            return "";
        } else if (item.getKey().equals(DiArgumentType.ARRAY.getArgumentType())) {
            return arrayValues.convertToXml(arrayValues);
        }

        return "";
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void guessTargetType() {
        final String mainType = PhpTypeMetadataParserUtil.getMainType(targetParameter);

        if (mainType == null) {
            return;
        }
        String targetDiType = "";

        if (Arrays.asList("int", "float").contains(mainType)) {
            targetDiType = DiArgumentType.NUMBER.getArgumentType();
        } else if (DiArgumentType.STRING.getArgumentType().equals(mainType)) {
            targetDiType = DiArgumentType.STRING.getArgumentType();
        } else if ("bool".equals(mainType)) {
            targetDiType = DiArgumentType.BOOLEAN.getArgumentType();
        } else if (PhpLangUtil.isFqn(mainType)) {
            targetDiType = DiArgumentType.OBJECT.getArgumentType();
        } else if ("array".equals(mainType)) {
            targetDiType = DiArgumentType.ARRAY.getArgumentType();
        }

        if (targetDiType.isEmpty()) {
            return;
        }

        for (int i = 0; i < argumentType.getItemCount(); i++) {
            if (targetDiType.equals(argumentType.getItemAt(i).getKey())) {
                argumentType.setSelectedIndex(i);
                break;
            }
        }
    }

    private DiArgumentData getDialogDataObject() {
        if (targetArea.getSelectedItem() == null) {
            showErrorMessage(new ValidatorBundle().message(NotEmptyRule.MESSAGE, TARGET_AREA));
            return null;
        }

        if (argumentType.getSelectedItem() == null) {
            showErrorMessage(new ValidatorBundle().message(NotEmptyRule.MESSAGE, ARGUMENT_TYPE));
            return null;
        }
        final String argumentTypeValue = ((ComboBoxItemData) argumentType
                .getSelectedItem()).getKey().trim();

        if (argumentTypeValue.isEmpty()) {
            showErrorMessage(new ValidatorBundle().message(NotEmptyRule.MESSAGE, ARGUMENT_TYPE));
            return null;
        }

        final String argValue = getArgumentValue();

        if (argValue.isEmpty()
                && !argumentTypeValue.equals(DiArgumentType.NULL.getArgumentType())) {
            showErrorMessage(new ValidatorBundle().message(NotEmptyRule.MESSAGE, ARGUMENT_VALUE));
            return null;
        }

        return new DiArgumentData(
                targetModule.getSelectedItem().toString().trim(),
                targetClassField.getText().trim(),
                targetArgument.getText().trim(),
                Areas.getAreaByString(targetArea.getSelectedItem().toString().trim()),
                DiArgumentType.getByValue(argumentTypeValue),
                argValue
        );
    }
}
