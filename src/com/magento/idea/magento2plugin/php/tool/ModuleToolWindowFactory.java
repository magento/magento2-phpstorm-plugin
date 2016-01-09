package com.magento.idea.magento2plugin.php.tool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.magento.idea.magento2plugin.php.module.ComposerPackageModel;
import com.magento.idea.magento2plugin.php.module.MagentoModule;
import com.magento.idea.magento2plugin.php.module.MagentoComponentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 12/7/15.
 */
public class ModuleToolWindowFactory implements ToolWindowFactory {
    private JPanel windowContent;
    private JTree modulesTree;
    private ToolWindow myToolWindow;

    private static final String UNDEFINED_VENDOR = "Undefined";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        myToolWindow = toolWindow;
        initializeModulesTree(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(windowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void initializeModulesTree(Project project) {
        MagentoComponentManager magentoComponentManager = MagentoComponentManager.getInstance(project);

        Map<String, DefaultMutableTreeNode> vendorNodes = new HashMap<>();

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Project modules");

        modulesTree.setModel(new  DefaultTreeModel(rootNode));

        for (MagentoModule magentoModule: magentoComponentManager.getAllComponentsOfType(MagentoModule.class)) {
            ComposerPackageModel packageModel = magentoModule.getComposerModel();
            if (packageModel == null) {
                continue;
            }

            String vendorName = packageModel.getVendor();
            if (vendorName == null) {
                vendorName = UNDEFINED_VENDOR;
            }

            if (!vendorNodes.containsKey(vendorName)) {
                vendorNodes.put(vendorName, new DefaultMutableTreeNode(vendorName));
                rootNode.add(vendorNodes.get(vendorName));
            }

            vendorNodes.get(vendorName).add(new DefaultMutableTreeNode(magentoModule.getMagentoName()));
        }
    }
}