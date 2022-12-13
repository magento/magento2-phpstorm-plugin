/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.OverrideTemplateInThemeAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.OverrideTemplateInThemeGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.OverridableFileType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInThemeDialog extends AbstractDialog {

    private static final String THEME_NAME = "target theme";

    private final @NotNull Project project;
    private final PsiFile psiFile;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel selectTheme; //NOPMD

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, THEME_NAME})
    private JComboBox theme;

    /**
     * Constructor.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public OverrideTemplateInThemeDialog(
            final @NotNull Project project,
            final @NotNull PsiFile psiFile
    ) {
        super();

        this.project = project;
        this.psiFile = psiFile;

        setContentPane(contentPane);
        setModal(true);

        final String fileType = psiFile.getVirtualFile().getExtension();
        if (OverridableFileType.isFilePhtml(fileType)) {
            setTitle(OverrideTemplateInThemeAction.ACTION_TEMPLATE_DESCRIPTION);
        } else if (OverridableFileType.isFileJS(fileType)) {
            setTitle(OverrideTemplateInThemeAction.ACTION_JS_DESCRIPTION);
        } else if (OverridableFileType.isFileStyle(fileType)) {
            setTitle(OverrideTemplateInThemeAction.ACTION_STYLES_DESCRIPTION);
        }
        getRootPane().setDefaultButton(buttonOK);
        fillThemeOptions();

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

        addComponentListener(new FocusOnAFieldListener(() -> theme.requestFocusInWindow()));
    }

    /**
     * Open popup.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public static void open(final @NotNull Project project, final @NotNull PsiFile psiFile) {
        final OverrideTemplateInThemeDialog dialog =
                new OverrideTemplateInThemeDialog(project, psiFile);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (validateFormFields()) {
            final OverrideTemplateInThemeGenerator overrideInThemeGenerator =
                    new OverrideTemplateInThemeGenerator(project);

            overrideInThemeGenerator.execute(psiFile, this.getTheme());
            exit();
        }
    }

    private String getTheme() {
        return this.theme.getSelectedItem().toString();
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    private void fillThemeOptions() {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(psiFile.getContainingDirectory(), project);
        String area = ""; // NOPMD

        if (moduleData == null) {
            if (psiFile.getVirtualFile().getExtension()
                    .equals(OverridableFileType.JS.getType())) {
                area = "base";
            } else {
                return;
            }
        } else {
            if (moduleData.getType().equals(ComponentType.module)) {
                final PsiDirectory viewDir = moduleData.getViewDir();

                if (viewDir == null) {
                    return;
                }
                final String filePath = psiFile.getVirtualFile().getPath();
                final String relativePath = filePath.replace(
                        viewDir.getVirtualFile().getPath(),
                        ""
                );
                area = relativePath.split(Package.V_FILE_SEPARATOR)[1];
            } else {
                area = moduleData.getName().split(Package.V_FILE_SEPARATOR)[0];
            }
        }

        final List<String> themeNames = new ModuleIndex(project).getEditableThemeNames();

        for (final String themeName : themeNames) {
            if (Areas.base.toString().equals(area)
                    || themeName.split(Package.V_FILE_SEPARATOR)[0].equals(area)) {
                theme.addItem(themeName);
            }
        }
    }
}
