/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.DocumentAdapter;
import com.magento.idea.magento2plugin.actions.generation.InjectAViewModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.ViewModelFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleViewModelClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.code.ClassArgumentInXmlConfigGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.packages.XsiTypes;
import com.magento.idea.magento2plugin.util.FirstLetterToLowercaseUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.ExcessiveImports"
})
public class InjectAViewModelDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private final XmlTag targetBlockTag;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private final CommonBundle commonBundle;
    private final ValidatorBundle validatorBundle;
    private JLabel inheritClassLabel;//NOPMD
    private JLabel viewModelDirectoryLabel;//NOPMD
    private JLabel viewModelClassNameLabel;//NOPMD
    private JLabel viewModelArgumentNameLabel;//NOPMD
    private static final String CLASS_NAME = "class name";
    private static final String DIRECTORY = "directory";
    private static final String ARGUMENT_NAME = "argument name";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CLASS_NAME})
    private JTextField viewModelClassName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DIRECTORY})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, DIRECTORY})
    private JTextField viewModelDirectory;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, ARGUMENT_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC_WITH_UNDERSCORE,
            message = {AlphanumericWithUnderscoreRule.MESSAGE, ARGUMENT_NAME})
    private JTextField viewModelArgumentName;

    /**
     * Constructor.
     *
     * @param project Project
     * @param targetBlockTag XmlTag
     */
    public InjectAViewModelDialog(
            final @NotNull Project project,
            final XmlTag targetBlockTag
    ) {
        super();

        this.project = project;
        this.targetBlockTag = targetBlockTag;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();

        this.viewModelClassName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                updateArgumentText();
            }
        });
        this.viewModelDirectory.setText("ViewModel");

        setContentPane(contentPane);
        setModal(true);
        setTitle(InjectAViewModelAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

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

        addComponentListener(
                new FocusOnAFieldListener(() -> viewModelClassName.requestFocusInWindow())
        );
    }

    protected void updateArgumentText() {
        final String classNameText = this.viewModelClassName.getText();
        this.viewModelArgumentName.setText(
                FirstLetterToLowercaseUtil.convert(classNameText)
        );
    }

    protected void onOK() {
        if (!validateFormFields()) {
            exit();
            return;
        }

        if (targetBlockTag.getContainingFile() == null
                || targetBlockTag.getContainingFile().getParent() == null) {
            return;
        }

        final String moduleName = GetModuleNameByDirectoryUtil.execute(
                targetBlockTag.getContainingFile().getParent(),
                project
        );
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                moduleName,
                getViewModelClassName(),
                getViewModelDirectory()
        );
        final PsiFile viewModel = new ModuleViewModelClassGenerator(new ViewModelFileData(
                getViewModelDirectory(),
                getViewModelClassName(),
                moduleName,
                namespaceBuilder.getNamespace()
        ), project).generate(InjectAViewModelAction.ACTION_NAME, true);
        if (viewModel == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.class.alreadyDeclared",
                    "ViewModel"
            );
            final String errorTitle = commonBundle.message("common.error");
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            exit();
            return;
        }

        new ClassArgumentInXmlConfigGenerator(
                        project,
                        this.getViewModelArgumentName(),
                        XsiTypes.object.toString(),
                        namespaceBuilder.getClassFqn()
        ).generate(targetBlockTag);

        exit();
    }

    public String getViewModelClassName() {
        return this.viewModelClassName.getText().trim();
    }

    public String getViewModelDirectory() {
        return this.viewModelDirectory.getText().trim();
    }

    public String getViewModelArgumentName() {
        return this.viewModelArgumentName.getText().trim();
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param targetXmlTag XmlTag
     */
    public static void open(final @NotNull Project project, final XmlTag targetXmlTag) {
        final InjectAViewModelDialog dialog =
                new InjectAViewModelDialog(project, targetXmlTag);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }
}
