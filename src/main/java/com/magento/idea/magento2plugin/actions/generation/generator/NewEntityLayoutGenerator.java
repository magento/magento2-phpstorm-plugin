/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.NewEntityLayoutData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.NewEntityLayoutFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class NewEntityLayoutGenerator extends FileGenerator {

    private final Project project;
    private final NewEntityLayoutData data;
    private final NewEntityLayoutFile file;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;

    /**
     * New entity layout data constructor.
     *
     * @param data NewEntityLayoutData
     * @param project Project
     */
    public NewEntityLayoutGenerator(
            final @NotNull NewEntityLayoutData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * New entity layout data constructor.
     *
     * @param data NewEntityLayoutData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public NewEntityLayoutGenerator(
            final @NotNull NewEntityLayoutData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.data = data;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        file = new NewEntityLayoutFile(data.getNewActionPath().replace(File.separator, "_"));
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = new ModuleIndex(project);
    }

    /**
     * Generate new entity layout.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiFile layout = FileBasedIndexUtil.findModuleViewFile(
                file.getFileName(),
                Areas.adminhtml,
                data.getModuleName(),
                project,
                file.getParentDirectory()
        );

        if (this.checkFileAlreadyExists && layout != null) {
            return layout;
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );

        if (moduleBaseDir == null) {
            return null;
        }
        final PsiDirectory layoutBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                file.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                layoutBaseDir,
                actionName
        );
    }

    /**
     * Fill layout file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty(
                "EDIT_ENTITY_LAYOUT",
                data.getEditActionPath().replace(File.separator, "_")
        );
    }
}
