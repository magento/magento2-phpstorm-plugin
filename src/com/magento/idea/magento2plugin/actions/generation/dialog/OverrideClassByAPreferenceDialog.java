/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.OverrideClassByAPreferenceAction;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.OverrideClassByAPreferenceDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.PreferenceClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PreferenceDiXmlGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class OverrideClassByAPreferenceDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private PhpClass targetClass;
    @NotNull
    private final OverrideClassByAPreferenceDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField preferenceClassName;
    private JLabel preferenceClassNameLabel;
    private JTextField preferenceDirectory;
    private JLabel selectPreferenceModule;
    private FilteredComboBox preferenceModule;
    private JComboBox preferenceArea;
    private JLabel preferenceAreaLabel;
    private JCheckBox inheritClass;
    private JLabel inheritClassLabel;
    private JLabel preferenceDirectoryLabel;

    public OverrideClassByAPreferenceDialog(@NotNull Project project, PhpClass targetClass) {
        this.project = project;
        this.targetClass = targetClass;
        this.validator = OverrideClassByAPreferenceDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();
        fillTargetAreaOptions();
        if (targetClass.isFinal()) {
            inheritClass.setVisible(false);
            inheritClassLabel.setVisible(false);
        }
        suggestPreferenceClassName(targetClass);
        suggestPreferenceDirectory(targetClass);

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

    private void suggestPreferenceDirectory(PhpClass targetClass) {
        String[] fqnParts = targetClass.getPresentableFQN().split("\\\\");
        if (fqnParts.length != 0) {
            fqnParts = ArrayUtil.remove(fqnParts, fqnParts.length - 1);
        }
        if (fqnParts[1] != null) {
            fqnParts = ArrayUtil.remove(fqnParts, 1);
        }
        if (fqnParts[0] != null) {
            fqnParts = ArrayUtil.remove(fqnParts, 0);
        }
        String suggestedDirectory = String.join(File.separator, fqnParts);
        preferenceDirectory.setText(suggestedDirectory);
    }

    private void suggestPreferenceClassName(PhpClass targetClass) {
        preferenceClassName.setText(targetClass.getName());
    }

    private void fillTargetAreaOptions() {
        for(Package.Areas area: Package.Areas.values()) {
            preferenceArea.addItem(area.toString());
        }
    }

    private void onOK() {
        if (!validator.validate(project)) {
            return;
        }
        PsiFile diXml = new PreferenceDiXmlGenerator(new PreferenceDiXmFileData(
                getPreferenceModule(),
                targetClass,
                getPreferenceClassFqn(),
                getNamespace(),
                getPreferenceArea()
        ), project).generate(OverrideClassByAPreferenceAction.ACTION_NAME);
        if (diXml == null) {
            JOptionPane.showMessageDialog(null, "Preference already declared in the target module!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new PreferenceClassGenerator(new PreferenceFileData(
                getPreferenceDirectory(),
                getPreferenceClassName(),
                getPreferenceModule(),
                targetClass,
                getPreferenceClassFqn(),
                getNamespace(),
                isInheritClass()
        ), project).generate(OverrideClassByAPreferenceAction.ACTION_NAME, true);


        this.setVisible(false);
    }

    public String getPreferenceClassName() {
        return this.preferenceClassName.getText().trim();
    }

    public String getPreferenceDirectory() {
        return this.preferenceDirectory.getText().trim();
    }

    public String getPreferenceArea() {
        return this.preferenceArea.getSelectedItem().toString();
    }

    public String getPreferenceModule() {
        return this.preferenceModule.getSelectedItem().toString();
    }

    public boolean isInheritClass() {
        return this.inheritClass.isSelected();
    }

    public static void open(@NotNull Project project, PhpClass targetClass) {
        OverrideClassByAPreferenceDialog dialog = new OverrideClassByAPreferenceDialog(project, targetClass);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();

        this.preferenceModule = new FilteredComboBox(allModulesList);
    }

    private String getNamespace() {
        String targetModule = getPreferenceModule();
        String namespace = targetModule.replace(Package.VENDOR_MODULE_NAME_SEPARATOR, Package.FQN_SEPARATOR);
        namespace = namespace.concat(Package.FQN_SEPARATOR);
        return namespace.concat(getPreferenceDirectory().replace(File.separator, Package.FQN_SEPARATOR));
    }

    private String getPreferenceClassFqn() {
        return getNamespace().concat(Package.FQN_SEPARATOR).concat(getPreferenceClassName());
    }
}
