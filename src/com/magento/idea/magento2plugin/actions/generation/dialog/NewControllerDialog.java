/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.NewControllerAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewControllerValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleControllerClassGenerator;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.ControllerFrontendPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.HttpRequest;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class NewControllerDialog extends AbstractDialog {
    private final NewControllerValidator validator;
    private final PsiDirectory baseDir;
    private final GetModuleNameByDirectory getModuleNameByDir;
    private final String moduleName;
    private JPanel contentPane;
    private JButton buttonOK;
    private Project project;
    private JButton buttonCancel;
    private FilteredComboBox controllerAreaSelect;
    private JLabel controllerAreaLabel;
    private FilteredComboBox httpMethodSelect;
    private JLabel controllerParentDirectoryLabel;
    private JTextField controllerName;
    private JTextField controllerParentDir;
    private JCheckBox inheritClass;
    private JPanel adminPanel;
    private JTextField acl;
    private JTextField actionName;

    /**
     * Open new dialog for adding new controller.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewControllerDialog(Project project, PsiDirectory directory) {
        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = NewControllerValidator.getInstance(this);
        this.getModuleNameByDir = GetModuleNameByDirectory.getInstance(project);

        setContentPane(contentPane);
        setModal(true);
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
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        onCancel();
                    }
                },
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
    public static void open(Project project, PsiDirectory directory) {
        NewControllerDialog dialog = new NewControllerDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (!validator.validate()) {
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
        String area = getArea();
        if (area.equals(Package.Areas.adminhtml.toString())) {
            controllerParentDir.setText(ControllerBackendPhp.DEFAULT_DIR);
            return;
        }
        controllerParentDir.setText(ControllerFrontendPhp.DEFAULT_DIR);
    }

    private void toggleAdminPanel() {
        String area = getArea();
        if (area.equals(Package.Areas.adminhtml.toString()) && inheritClass.isSelected()) {
            adminPanel.setVisible(true);
            return;
        }
        adminPanel.setVisible(false);
    }

    private String getModuleIdentifierPath() {
        String[]parts = moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        return parts[0] + File.separator + parts[1];
    }

    private String getNamespace() {
        String[]parts = moduleName.split(Package.VENDOR_MODULE_NAME_SEPARATOR);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        String directoryPart = getControllerDirectory().replace(
                File.separator,
                Package.FQN_SEPARATOR
        );
        String controllerPart = Package.FQN_SEPARATOR + getControllerName();

        return String.format(
                "%s%s%s%s%s%s",
                parts[0],
                Package.FQN_SEPARATOR,
                parts[1],
                Package.FQN_SEPARATOR,
                directoryPart,
                controllerPart
        );
    }

    private Boolean getIsInheritClass() {
        return inheritClass.isSelected();
    }

    protected void onCancel() {
        dispose();
    }

    private ArrayList<String> getAreaList() {
        return new ArrayList<>(
                Arrays.asList(
                        Package.Areas.frontend.toString(),
                        Package.Areas.adminhtml.toString()
                )
        );
    }

    private void createUIComponents() {
        this.controllerAreaSelect = new FilteredComboBox(getAreaList());
        this.httpMethodSelect = new FilteredComboBox(HttpRequest.getHttpMethodList());
    }
}
