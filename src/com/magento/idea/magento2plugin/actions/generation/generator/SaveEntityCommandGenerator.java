/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.google.common.base.CaseFormat;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityCommandData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandGenerator extends FileGenerator {

    private final Project project;
    private final SaveEntityCommandData data;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;

    /**
     * Save entity command generator constructor.
     *
     * @param data SaveEntityCommandData
     * @param project Project
     */
    public SaveEntityCommandGenerator(
            final @NotNull SaveEntityCommandData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Save entity command generator constructor.
     *
     * @param data SaveEntityCommandData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SaveEntityCommandGenerator(
            final @NotNull SaveEntityCommandData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.data = data;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = ModuleIndex.getInstance(project);
    }

    /**
     * Generate save command for entity.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass saveEntityCommandClass = GetPhpClassByFQN.getInstance(project).execute(
                data.getClassFqn()
        );

        if (this.checkFileAlreadyExists && saveEntityCommandClass != null) {
            return saveEntityCommandClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );
        final PsiDirectory saveCommandFileBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                SaveEntityCommandFile.getDirectory(data.getEntityName())
        );

        return fileFromTemplateGenerator.generate(
                new SaveEntityCommandFile(),
                getAttributes(),
                saveCommandFileBaseDir,
                actionName
        );
    }

    /**
     * Fill save command file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAMESPACE", data.getNamespace());

        PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", SaveEntityCommandFile.CLASS_NAME)
                .append("EXCEPTION", "Exception")
                .append("DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType())
                .append("COULD_NOT_SAVE", ExceptionType.COULD_NOT_SAVE.getType())
                .append("LOGGER", FrameworkLibraryType.LOGGER.getType());

        final String dtoType = data.getDataModelClassFqn();
        final String dtoProperty = CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL, data.getEntityName()
        );
        final String modelType = data.getModelClassFqn();
        final String modelFactoryType = modelType.concat("Factory");
        final String resourceType = data.getResourceModelClassFqn();

        phpClassTypesBuilder.append("DTO", dtoType)
                .appendProperty("DTO_PROPERTY", dtoProperty)
                .append("MODEL", modelType)
                .append("MODEL_FACTORY", modelFactoryType)
                .append("RESOURCE", resourceType);

        phpClassTypesBuilder.mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }
}
