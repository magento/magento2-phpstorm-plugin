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
    private JLabel targetVersionLabel;//NOPMD
    private JLabel targetIndexLabel;//NOPMD

    /**
     * Reindexing dialog.
     *
     * @param project   Project
     * @param directory PsiDirectory
     */
    public ReindexDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super(project);

        this.project = project;
        this.directory = directory;

        setTitle(ReindexVersionedIndexesAction.ACTION_NAME);

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        init();
    }

    /**
     * Open reindexing dialog window.
     *
     * @param project   Project
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
        dialog.centerDialog(dialog);
        dialog.showDialog();
    }

    /**
     * Create center panel.
     *
     * @return JComponent
     */
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    /**
     * Execute reindexing action.
     */
    protected void onWriteActionOK() {
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

        for (final SupportedVersion version : SupportedVersion.getSupportedVersions()) {
            targetVersion.addItem(new ComboBoxItemData(version.getVersion(), version.getVersion()));
        }
        targetIndex = new ComboBox<>();
        targetIndex.addItem(new ComboBoxItemData("", " --- Choose Target Index --- "));

        for (final String key : IndexRegistry.getIndexList()) {
            targetIndex.addItem(new ComboBoxItemData(key, key));
        }
    }
}
