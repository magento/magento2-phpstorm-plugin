/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityByIdCommandData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityByIdCommandGenerator extends FileGenerator {

    private final Project project;
    private final DeleteEntityByIdCommandData data;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;
    private final DeleteEntityByIdCommandFile file;

    /**
     * Delete entity command generator constructor.
     *
     * @param data DeleteEntityCommandData
     * @param project Project
     */
    public DeleteEntityByIdCommandGenerator(
            final @NotNull DeleteEntityByIdCommandData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Delete entity command generator constructor.
     *
     * @param data DeleteEntityCommandData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public DeleteEntityByIdCommandGenerator(
            final @NotNull DeleteEntityByIdCommandData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.data = data;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = new ModuleIndex(project);
        file = new DeleteEntityByIdCommandFile(data.getEntityName());
    }

    /**
     * Generate delete command for entity.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass deleteEntityCommandClass = GetPhpClassByFQN.getInstance(project).execute(
                file.getClassFqn(data.getModuleName())
        );

        if (this.checkFileAlreadyExists && deleteEntityCommandClass != null) {
            return deleteEntityCommandClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );
        final PsiDirectory deleteCommandFileBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                DeleteEntityByIdCommandFile.getDirectory(data.getEntityName())
        );

        return fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                deleteCommandFileBaseDir,
                actionName
        );
    }

    /**
     * Fill delete command file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        final ModelFile modelFile = new ModelFile(data.getModuleName(), data.getModelName());
        final String modelType = modelFile.getClassFqn();
        final String modelFactoryType = modelType.concat("Factory");
        final ResourceModelFile resourceFile =
                new ResourceModelFile(data.getModuleName(), data.getResourceModelName());

        phpClassTypesBuilder
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("NAMESPACE", file.getNamespace(data.getModuleName()))
                .appendProperty("CLASS_NAME", DeleteEntityByIdCommandFile.CLASS_NAME)
                .appendProperty("ENTITY_ID", data.getEntityId())
                .append("Exception", "Exception")
                .append("COULD_NOT_DELETE", ExceptionType.COULD_NOT_DELETE.getType())
                .append("NO_SUCH_ENTITY_EXCEPTION",
                        ExceptionType.NO_SUCH_ENTITY_EXCEPTION.getType())
                .append("LOGGER", FrameworkLibraryType.LOGGER.getType())
                .append("MODEL", modelType)
                .append("MODEL_FACTORY", modelFactoryType)
                .append("RESOURCE", resourceFile.getClassFqn())
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
