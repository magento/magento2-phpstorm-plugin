/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewDbSchemaAction;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import org.jetbrains.annotations.NotNull;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class NewDbSchemaDialog extends AbstractDialog {
    private final Project project;
    private final PsiDirectory directory;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tableName;
    private JTextField tableComment;
    private JLabel tableNameLabel;
    private JLabel tableEngineLabel;
    private FilteredComboBox tableEngine;
    private JLabel tableResourceLabel;
    private FilteredComboBox tableResource;
    private JLabel tableCommentLabel;

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewDbSchemaDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();
        this.project = project;
        this.directory = directory;

        setTitle(NewDbSchemaAction.ACTION_DESCRIPTION);
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
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
        contentPanel.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * On buttonOK action listener.
     */
    private void onOK() {
        if (!validateFormFields()) {
            return;
        }

        this.setVisible(false);
    }

    /**
     * Open new declarative schema dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewDbSchemaDialog dialog = new NewDbSchemaDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        tableEngine = new FilteredComboBox(Arrays.asList());
        tableResource = new FilteredComboBox(Arrays.asList());
    }
}
