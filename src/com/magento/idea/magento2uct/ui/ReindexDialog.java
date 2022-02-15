/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.AbstractDialog;
import com.magento.idea.magento2uct.actions.ReindexVersionedIndexesAction;
import com.magento.idea.magento2uct.execution.DefaultExecutor;
import com.magento.idea.magento2uct.execution.process.ReindexHandler;
import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class ReindexDialog extends AbstractDialog {

    private final Project project;
    private final PsiDirectory directory;

    private JPanel contentPanel;
    private JComboBox<ComboBoxItemData> targetVersion;
    private JComboBox<ComboBoxItemData> targetIndex;
    private JButton buttonOk;
    private JButton buttonCancel;
    private JLabel targetVersionLabel;//NOPMD
    private JLabel targetIndexLabel;//NOPMD

    /**
     * Reindexing dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public ReindexDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();

        this.project = project;
        this.directory = directory;

        setContentPane(contentPanel);
        setModal(true);
        setTitle(ReindexVersionedIndexesAction.ACTION_NAME);
        getRootPane().setDefaultButton(buttonOk);

        buttonOk.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());

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
     * Open reindexing dialog window.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final ReindexDialog dialog = new ReindexDialog(
                project,
                directory
        );
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Execute reindexing action.
     */
    private void onOK() {
        if (targetVersion.getSelectedItem() == null || targetIndex.getSelectedItem() == null) {
            return;
        }
        final SupportedVersion version = SupportedVersion.getVersion(
                targetVersion.getSelectedItem().toString()
        );
        final IndexRegistry index = IndexRegistry.getRegistryInfoByKey(
                targetIndex.getSelectedItem().toString()
        );
        if (version == null || index == null) {
            return;
        }
        final DefaultExecutor executor = new DefaultExecutor(
                project,
                new ReindexHandler(
                        project,
                        directory,
                        version,
                        index
                )
        );
        executor.run();

        exit();
    }

    /**
     * Create custom components and fill their entries.
     */
    @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidInstantiatingObjectsInLoops"})
    private void createUIComponents() {
        targetVersion = new ComboBox<>();

        for (final String version : SupportedVersion.getSupportedVersions()) {
            targetVersion.addItem(new ComboBoxItemData(version, version));
        }
        targetIndex = new ComboBox<>();
        targetIndex.addItem(new ComboBoxItemData("", " --- Choose Target Index --- "));

        for (final String key : IndexRegistry.getIndexList()) {
            targetIndex.addItem(new ComboBoxItemData(key, key));
        }
    }
}
