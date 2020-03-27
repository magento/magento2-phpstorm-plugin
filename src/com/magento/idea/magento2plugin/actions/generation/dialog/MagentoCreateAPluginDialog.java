/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.MagentoCreateAPluginDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.MagentoPackages;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Properties;

public class MagentoCreateAPluginDialog extends JDialog {
    @NotNull
    private final Project project;
    @NotNull
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final MagentoCreateAPluginDialogValidator validator;
    private final CamelCaseToHyphen camelCaseToHyphen;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField pluginClassName;
    private JLabel pluginClassNameLabel;
    private JTextField pluginDirectory;
    private JLabel pluginDirectoryName;
    private JTextArea moduleDescription;
    private JTextField moduleVersion;
    private JLabel selectTargetModule;
    private JComboBox pluginType;
    private JLabel pluginTypeLabel;
    private FilteredComboBox targetModule;
    private String detectedPackageName;

    public MagentoCreateAPluginDialog(@NotNull Project project) {
        this.project = project;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.camelCaseToHyphen = CamelCaseToHyphen.getInstance();
        this.validator = MagentoCreateAPluginDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();
        fillPluginTypeOptions();

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
        pluginType.addItem(Plugin.beforePluginPrefix);
        pluginType.addItem(Plugin.afterPluginPrefix);
        pluginType.addItem(Plugin.aroundPluginPrefix);
    }

    private void detectPackageName(@NotNull PsiDirectory initialBaseDir) {
        PsiDirectory parentDir = initialBaseDir.getParent();
        if (parentDir != null && parentDir.toString().endsWith(MagentoPackages.PACKAGES_ROOT)) {
            pluginClassName.setVisible(false);
            pluginClassNameLabel.setVisible(false);
            this.detectedPackageName = initialBaseDir.getName();
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
        //generateFiles();
        this.setVisible(false);
    }

//    private void generateFiles() {
//        PsiDirectory baseDir = detectedPackageName != null;
//        ModuleDirectoriesData moduleDirectoriesData = directoryGenerator.createModuleDirectories(getPluginClassName(), getPluginDirectory(), baseDir);
//        Properties attributes = getAttributes();
//        PsiFile composerJson = fileFromTemplateGenerator.generate(ComposerJson.getInstance(), attributes, moduleDirectoriesData.getModuleDirectory(), NewModuleAction.ACTION_NAME);
//        if (composerJson == null) {
//            return;
//        }
//        PsiFile registrationPhp = fileFromTemplateGenerator.generate(RegistrationPhp.getInstance(), attributes, moduleDirectoriesData.getModuleDirectory(), NewModuleAction.ACTION_NAME);
//        if (registrationPhp == null) {
//            return;
//        }
//        fileFromTemplateGenerator.generate(ModuleXml.getInstance(), attributes, moduleDirectoriesData.getModuleEtcDirectory(), NewModuleAction.ACTION_NAME);
//    }

    public String getPluginClassName() {
        if (detectedPackageName != null) {
            return detectedPackageName;
        }
        return this.pluginClassName.getText().trim();
    }

    public String getPluginDirectory() {
        return this.pluginDirectory.getText().trim();
    }

    public String getModuleDescription() {
        return this.moduleDescription.getText().trim();
    }

    public String getModuleVersion() {
        return this.moduleVersion.getText().trim();
    }

    private void onCancel() {
        this.setVisible(false);
    }

    public static void open(@NotNull Project project) {
        MagentoCreateAPluginDialog dialog = new MagentoCreateAPluginDialog(project);
        dialog.pack();
        dialog.setVisible(true);
    }

    private Properties getAttributes() {
        Properties attributes = new Properties();
        this.fillAttributes(attributes);
        return attributes;
    }

    private void fillAttributes(Properties attributes) {
        attributes.setProperty("PACKAGE", getPluginClassName());
        attributes.setProperty("MODULE_NAME", getPluginDirectory());
        attributes.setProperty("MODULE_DESCRIPTION", getModuleDescription());
        attributes.setProperty("COMPOSER_PACKAGE_NAME", getComposerPackageName());
        attributes.setProperty("MODULE_VERSION", getModuleVersion());
    }

    @NotNull
    private String getComposerPackageName() {
        return camelCaseToHyphen.convert(getPluginClassName())
                .concat("/")
                .concat(camelCaseToHyphen.convert(getPluginDirectory()));
    }

    private void createUIComponents() {
        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();

        this.targetModule = new FilteredComboBox(allModulesList);
    }
}
