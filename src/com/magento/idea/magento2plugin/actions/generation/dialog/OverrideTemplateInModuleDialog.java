/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.OverrideTemplateInModuleAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.OverrideTemplateInModuleGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInModuleDialog extends AbstractDialog {

    private static final String MODULE_NAME = "target module";
    private final @NotNull Project project;
    private final PsiFile psiFile;
    private JPanel contentPane;
    private JLabel selectModule; //NOPMD

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, MODULE_NAME})
    private JComboBox module;
    private JButton buttonOK;
    private JButton buttonCancel;

    /**
     * Constructor.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public OverrideTemplateInModuleDialog(
            final @NotNull Project project,
            final @NotNull PsiFile psiFile
    ) {
        super();

        this.project = project;
        this.psiFile = psiFile;

        setContentPane(contentPane);
        setModal(true);
        setTitle(OverrideTemplateInModuleAction.ACTION_TEMPLATE_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        fillModuleOptions();

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

        addComponentListener(new FocusOnAFieldListener(() -> module.requestFocusInWindow()));
    }

    private void fillModuleOptions() {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(psiFile.getContainingDirectory(), project);

        if (moduleData == null) {
            return;
        }
        final List<String> moduleNames = new ModuleIndex(project).getEditableModuleNames();

        for (final String moduleName : moduleNames) {
            if (!moduleData.getName().equals(moduleName)) {
                module.addItem(moduleName);
            }
        }
    }

    private void onOK() {
        if (validateFormFields()) {
            final OverrideTemplateInModuleGenerator overrideInModuleGenerator =
                    new OverrideTemplateInModuleGenerator(project);

            overrideInModuleGenerator.execute(psiFile, getModule());
            exit();
        }
    }

    private String getModule() {
        return this.module.getSelectedItem().toString();
    }

    /**
     * Ope popup.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public static void open(final @NotNull Project project, final @NotNull PsiFile psiFile) {
        final OverrideTemplateInModuleDialog dialog =
                new OverrideTemplateInModuleDialog(project, psiFile);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }
}
