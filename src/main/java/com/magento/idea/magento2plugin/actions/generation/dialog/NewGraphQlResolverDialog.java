/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewGraphQlResolverAction;
import com.magento.idea.magento2plugin.actions.generation.data.GraphQlResolverFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpDirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleGraphQlResolverClassGenerator;
import com.magento.idea.magento2plugin.magento.files.GraphQlResolverPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class NewGraphQlResolverDialog extends AbstractDialog {

    private final PsiDirectory baseDir;
    private final String moduleName;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private final Project project;
    private static final String CLASS_NAME = "class name";
    private static final String PARENT_DIRECTORY = "directory";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CLASS_NAME})
    private JTextField graphQlResolverClassName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, PARENT_DIRECTORY})
    @FieldValidation(rule = RuleRegistry.PHP_DIRECTORY,
            message = {PhpDirectoryRule.MESSAGE, PARENT_DIRECTORY})
    private JTextField graphQlResolverParentDir;

    private JLabel graphQlResolverClassNameErrorMessage;//NOPMD
    private JLabel graphQlResolverParentDirErrorMessage;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewGraphQlResolverDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();

        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle(NewGraphQlResolverAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        suggestGraphQlResolverDirectory();

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(
                new FocusOnAFieldListener(() -> graphQlResolverClassName.requestFocusInWindow())
        );
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewGraphQlResolverDialog dialog = new NewGraphQlResolverDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);

        // TODO: It's a workaround. Proper fix should be done as:
        // https://github.com/magento/magento2-phpstorm-plugin/issues/2080
        try (var token = com.intellij.concurrency.ThreadContext.resetThreadContext()) {
            dialog.setVisible(true);
        }
    }

    protected void onOK() {
        if (validateFormFields()) {
            generateFile();
            exit();
        }
    }

    private void generateFile() {
        new ModuleGraphQlResolverClassGenerator(new GraphQlResolverFileData(
                getGraphQlResolverDirectory(),
                getGraphQlResolverClassName(),
                getModuleName(),
                getGraphQlResolverClassFqn(),
                getNamespace()
        ), project).generate(NewGraphQlResolverAction.ACTION_NAME, true);
    }

    private String getModuleName() {
        return moduleName;
    }

    public String getGraphQlResolverClassName() {
        return graphQlResolverClassName.getText().trim();
    }

    public String getGraphQlResolverDirectory() {
        return graphQlResolverParentDir.getText().trim();
    }

    private void suggestGraphQlResolverDirectory() {
        final String moduleIdentifierPath = getModuleIdentifierPath();
        if (moduleIdentifierPath == null) {
            graphQlResolverParentDir.setText(GraphQlResolverPhp.DEFAULT_DIR);
            return;
        }

        final String path = baseDir.getVirtualFile().getPath();
        final String[] pathParts = path.split(moduleIdentifierPath);
        final int partsMaxLength = 2;
        if (pathParts.length != partsMaxLength) {
            graphQlResolverParentDir.setText(GraphQlResolverPhp.DEFAULT_DIR);
            return;
        }

        if (pathParts[1] != null) {
            graphQlResolverParentDir.setText(pathParts[1].substring(1));
            return;
        }
        graphQlResolverParentDir.setText(GraphQlResolverPhp.DEFAULT_DIR);
    }

    private String getModuleIdentifierPath() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        return parts[0] + File.separator + parts[1];
    }

    private String getNamespace() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        final String directoryPart = getGraphQlResolverDirectory().replace(
                File.separator,
                Package.fqnSeparator
        );
        return parts[0] + Package.fqnSeparator + parts[1] + Package.fqnSeparator + directoryPart;
    }

    private String getGraphQlResolverClassFqn() {
        return getNamespace().concat(Package.fqnSeparator).concat(getGraphQlResolverClassName());
    }
}
