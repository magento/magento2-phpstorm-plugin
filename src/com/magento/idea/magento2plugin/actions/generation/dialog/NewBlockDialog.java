/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NewBlockDialog extends AbstractDialog {
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField blockName;
    private JRadioButton frontendRadioButton;
    private JRadioButton adminhtmlRadioButton;
    private JTextField blockParentDir;
    private Project project;

    public NewBlockDialog(Project project) {
        this.project = project;

        setContentPane(contentPanel);
        setModal(true);
        setTitle("Create a new Magento 2 block..");
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

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void open(Project project, PsiDirectory directory) {
        NewBlockDialog dialog = new NewBlockDialog(project);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void onOK() {
        if (!validator.validate()) {
            return;
        }
        generateFiles();
        this.setVisible(false);
    }

    public void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
