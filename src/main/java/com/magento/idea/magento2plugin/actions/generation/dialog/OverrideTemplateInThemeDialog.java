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
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class OverrideTemplateInThemeDialog extends AbstractDialog {

    private static final String THEME_NAME = "target theme";

    private final @NotNull Project project;
    private final PsiFile psiFile;
    private JPanel contentPane;
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
        super(project);

        this.project = project;
        this.psiFile = psiFile;

        final String fileType = psiFile.getVirtualFile().getExtension();
        if (OverridableFileType.isFilePhtml(fileType)) {
            setTitle(OverrideTemplateInThemeAction.ACTION_TEMPLATE_DESCRIPTION);
        } else if (OverridableFileType.isFileJS(fileType)) {
            setTitle(OverrideTemplateInThemeAction.ACTION_JS_DESCRIPTION);
        } else if (OverridableFileType.isFileStyle(fileType)) {
            setTitle(OverrideTemplateInThemeAction.ACTION_STYLES_DESCRIPTION);
        }

        fillThemeOptions();

        // DialogWrapper handles button actions and ESC key automatically

        init();
    }

    /**
     * Create center panel.
     *
     * @return JComponent
     */
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
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
        dialog.centerDialog(dialog);
        dialog.showDialog();
    }

    protected void onWriteActionOK() {
        final OverrideTemplateInThemeGenerator overrideInThemeGenerator =
                new OverrideTemplateInThemeGenerator(project);

        overrideInThemeGenerator.execute(psiFile, this.getTheme());
        exit();
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
