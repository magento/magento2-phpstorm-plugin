/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;//NOPMD

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.OverrideClassByAPreferenceAction;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.BoxNotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.PreferenceClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.PreferenceDiXmlGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.DataClass", "PMD.UnusedPrivateMethod"})
public class OverrideClassByAPreferenceDialog extends AbstractDialog { //NOPMD
    @NotNull
    private final Project project;
    private final PhpClass targetClass;
    private boolean isInterface;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private final CommonBundle commonBundle;
    private final ValidatorBundle validatorBundle;
    private JLabel inheritClassLabel;
    private JComboBox preferenceArea;
    private JCheckBox inheritClass;
    private JLabel preferenceAreaLabel;//NOPMD
    private JLabel selectPreferenceModule;//NOPMD
    private JLabel preferenceDirectoryLabel;//NOPMD
    private JLabel preferenceClassNameLabel;//NOPMD
    private static final String MODULE = "target module";
    private static final String CLASS = "class name";
    private static final String DIRECTORY = "directory";

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MODULE})
    @FieldValidation(rule = RuleRegistry.BOX_NOT_EMPTY,
            message = {BoxNotEmptyRule.MESSAGE, MODULE})
    private FilteredComboBox preferenceModule;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CLASS})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CLASS})
    private JTextField preferenceClassName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, DIRECTORY})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, DIRECTORY})
    private JTextField preferenceDirectory;

    /**
     * Constructor.
     *
     * @param project Project
     * @param targetClass PhpClass
     */
    public OverrideClassByAPreferenceDialog(
            final @NotNull Project project,
            final PhpClass targetClass
    ) {
        super();

        this.project = project;
        this.targetClass = targetClass;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.isInterface = false;

        setContentPane(contentPane);
        setModal(true);
        setTitle(OverrideClassByAPreferenceAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        fillTargetAreaOptions();
        if (targetClass.isFinal()) {
            inheritClass.setVisible(false);
            inheritClassLabel.setVisible(false);
        }
        if (targetClass.isInterface()) {
            this.isInterface = true;
        }
        suggestPreferenceClassName(targetClass);
        suggestPreferenceDirectory(targetClass);

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    private void suggestPreferenceDirectory(final PhpClass targetClass) {
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
        final String suggestedDirectory = String.join(File.separator, fqnParts);
        preferenceDirectory.setText(suggestedDirectory);
    }

    private void suggestPreferenceClassName(final PhpClass targetClass) {
        preferenceClassName.setText(targetClass.getName());
    }

    private void fillTargetAreaOptions() {
        for (final Areas area: Areas.values()) {
            preferenceArea.addItem(area.toString());
        }
    }

    protected void onOK() {
        if (!validateFormFields()) {
            return;
        }
        final PsiFile diXml = new PreferenceDiXmlGenerator(new PreferenceDiXmFileData(
                getPreferenceModule(),
                targetClass,
                getPreferenceClassFqn(),
                getNamespace(),
                getPreferenceArea()
        ), project).generate(OverrideClassByAPreferenceAction.ACTION_NAME);
        if (diXml == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.class.alreadyDeclared",
                    "Preference"
            );
            final String errorTitle = commonBundle.message("common.error");
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        new PreferenceClassGenerator(new PreferenceFileData(
                getPreferenceDirectory(),
                getPreferenceClassName(),
                getPreferenceModule(),
                targetClass,
                getPreferenceClassFqn(),
                getNamespace(),
                isInheritClass(),
                isInterface
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

    /**
     * Open dialog.
     *
     * @param project Project
     * @param targetClass PhpClass
     */
    public static void open(final @NotNull Project project, final PhpClass targetClass) {
        final OverrideClassByAPreferenceDialog dialog =
                new OverrideClassByAPreferenceDialog(project, targetClass);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        final List<String> allModulesList = ModuleIndex.getInstance(project)
                .getEditableModuleNames();

        this.preferenceModule = new FilteredComboBox(allModulesList);
    }

    private String getNamespace() {
        final String targetModule = getPreferenceModule();
        String namespace = targetModule.replace(
                Package.vendorModuleNameSeparator,
                Package.fqnSeparator
        );
        namespace = namespace.concat(Package.fqnSeparator);
        return namespace.concat(getPreferenceDirectory()
                .replace(File.separator, Package.fqnSeparator));
    }

    private String getPreferenceClassFqn() {
        return getNamespace().concat(Package.fqnSeparator).concat(getPreferenceClassName());
    }
}
