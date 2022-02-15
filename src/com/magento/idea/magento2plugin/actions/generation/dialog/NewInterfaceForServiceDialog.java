/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.lang.actions.PhpNamedElementNode;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.NewWebApiInterfaceAction;
import com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.php.WebApiInterfaceGenerator;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports"})
public class NewInterfaceForServiceDialog extends AbstractDialog {

    private static final String SERVICE_METHODS = "Covered Methods";
    private static final String NAME_FIELD = "API Name";
    private static final String DESCRIPTION_FIELD = "API Description";

    private final @NotNull Project project;
    private final String moduleName;
    private final PhpClass phpClass;
    private final List<Method> serviceClassMethods;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton chooseMethodsButton;
    private JTextField serviceClassField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, SERVICE_METHODS})
    private JTextField serviceMethodsField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, NAME_FIELD})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS, message = {PhpClassRule.MESSAGE, NAME_FIELD})
    private JTextField nameField;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DESCRIPTION_FIELD})
    private JTextArea descriptionField;

    // labels
    private JLabel serviceClassFieldLabel;//NOPMD
    private JLabel serviceMethodsFieldLabel;//NOPMD
    private JLabel serviceMethodsFieldErrorMessage;//NOPMD
    private JLabel descriptionFieldLabel;//NOPMD
    private JLabel nameFieldLabel;//NOPMD
    private JLabel nameFieldErrorMessage;//NOPMD
    private JLabel descriptionFieldErrorMessage;//NOPMD

    /**
     * New Web API Interface for the php class.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param phpClass PhpClass
     */
    public NewInterfaceForServiceDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
            final @NotNull PhpClass phpClass
    ) {
        super();

        this.project = project;
        this.phpClass = phpClass;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        serviceClassMethods = PhpTypeMetadataParserUtil.getPublicMethods(phpClass);

        setContentPane(contentPane);
        setModal(true);
        setTitle(NewWebApiInterfaceAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());
        chooseMethodsButton.addActionListener(event -> openMethodChooser());

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

        fillPredefinedValuesAndDisableInputs();

        addComponentListener(new FocusOnAFieldListener(() -> nameField.requestFocusInWindow()));
    }

    /**
     * Open New Web API interface for the php class dialog window.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param phpClass PhpClass
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
            final @NotNull PhpClass phpClass
    ) {
        final NewInterfaceForServiceDialog dialog =
                new NewInterfaceForServiceDialog(project, directory, phpClass);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Fill predefined values and disable corresponding inputs.
     */
    private void fillPredefinedValuesAndDisableInputs() {
        serviceClassField.setText(phpClass.getPresentableFQN());
        serviceClassField.setEnabled(false);
        serviceClassField.setEditable(false);
        final int singleClassMethod = 1;

        if (serviceClassMethods.isEmpty() || serviceClassMethods.size() == singleClassMethod) {
            chooseMethodsButton.setEnabled(false);
            chooseMethodsButton.removeActionListener(event -> openMethodChooser());
        }

        if (serviceClassMethods.size() == singleClassMethod) {
            serviceMethodsField.setText(serviceClassMethods.get(0).getName());
        }
        serviceMethodsField.setEnabled(false);
        serviceMethodsField.setEditable(false);

        nameField.setText(PhpTypeMetadataParserUtil.getName(phpClass).concat("Interface"));
        descriptionField.setText(PhpTypeMetadataParserUtil.getShortDescription(phpClass));
    }

    /**
     * Fire generation process if all fields are valid.
     */
    private void onOK() {
        if (validateFormFields()) {
            final WebApiInterfaceData data = getDialogDataObject();

            new WebApiInterfaceGenerator(
                    data,
                    project
            ).generate(NewWebApiInterfaceAction.ACTION_NAME, true);
        }
        exit();
    }

    /**
     * Get dialog DTO.
     *
     * @return WebApiInterfaceData
     */
    private WebApiInterfaceData getDialogDataObject() {
        final List<String> chosenMethods = new LinkedList<>(
                Arrays.asList(serviceMethodsField.getText().split(", "))
        );
        final List<Method> methodList = new LinkedList<>();

        for (final Method method : serviceClassMethods) {
            if (chosenMethods.contains(method.getName())) {
                methodList.add(method);
            }
        }

        return new WebApiInterfaceData(
                moduleName,
                PhpTypeMetadataParserUtil.getFqn(phpClass),
                nameField.getText().trim(),
                descriptionField.getText().trim(),
                methodList
        );
    }

    /**
     * Open service methods chooser.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void openMethodChooser() {
        final List<PhpNamedElementNode> methodsNodes = new LinkedList<>();

        for (final Method method : serviceClassMethods) {
            methodsNodes.add(new PhpNamedElementNode(method));
        }

        final WebApiInterfaceMethodsChooser methodsChooser =
                new WebApiInterfaceMethodsChooser(
                        methodsNodes.toArray(new PhpNamedElementNode[0]),
                        project
                );

        methodsChooser.setTitle("Choose Methods");
        methodsChooser.setCopyJavadocVisible(false);
        methodsChooser.show();

        final List<PhpNamedElementNode> selectedMethods = methodsChooser.getSelectedElements();

        if (selectedMethods == null) {
            return;
        }
        final List<String> methodsNames = new LinkedList<>();

        for (final PhpNamedElementNode methodNode : selectedMethods) {
            methodsNames.add(((Method) methodNode.getPsiElement()).getName());
        }

        serviceMethodsField.setText(String.join(", ", methodsNames));
    }

    /**
     * Implementation for the service method chooser.
     */
    private static class WebApiInterfaceMethodsChooser extends MemberChooser<PhpNamedElementNode> {

        /**
         * Constructor for the service method chooser.
         *
         * @param nodes PhpNamedElementNode
         * @param project Project
         */
        protected WebApiInterfaceMethodsChooser(
                final @NotNull PhpNamedElementNode[] nodes,
                final @NotNull Project project
        ) {
            super(nodes, false, true, project, false);
        }
    }
}
