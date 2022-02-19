/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.CreateAPluginAction;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.BoxNotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierWithColonRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginDiXmlGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.DataClass",
        "PMD.UnusedPrivateMethod",
        "PMD.ExcessiveImports"
})
public class CreateAPluginDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private Method targetMethod;
    private final PhpClass targetClass;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox pluginType;
    private JComboBox pluginArea;

    private static final String CLASS_NAME = "class name";
    private static final String DIRECTORY = "directory path";
    private static final String SORT_ORDER = "sort order";
    private static final String PLUGIN_NAME = "plugin name";
    private static final String TARGET_MODULE = "target module";
    private static final String TARGET_METHOD = "target method";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, TARGET_MODULE})
    @FieldValidation(rule = RuleRegistry.BOX_NOT_EMPTY,
            message = {BoxNotEmptyRule.MESSAGE, TARGET_MODULE})
    private FilteredComboBox pluginModule;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, TARGET_METHOD})
    @FieldValidation(rule = RuleRegistry.BOX_NOT_EMPTY,
            message = {BoxNotEmptyRule.MESSAGE, TARGET_METHOD})
    private FilteredComboBox targetMethodSelect;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CLASS_NAME})
    private JTextField pluginClassName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DIRECTORY})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, DIRECTORY})
    private JTextField pluginDirectory;

    @FieldValidation(rule = RuleRegistry.NUMERIC,
            message = {NumericRule.MESSAGE, SORT_ORDER})
    private JTextField pluginSortOrder;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, PLUGIN_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER_WITH_COLON,
            message = {IdentifierWithColonRule.MESSAGE, PLUGIN_NAME})
    private JTextField pluginName;

    private JLabel pluginDirectoryName;//NOPMD
    private JLabel selectPluginModule;//NOPMD
    private JLabel pluginTypeLabel;//NOPMD
    private JLabel pluginAreaLabel;//NOPMD
    private JLabel pluginNameLabel;//NOPMD
    private JLabel pluginClassNameLabel;//NOPMD
    private JLabel pluginSortOrderLabel;//NOPMD
    private JLabel targetMethodLabel;

    /**
     * Constructor.
     *
     * @param project Project
     * @param targetMethod Method
     * @param targetClass PhpClass
     */
    public CreateAPluginDialog(
            final @NotNull Project project,
            final Method targetMethod,
            final PhpClass targetClass
    ) {
        super();
        this.project = project;
        this.targetMethod = targetMethod;
        this.targetClass = targetClass;

        setContentPane(contentPane);
        setModal(true);
        setTitle(CreateAPluginAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        fillPluginTypeOptions();
        fillTargetAreaOptions();

        if (targetMethod != null) {
            this.targetMethodLabel.setVisible(false);
        }

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(new FocusOnAFieldListener(() -> pluginModule.requestFocusInWindow()));
    }

    private void fillPluginTypeOptions() {
        for (final Plugin.PluginType pluginPrefixType: Plugin.PluginType.values()) {
            pluginType.addItem(pluginPrefixType.toString());
        }
    }

    private void fillTargetAreaOptions() {
        for (final Areas area : Areas.values()) {
            pluginArea.addItem(area.toString());
        }
    }

    protected void onOK() {
        if (targetMethod == null) {
            targetMethod = getSelectedTargetMethod();
        }
        if (validateFormFields()) {
            new PluginClassGenerator(new PluginFileData(
                    getPluginDirectory(),
                    getPluginClassName(),
                    getPluginType(),
                    getPluginModule(),
                    targetClass,
                    targetMethod,
                    getPluginClassFqn(),
                    getNamespace()
            ), project).generate(CreateAPluginAction.ACTION_NAME, true);

            new PluginDiXmlGenerator(new PluginDiXmlData(
                    getPluginArea(),
                    getPluginModule(),
                    targetClass,
                    getPluginSortOrder(),
                    getPluginName(),
                    getPluginClassFqn()
            ), project).generate(CreateAPluginAction.ACTION_NAME);
        }
        exit();
    }

    public String getPluginName() {
        return this.pluginName.getText().trim();
    }

    public String getPluginSortOrder() {
        return this.pluginSortOrder.getText().trim();
    }

    public String getPluginClassName() {
        return this.pluginClassName.getText().trim();
    }

    public String getPluginDirectory() {
        return this.pluginDirectory.getText().trim();
    }

    public String getPluginArea() {
        return this.pluginArea.getSelectedItem().toString();
    }

    public String getPluginType() {
        return this.pluginType.getSelectedItem().toString();
    }

    public String getPluginModule() {
        return this.pluginModule.getSelectedItem().toString();
    }

    /**
     * Searches and returns a selected target method.
     *
     * @return Method target method
     */
    public Method getSelectedTargetMethod() {
        final String selectedMethodString = this.targetMethodSelect.getSelectedItem().toString();
        final List<Method> publicMethods = PhpTypeMetadataParserUtil.getPublicMethods(
                this.targetClass
        );
        for (final Method method: publicMethods) {
            if (method.getName().equals(selectedMethodString)) {
                return method;
            }
        }

        return null;
    }

    /**
     * Open an action dialog.
     *
     * @param project Project
     * @param targetMethod Method
     * @param targetClass PhpClass
     */
    public static void open(
            final @NotNull Project project,
            final Method targetMethod,
            final PhpClass targetClass
    ) {
        final CreateAPluginDialog dialog = new CreateAPluginDialog(
                project,
                targetMethod,
                targetClass
        );
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        final List<String> allModulesList = new ModuleIndex(project)
                .getEditableModuleNames();

        this.pluginModule = new FilteredComboBox(allModulesList);

        final List<Method> publicMethods
                = PhpTypeMetadataParserUtil.getPublicMethods(this.targetClass);
        final List<String> methodList = new ArrayList<>();
        for (final Method method: publicMethods) {
            methodList.add(method.getName());
        }

        this.targetMethodSelect = new FilteredComboBox(methodList);
        if (targetMethod != null) {
            this.targetMethodSelect.setVisible(false);
        }
    }

    private String getNamespace() {
        final String targetModule = getPluginModule();
        String namespace = targetModule.replace(
                Package.vendorModuleNameSeparator,
                Package.fqnSeparator
        );
        namespace = namespace.concat(Package.fqnSeparator);
        return namespace.concat(getPluginDirectory().replace(File.separator, Package.fqnSeparator));
    }

    private String getPluginClassFqn() {
        return getNamespace().concat(Package.fqnSeparator).concat(getPluginClassName());
    }
}
