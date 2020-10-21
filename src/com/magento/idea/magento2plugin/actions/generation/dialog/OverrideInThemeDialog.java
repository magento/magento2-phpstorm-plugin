/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.OverrideInThemeGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class OverrideInThemeDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private final PsiFile psiFile;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel selectTheme; //NOPMD
    private static final String THEME_NAME = "target theme";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, THEME_NAME})
    private FilteredComboBox theme;

    /**
     * Constructor.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public OverrideInThemeDialog(final @NotNull Project project, final PsiFile psiFile) {
        super();

        this.project = project;
        this.psiFile = psiFile;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    private void onOK() {
        if (!validateFormFields()) {
            return;
        }

        final OverrideInThemeGenerator overrideInThemeGenerator =
                new OverrideInThemeGenerator(project);
        overrideInThemeGenerator.execute(psiFile, this.getTheme());

        this.setVisible(false);
    }

    public String getTheme() {
        return this.theme.getSelectedItem().toString();
    }

    /**
     * Open popup.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public static void open(final @NotNull Project project, final PsiFile psiFile) {
        final OverrideInThemeDialog dialog = new OverrideInThemeDialog(project, psiFile);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void createUIComponents() { //NOPMD
        final List<String> allThemesList = ModuleIndex.getInstance(project).getEditableThemeNames();

        this.theme = new FilteredComboBox(allThemesList);
    }
}
