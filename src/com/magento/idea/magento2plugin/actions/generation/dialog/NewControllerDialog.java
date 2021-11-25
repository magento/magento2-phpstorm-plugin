/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewControllerAction;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpDirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleControllerClassGenerator;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.ControllerFrontendPhp;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ConstructorCallsOverridableMethod",
        "PMD.ExcessiveImports"
})
public class NewControllerDialog extends AbstractDialog {
    private final String moduleName;
    private final Project project;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private FilteredComboBox controllerAreaSelect;
    private FilteredComboBox httpMethodSelect;
    private JTextField controllerParentDir;
    private JCheckBox inheritClass;
    private JPanel adminPanel;
    private JTextField acl;

    private static final String CONTROLLER_NAME = "controller name";
    private static final String ACTION_NAME = "action name";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONTROLLER_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_DIRECTORY,
            message = {PhpDirectoryRule.MESSAGE, CONTROLLER_NAME})
    private JTextField controllerName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, ACTION_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, ACTION_NAME})
    private JTextField actionName;

    /**
     * Open new dialog for adding new controller.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewControllerDialog(final Project project, final PsiDirectory directory) {
        super();
        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPane);
        setModal(true);
        setTitle(NewControllerAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        suggestControllerDirectory();
        controllerAreaSelect.addActionListener(e -> suggestControllerDirectory());
        controllerAreaSelect.addActionListener(e -> toggleAdminPanel());
        inheritClass.addActionListener(e -> toggleAdminPanel());
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

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
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    private String getModuleName() {
        return moduleName;
    }

    /**
     * Get controller name.
     *
     * @return String
     */
    public String getControllerName() {
        return controllerName.getText().trim();
    }

    /**
     * Get HTTP method name.
     *
     * @return String
     */
    public String getHttpMethodName() {
        return httpMethodSelect.getSelectedItem().toString();
    }

    /**
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return controllerAreaSelect.getSelectedItem().toString();
    }

    /**
     * Get ACL.
     *
     * @return String
     */
    public String getAcl() {
        return acl.getText().trim();
    }

    /**
     * Get action name.
     *
     * @return String
     */
    public String getActionName() {
        return actionName.getText().trim();
    }

    /**
     * Get controller directory.
     *
     * @return String
     */
    public String getControllerDirectory() {
        return controllerParentDir.getText().trim();
    }

    /**
     * Get action directory.
     *
     * @return String
     */
    public String getActionDirectory() {
        return getControllerDirectory() + File.separator + getControllerName();
    }

    /**
     * Open new controller dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewControllerDialog dialog = new NewControllerDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (!validateFormFields()) {
            return;
        }

        generateFile();
        this.setVisible(false);
    }

    private PsiFile generateFile() {
        return new ModuleControllerClassGenerator(new ControllerFileData(
                getActionDirectory(),
                getActionName(),
                getModuleName(),
                getArea(),
                getHttpMethodName(),
                getAcl(),
                getIsInheritClass(),
                getNamespace()
        ), project).generate(NewControllerAction.ACTION_NAME, true);
    }

    private void suggestControllerDirectory() {
        final String area = getArea();
        if (area.equals(Areas.adminhtml.toString())) {
            controllerParentDir.setText(ControllerBackendPhp.DEFAULT_DIR);
            return;
        }
        controllerParentDir.setText(ControllerFrontendPhp.DEFAULT_DIR);
    }

    private void toggleAdminPanel() {
        final String area = getArea();
        if (area.equals(Areas.adminhtml.toString()) && inheritClass.isSelected()) {
            adminPanel.setVisible(true);
            return;
        }
        adminPanel.setVisible(false);
    }

    private String getNamespace() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        final String directoryPart = getControllerDirectory().replace(
                File.separator,
                Package.fqnSeparator
        );
        final String controllerPart = Package.fqnSeparator + getControllerName();

        return String.format(
                "%s%s%s%s%s%s",
                parts[0],
                Package.fqnSeparator,
                parts[1],
                Package.fqnSeparator,
                directoryPart,
                controllerPart
        );
    }

    private Boolean getIsInheritClass() {
        return inheritClass.isSelected();
    }

    @Override
    protected void onCancel() {
        dispose();
    }

    private List<String> getAreaList() {
        return new ArrayList<>(
                Arrays.asList(
                        Areas.frontend.toString(),
                        Areas.adminhtml.toString()
                )
        );
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        this.controllerAreaSelect = new FilteredComboBox(getAreaList());
        this.httpMethodSelect = new FilteredComboBox(HttpMethod.getHttpMethodList());
    }
}
