/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.context;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateActionBase;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractContextAction extends CreateFromTemplateActionBase {

    private static final DataKey<AttributesDefaults> ATTRIBUTE_DEFAULTS = DataKey.create(
            "attribute.defaults"
    );
    private final ModuleFileInterface moduleFile;
    private DataContext customDataContext;

    /**
     * Abstract context action constructor.
     *
     * @param title String
     * @param description String
     * @param moduleFile ModuleFileInterface
     */
    public AbstractContextAction(
            final @NotNull String title,
            final @NotNull String description,
            final @NotNull ModuleFileInterface moduleFile
    ) {
        super(title, description, MagentoIcons.MODULE);
        this.moduleFile = moduleFile;
    }

    /**
     * Abstract context action constructor.
     *
     * @param title String
     * @param description String
     * @param moduleFile ModuleFileInterface
     * @param icon Icon
     */
    public AbstractContextAction(
            final @NotNull String title,
            final @NotNull String description,
            final @NotNull ModuleFileInterface moduleFile,
            final @Nullable Icon icon
    ) {
        super(title, description, icon);
        this.moduleFile = moduleFile;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void update(final @NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(false);
        event.getPresentation().setVisible(false);

        final Project project = event.getProject();

        if (project == null || !Settings.isEnabled(project)) {
            return;
        }
        final DataContext context = event.getDataContext();
        final PsiElement targetElement = LangDataKeys.PSI_ELEMENT.getData(context);

        if (targetElement == null) {
            return;
        }
        PsiDirectory targetDirectory = null;
        PsiFile targetFile = null;

        if (targetElement instanceof PsiDirectory) {
            targetDirectory = (PsiDirectory) targetElement;
        } else if (targetElement instanceof PsiFile) {
            targetFile = (PsiFile) targetElement;
            targetDirectory = targetFile.getContainingDirectory();
        }

        if (targetDirectory == null) {
            return;
        }
        final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                .getByContext(targetDirectory, project);

        if (moduleData == null
                || moduleData.getName() == null
                || !isVisible(moduleData, targetDirectory, targetFile)) {
            return;
        }

        if (moduleData.getType().equals(ComponentType.module)
                && !GetMagentoModuleUtil.isEditableModule(moduleData)) {
            return;
        }

        customDataContext = SimpleDataContext
                .builder()
                .add(
                        ATTRIBUTE_DEFAULTS,
                        getProperties(
                                new AttributesDefaults(),
                                moduleData,
                                targetDirectory,
                                targetFile
                        )
                )
                .build();

        event.getPresentation().setEnabled(true);
        event.getPresentation().setVisible(true);
    }

    /**
     * Implement check if an action should be shown in the context defined by the module,
     * target directory or target file.
     *
     * @param moduleData GetMagentoModuleUtil.MagentoModuleData
     * @param targetDirectory PsiDirectory
     * @param targetFile PsiFile
     *
     * @return boolean
     */
    protected abstract boolean isVisible(
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    );

    /**
     * Implement to populate used in the target template variables.
     *
     * @param defaults AttributesDefaults
     * @param moduleData GetMagentoModuleUtil.MagentoModuleData
     * @param targetDirectory PsiDirectory
     * @param targetFile PsiFile
     *
     * @return AttributesDefaults
     */
    protected abstract AttributesDefaults getProperties(
            final @NotNull AttributesDefaults defaults,
            final @NotNull GetMagentoModuleUtil.MagentoModuleData moduleData,
            final PsiDirectory targetDirectory,
            final PsiFile targetFile
    );

    @Override
    protected @Nullable AttributesDefaults getAttributesDefaults(
            final @NotNull DataContext dataContext
    ) {
        return customDataContext.getData(ATTRIBUTE_DEFAULTS);
    }

    @Override
    protected FileTemplate getTemplate(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final FileTemplateManager manager = FileTemplateManager.getInstance(project);
        final FileTemplate template = manager.findInternalTemplate(moduleFile.getTemplate());
        template.setFileName(moduleFile.getFileName());

        return template;
    }

    protected @Nullable PsiDirectory getGlobalScopeDir(final @NotNull PsiDirectory directory) {
        PsiDirectory globalScopeDir;

        if (Package.moduleBaseAreaDir.equals(directory.getName())) {
            globalScopeDir = directory;
        } else {
            globalScopeDir = directory.getParentDirectory();
        }

        return globalScopeDir;
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
