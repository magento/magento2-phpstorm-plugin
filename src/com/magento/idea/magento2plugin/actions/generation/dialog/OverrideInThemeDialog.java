/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.OverrideInThemeDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.OverrideInThemeGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class OverrideInThemeDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private final PsiFile psiFile;
    @NotNull
    private final OverrideInThemeDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel selectTheme;
    private FilteredComboBox theme;

    public OverrideInThemeDialog(@NotNull Project project, PsiFile psiFile) {
        this.project = project;
        this.psiFile = psiFile;
        this.validator = OverrideInThemeDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (!validator.validate(project)) {
            JBPopupFactory.getInstance().createMessage("Invalid theme selection.").showCenteredInCurrentWindow(project);
            return;
        }

        OverrideInThemeGenerator overrideInThemeGenerator = OverrideInThemeGenerator.getInstance(project);
        overrideInThemeGenerator.execute(psiFile, this.getTheme());

        this.setVisible(false);
    }

    public String getTheme() {
        return this.theme.getSelectedItem().toString();
    }

    public static void open(@NotNull Project project, PsiFile psiFile) {
        OverrideInThemeDialog dialog = new OverrideInThemeDialog(project, psiFile);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        List<String> allThemesList = ModuleIndex.getInstance(project).getEditableThemeNames();

        this.theme = new FilteredComboBox(allThemesList);
    }
}
