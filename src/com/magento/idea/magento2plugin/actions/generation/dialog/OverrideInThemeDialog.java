/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.CreateAPluginAction;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.CreateAPluginDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.OverrideInThemeDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginDiXmlGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class OverrideInThemeDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private Method targetMethod;
    private PhpClass targetClass;
    @NotNull
    private final OverrideInThemeDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel selectTheme;
    private FilteredComboBox theme;

    public OverrideInThemeDialog(@NotNull Project project, PsiFile psiFile) {
        this.project = project;
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
            return;
        }

        // TODO: implement generator

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
