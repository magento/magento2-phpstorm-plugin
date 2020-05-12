/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewUiFormAction;
import com.magento.idea.magento2plugin.actions.generation.data.UiFormFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewUiFormValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleUiFormGenerator;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ConstructorCallsOverridableMethod"
})
public class NewUiFormDialog extends AbstractDialog {
    private final NewUiFormValidator validator;
    private final Project project;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private FilteredComboBox formAreaSelect;
    private JTextField formName;

    /**
     * Open new dialog for adding new controller.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewUiFormDialog(final Project project, final PsiDirectory directory) {
        super();
        this.project = project;
        this.validator = new NewUiFormValidator(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        onCancel();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Get controller name.
     *
     * @return String
     */
    public String getFormName() {
        return formName.getText().trim();
    }

    /**
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return formAreaSelect.getSelectedItem().toString();
    }

    /**
     * Open new controller dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewUiFormDialog dialog = new NewUiFormDialog(project, directory);
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
        return new ModuleUiFormGenerator(new UiFormFileData(
                getFormName(),
                getArea()
        ), project).generate(NewUiFormAction.ACTION_NAME, true);
    }

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
        this.formAreaSelect = new FilteredComboBox(getAreaList());
    }
}
