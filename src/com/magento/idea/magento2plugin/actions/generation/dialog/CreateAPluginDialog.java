/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.CreateAPluginAction;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.PluginFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.CreateAPluginDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PluginDiXmlGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.List;

public class CreateAPluginDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private Method targetMethod;
    private PhpClass targetClass;
    @NotNull
    private final CreateAPluginDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField pluginClassName;
    private JLabel pluginClassNameLabel;
    private JTextField pluginDirectory;
    private JLabel pluginDirectoryName;
    private JLabel selectPluginModule;
    private JComboBox pluginType;
    private JLabel pluginTypeLabel;
    private FilteredComboBox pluginModule;
    private JComboBox pluginArea;
    private JLabel pluginAreaLabel;
    private JTextField pluginName;
    private JLabel pluginNameLabel;
    private JTextField pluginSortOrder;
    private JLabel pluginSortOrderLabel;

    public CreateAPluginDialog(@NotNull Project project, Method targetMethod, PhpClass targetClass) {
        this.project = project;
        this.targetMethod = targetMethod;
        this.targetClass = targetClass;
        this.validator = CreateAPluginDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        fillPluginTypeOptions();
        fillTargetAreaOptions();

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

    private void fillPluginTypeOptions() {
        for (Plugin.PluginType pluginPrefixType: Plugin.PluginType.values()) {
            pluginType.addItem(pluginPrefixType.toString());
        }
    }

    private void fillTargetAreaOptions() {
        for(Package.Areas area: Package.Areas.values()) {
            pluginArea.addItem(area.toString());
        }
    }

    private void onOK() {
        if (!validator.validate(project)) {
            return;
        }
        new PluginClassGenerator(new PluginFileData(
                getPluginDirectory(),
                getPluginClassName(),
                getPluginType(),
                getPluginModule(),
                targetClass,
                targetMethod,
                getPluginClassFqn(),
                getNamespace()
        ), project).generate(CreateAPluginAction.ACTION_NAME, true);

        new PluginDiXmlGenerator(new PluginDiXmlData(
                getPluginArea(),
                getPluginModule(),
                targetClass,
                getPluginSortOrder(),
                getPluginName(),
                getPluginClassFqn()
        ), project).generate(CreateAPluginAction.ACTION_NAME);

        this.setVisible(false);
    }

    public String getPluginName() {
        return this.pluginName.getText().trim();
    }

    public String getPluginSortOrder() {
        return this.pluginSortOrder.getText().trim();
    }

    public String getPluginClassName() {
        return this.pluginClassName.getText().trim();
    }

    public String getPluginDirectory() {
        return this.pluginDirectory.getText().trim();
    }

    public String getPluginArea() {
        return this.pluginArea.getSelectedItem().toString();
    }

    public String getPluginType() {
        return this.pluginType.getSelectedItem().toString();
    }

    public String getPluginModule() {
        return this.pluginModule.getSelectedItem().toString();
    }

    public static void open(@NotNull Project project, Method targetMethod, PhpClass targetClass) {
        CreateAPluginDialog dialog = new CreateAPluginDialog(project, targetMethod, targetClass);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();

        this.pluginModule = new FilteredComboBox(allModulesList);
    }

    private String getNamespace() {
        String targetModule = getPluginModule();
        String namespace = targetModule.replace(Package.VENDOR_MODULE_NAME_SEPARATOR, Package.FQN_SEPARATOR);
        namespace = namespace.concat(Package.FQN_SEPARATOR);
        return namespace.concat(getPluginDirectory().replace(File.separator, Package.FQN_SEPARATOR));
    }

    private String getPluginClassFqn() {
        return getNamespace().concat(Package.FQN_SEPARATOR).concat(getPluginClassName());
    }
}
