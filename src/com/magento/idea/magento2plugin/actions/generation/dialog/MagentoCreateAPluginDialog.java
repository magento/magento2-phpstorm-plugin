/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.MagentoCreateAPluginDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.MagentoPluginClassGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MagentoCreateAPluginDialog extends JDialog {
    @NotNull
    private final Project project;
    private Method targetMethod;
    private PhpClass targetClass;
    @NotNull
    private final MagentoCreateAPluginDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField pluginClassName;
    private JLabel pluginClassNameLabel;
    private JTextField pluginDirectory;
    private JLabel pluginDirectoryName;
    private JLabel selectTargetModule;
    private JComboBox pluginType;
    private JLabel pluginTypeLabel;
    private FilteredComboBox pluginModule;
    private JComboBox targetArea;
    private JLabel targetAreaLabel;

    public MagentoCreateAPluginDialog(@NotNull Project project, Method targetMethod, PhpClass targetClass) {
        this.project = project;
        this.targetMethod = targetMethod;
        this.targetClass = targetClass;
        this.validator = MagentoCreateAPluginDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();
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
            targetArea.addItem(area.toString());
        }
    }

    private void pushToMiddle() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2  -this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
    }

    private void onOK() {
        if (!validator.validate()) {
            return;
        }
        MagentoPluginFileData magentoPluginFileData = new MagentoPluginFileData(
                getPluginDirectory(),
                getPluginClassName(),
                getPluginType(),
                getPluginModule(),
                targetClass,
                targetMethod
        );
        MagentoPluginClassGenerator classGenerator = new MagentoPluginClassGenerator(magentoPluginFileData, project);
        classGenerator.generate();

        this.setVisible(false);
    }

    public String getPluginClassName() {
        return this.pluginClassName.getText().trim();
    }

    public String getPluginDirectory() {
        return this.pluginDirectory.getText().trim();
    }

    public String getPluginType() {
        return this.pluginType.getSelectedItem().toString();
    }

    public String getPluginModule() {
        return this.pluginModule.getSelectedItem().toString();
    }

    private void onCancel() {
        this.setVisible(false);
    }

    public static void open(@NotNull Project project, Method targetMethod, PhpClass targetClass) {
        MagentoCreateAPluginDialog dialog = new MagentoCreateAPluginDialog(project, targetMethod, targetClass);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();

        this.pluginModule = new FilteredComboBox(allModulesList);
    }
}
