/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.EntityDataMapperData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.magento.files.EntityDataMapperFile;
import com.magento.idea.magento2plugin.magento.files.ModelFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class EntityDataMapperGenerator extends FileGenerator {

    private final EntityDataMapperData data;
    private final Project project;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final EntityDataMapperFile file;
    private final boolean checkFileAlreadyExists;

    /**
     * Entity data mapper generator constructor.
     *
     * @param data EntityDataMapperData
     * @param project Project
     */
    public EntityDataMapperGenerator(
            final @NotNull EntityDataMapperData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Entity data mapper generator constructor.
     *
     * @param data EntityDataMapperData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public EntityDataMapperGenerator(
            final @NotNull EntityDataMapperData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.data = data;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        file = new EntityDataMapperFile(data.getEntityName());
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = new ModuleIndex(project);
    }

    /**
     * Generate entity data mapper class.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass entityDataMapperClass = GetPhpClassByFQN.getInstance(project).execute(
                file.getClassFqn(data.getModuleName())
        );
        if (this.checkFileAlreadyExists && entityDataMapperClass != null) {
            return entityDataMapperClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );
        final PsiDirectory entityDataMapperDir = directoryGenerator.findOrCreateSubdirectory(
                moduleBaseDir,
                file.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                entityDataMapperDir,
                actionName
        );
    }

    /**
     * Fill entity data mapper file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        final ModelFile modelFile = new ModelFile(data.getModuleName(), data.getModelName());
        final DataModelFile dtoFile = new DataModelFile(data.getDtoName());
        final DataModelInterfaceFile dtoInterfaceFile =
                new DataModelInterfaceFile(data.getDtoInterfaceName());
        String dtoType;

        if (data.isDtoWithInterface()) {
            dtoType = dtoInterfaceFile.getNamespaceBuilder(data.getModuleName()).getClassFqn();
        } else {
            dtoType = dtoFile.getNamespaceBuilder(data.getModuleName()).getClassFqn();
        }

        phpClassTypesBuilder
                .appendProperty("NAMESPACE", file.getNamespace(data.getModuleName()))
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", file.getClassName())
                .append("DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType())
                .append("DTO_TYPE", dtoType)
                .append("MAGENTO_MODEL_TYPE", modelFile.getClassFqn())
                .append("DTO_FACTORY", dtoType.concat("Factory"))
                .append("ABSTRACT_COLLECTION", FrameworkLibraryType.ABSTRACT_COLLECTION.getType())
                .mergeProperties(attributes);


        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }
}
