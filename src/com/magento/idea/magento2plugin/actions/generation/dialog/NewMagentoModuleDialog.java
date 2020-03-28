/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.ide.IdeView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewModuleAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.NewMagentoModuleDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;

public class NewMagentoModuleDialog extends JDialog {
    @NotNull
    private final Project project;
    @NotNull
    private final PsiDirectory initialBaseDir;
    @Nullable
    private final PsiFile file;
    @Nullable
    private final IdeView view;
    @Nullable
    private final Editor editor;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final NewMagentoModuleDialogValidator validator;
    private final CamelCaseToHyphen camelCaseToHyphen;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField packageName;
    private JLabel packageNameLabel;
    private JTextField moduleName;
    private JLabel moduleNameLabel;
    private JTextArea moduleDescription;
    private JLabel moduleDescriptionLabel;
    private JTextField moduleVersion;
    private JLabel moduleVersionLabel;
    private String detectedPackageName;

    public NewMagentoModuleDialog(@NotNull Project project, @NotNull PsiDirectory initialBaseDir, @Nullable PsiFile file, @Nullable IdeView view, @Nullable Editor editor) {
        this.project = project;
        this.initialBaseDir = initialBaseDir;
        this.file = file;
        this.view = view;
        this.editor = editor;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.camelCaseToHyphen = CamelCaseToHyphen.getInstance();
        this.validator = NewMagentoModuleDialogValidator.getInstance(this);
        detectPackageName(initialBaseDir);
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

    private void detectPackageName(@NotNull PsiDirectory initialBaseDir) {
        PsiDirectory parentDir = initialBaseDir.getParent();
        if (parentDir != null && parentDir.toString().endsWith(Package.PACKAGES_ROOT)) {
            packageName.setVisible(false);
            packageNameLabel.setVisible(false);
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
        generateFiles();
        this.setVisible(false);
    }

    private void generateFiles() {
        PsiDirectory baseDir = detectedPackageName != null ? this.initialBaseDir.getParent() : this.initialBaseDir;
        ModuleDirectoriesData moduleDirectoriesData = directoryGenerator.createModuleDirectories(getPackageName(), getModuleName(), baseDir);
        Properties attributes = getAttributes();
        PsiFile composerJson = fileFromTemplateGenerator.generate(ComposerJson.getInstance(), attributes, moduleDirectoriesData.getModuleDirectory(), NewModuleAction.ACTION_NAME);
        if (composerJson == null) {
            return;
        }
        PsiFile registrationPhp = fileFromTemplateGenerator.generate(RegistrationPhp.getInstance(), attributes, moduleDirectoriesData.getModuleDirectory(), NewModuleAction.ACTION_NAME);
        if (registrationPhp == null) {
            return;
        }
        fileFromTemplateGenerator.generate(ModuleXml.getInstance(), attributes, moduleDirectoriesData.getModuleEtcDirectory(), NewModuleAction.ACTION_NAME);
    }

    public String getPackageName() {
        if (detectedPackageName != null) {
            return detectedPackageName;
        }
        return this.packageName.getText().trim();
    }

    public String getModuleName() {
        return this.moduleName.getText().trim();
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

    public static void open(@NotNull Project project, @NotNull PsiDirectory initialBaseDir, @Nullable PsiFile file, @Nullable IdeView view, @Nullable Editor editor) {
        NewMagentoModuleDialog dialog = new NewMagentoModuleDialog(project, initialBaseDir, file, view, editor);
        dialog.pack();
        dialog.setVisible(true);
    }

    private Properties getAttributes() {
        Properties attributes = new Properties();
        this.fillAttributes(attributes);
        return attributes;
    }

    private void fillAttributes(Properties attributes) {
        attributes.setProperty("PACKAGE", getPackageName());
        attributes.setProperty("MODULE_NAME", getModuleName());
        attributes.setProperty("MODULE_DESCRIPTION", getModuleDescription());
        attributes.setProperty("COMPOSER_PACKAGE_NAME", getComposerPackageName());
        attributes.setProperty("MODULE_VERSION", getModuleVersion());
    }

    @NotNull
    private String getComposerPackageName() {
        return camelCaseToHyphen.convert(getPackageName())
                .concat("/")
                .concat(camelCaseToHyphen.convert(getModuleName()));
    }
}
