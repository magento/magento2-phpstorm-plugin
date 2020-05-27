/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.treeStructure.Tree;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewMenuItemValidator;
import com.magento.idea.magento2plugin.indexes.AdminMenuIndex;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectory;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class NewMenuItemDialog extends AbstractDialog {
    private javax.swing.JPanel controlPanel;
    private javax.swing.JTextField titleFieldName;
    private javax.swing.JTextField actionFieldName;
    private javax.swing.JTextField resourceFieldName;
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonCancel;
    private JTree moduleMenuTree;

    private final Project project;
    private final String moduleName;
    private final NewMenuItemValidator validator;

    /**
     * Open new dialog for adding a new menu item.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewMenuItemDialog(final Project project, final PsiDirectory directory) {
        super();
        this.project = project;
        this.moduleName = GetModuleNameByDirectory.getInstance(project).execute(directory);
        this.validator = new NewMenuItemValidator();

        setContentPane(controlPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(this.bundle.message("common.create.new.menu.item.title"));

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setMenuTree();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                onCancel();
            }
        });

        controlPanel.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    private void setMenuTree() {
        final List<String> menuItems = AdminMenuIndex.getInstance(project).getAdminMenuItems();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Contacts"); // root node

        DefaultMutableTreeNode contact1 = new DefaultMutableTreeNode("Contact # 1"); // level 1 node
        DefaultMutableTreeNode nickName1 = new DefaultMutableTreeNode("drocktapiff"); // level 2 (leaf) node
        contact1.add(nickName1);

        DefaultMutableTreeNode contact2 = new DefaultMutableTreeNode("Contact # 2");
        DefaultMutableTreeNode nickName2 = new DefaultMutableTreeNode("dic19");
        contact2.add(nickName2);

        root.add(contact1);
        root.add(contact2);

        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tree = new Tree(model);

        moduleMenuTree.setModel(tree.getModel());
    }

    /**
     * Open a new CLI command dialog.
     *
     * @param project Project
     * @param directory Directory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewMenuItemDialog dialog = new NewMenuItemDialog(project, directory);

        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        //if (!validator.validate(this.project,this)) {
        //    return;
       // }
        //this.generate();
        this.setVisible(false);
    }
}
