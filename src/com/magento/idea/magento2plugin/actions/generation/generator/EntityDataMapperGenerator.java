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
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.EntityDataMapperFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class EntityDataMapperGenerator extends FileGenerator {

    private final EntityDataMapperData entityDataMapperData;
    private final Project project;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final EntityDataMapperFile entityDataMapperFile;
    private final boolean checkFileAlreadyExists;
    private final List<String> uses;

    /**
     * Entity data mapper generator constructor.
     *
     * @param entityDataMapperData EntityDataMapperData
     * @param project Project
     */
    public EntityDataMapperGenerator(
            final @NotNull EntityDataMapperData entityDataMapperData,
            final @NotNull Project project
    ) {
        this(entityDataMapperData, project, true);
    }

    /**
     * Entity data mapper generator constructor.
     *
     * @param entityDataMapperData EntityDataMapperData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public EntityDataMapperGenerator(
            final @NotNull EntityDataMapperData entityDataMapperData,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.entityDataMapperData = entityDataMapperData;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        entityDataMapperFile = EntityDataMapperFile
                .getInstance(entityDataMapperData.getEntityName());
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = ModuleIndex.getInstance(project);
        uses = new ArrayList<>();
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
                entityDataMapperData.getClassFqn()
        );
        if (this.checkFileAlreadyExists && entityDataMapperClass != null) {
            return entityDataMapperClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                entityDataMapperData.getModuleName()
        );
        final PsiDirectory entityDataMapperDir = directoryGenerator.findOrCreateSubdirectory(
                moduleBaseDir,
                entityDataMapperFile.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                entityDataMapperFile,
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
        attributes.setProperty("NAMESPACE", entityDataMapperData.getNamespace());
        attributes.setProperty("ENTITY_NAME", entityDataMapperData.getEntityName());
        attributes.setProperty("CLASS_NAME", entityDataMapperFile.getClassName());

        addProperty(attributes, "DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType());
        addProperty(attributes, "DTO_TYPE", entityDataMapperData.getDataModelClassFqn());
        addProperty(attributes, "MAGENTO_MODEL_TYPE", entityDataMapperData.getModelClassFqn());
        addProperty(
                attributes,
                "DTO_FACTORY",
                entityDataMapperData.getDataModelClassFqn().concat("Factory")
        );
        addProperty(
                attributes,
                "ABSTRACT_COLLECTION",
                FrameworkLibraryType.ABSTRACT_COLLECTION.getType()
        );

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    /**
     * Add type to property list.
     *
     * @param properties Properties
     * @param propertyName String
     * @param type String
     */
    protected void addProperty(
            final @NotNull Properties properties,
            final String propertyName,
            final String type
    ) {
        uses.add(type);
        properties.setProperty(propertyName, PhpClassGeneratorUtil.getNameFromFqn(type));
    }
}
