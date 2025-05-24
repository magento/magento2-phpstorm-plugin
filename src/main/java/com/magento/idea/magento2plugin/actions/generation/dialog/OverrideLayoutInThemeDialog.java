/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.OverrideLayoutInThemeAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.OverrideLayoutInThemeGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
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
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OverrideLayoutInThemeDialog extends AbstractDialog {

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
    private JRadioButton radioButtonOverride;
    private JRadioButton radioButtonExtend;

    /**
     * Constructor.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public OverrideLayoutInThemeDialog(
            final @NotNull Project project,
            final @NotNull PsiFile psiFile
    ) {
        super(project);

        this.project = project;
        this.psiFile = psiFile;

        setTitle(OverrideLayoutInThemeAction.ACTION_DESCRIPTION);
        fillThemeOptions();

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        radioButtonOverride.addActionListener((final ActionEvent event) -> onOverride());
        radioButtonExtend.addActionListener((final ActionEvent event) -> onExtend());

        contentPane.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        init();
    }

    /**
     * Open popup.
     *
     * @param project Project
     * @param psiFile PsiFile
     */
    public static void open(final @NotNull Project project, final @NotNull PsiFile psiFile) {
        final OverrideLayoutInThemeDialog dialog =
                new OverrideLayoutInThemeDialog(project, psiFile);
        dialog.centerDialog(dialog);
        dialog.showDialog();
    }

    /**
     * Create center panel.
     *
     * @return JComponent
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected void onWriteActionOK() {
        final OverrideLayoutInThemeGenerator overrideLayoutInThemeGenerator =
                new OverrideLayoutInThemeGenerator(project);

        overrideLayoutInThemeGenerator.execute(psiFile, getTheme(), isOverride());
        exit();
    }

    private String getTheme() {
        return this.theme.getSelectedItem().toString();
    }

    private boolean isOverride() {
        return this.radioButtonOverride.isSelected();
    }

    private void onOverride() {
        this.radioButtonOverride.setSelected(true);
        this.radioButtonExtend.setSelected(false);
    }

    private void onExtend() {
        this.radioButtonOverride.setSelected(false);
        this.radioButtonExtend.setSelected(true);
    }

    private void fillThemeOptions() {
        final GetMagentoModuleUtil.MagentoModuleData moduleData =
                GetMagentoModuleUtil.getByContext(psiFile.getContainingDirectory(), project);

        if (moduleData == null) {
            return;
        }
        String area = ""; // NOPMD;

        if (moduleData.getType().equals(ComponentType.module)) {
            final PsiDirectory viewDir = moduleData.getViewDir();

            if (viewDir == null) {
                return;
            }
            final String filePath = psiFile.getVirtualFile().getPath();
            final String relativePath = filePath.replace(viewDir.getVirtualFile().getPath(), "");
            area = relativePath.split(Package.V_FILE_SEPARATOR)[1];
        } else {
            area = moduleData.getName().split(Package.V_FILE_SEPARATOR)[0];
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
