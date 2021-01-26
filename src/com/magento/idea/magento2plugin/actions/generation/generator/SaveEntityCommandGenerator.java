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
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.magento.packages.code.PhpCoreType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandGenerator extends FileGenerator {

    private final Project project;
    private final SaveEntityCommandData saveEntityCommandData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;

    /**
     * Save entity command generator constructor.
     *
     * @param saveEntityCommandData SaveEntityCommandData
     * @param project Project
     */
    public SaveEntityCommandGenerator(
            final @NotNull SaveEntityCommandData saveEntityCommandData,
            final @NotNull Project project
    ) {
        this(saveEntityCommandData, project, true);
    }

    /**
     * Save entity command generator constructor.
     *
     * @param saveEntityCommandData SaveEntityCommandData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SaveEntityCommandGenerator(
            final @NotNull SaveEntityCommandData saveEntityCommandData,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.saveEntityCommandData = saveEntityCommandData;
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
                saveEntityCommandData.getClassFqn()
        );

        if (this.checkFileAlreadyExists && saveEntityCommandClass != null) {
            return saveEntityCommandClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                saveEntityCommandData.getModuleName()
        );
        final PsiDirectory saveCommandFileBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                SaveEntityCommandFile.getDirectory(saveEntityCommandData.getEntityName())
        );

        return fileFromTemplateGenerator.generate(
                SaveEntityCommandFile.getInstance(),
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
        final List<String> uses = new LinkedList<>();
        uses.add(PhpCoreType.EXCEPTION.getType());
        uses.add(FrameworkLibraryType.DATA_OBJECT.getType());
        uses.add(ExceptionType.COULD_NOT_SAVE.getType());
        uses.add(FrameworkLibraryType.LOGGER.getType());

        final String dtoType = saveEntityCommandData.getDataModelClassFqn();
        uses.add(dtoType);
        attributes.setProperty("DTO", PhpClassGeneratorUtil.getNameFromFqn(dtoType));

        final String dtoProperty = CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL, saveEntityCommandData.getEntityName()
        );
        attributes.setProperty("DTO_PROPERTY", dtoProperty);

        final String modelType = saveEntityCommandData.getModelClassFqn();
        uses.add(modelType);
        attributes.setProperty("MODEL", PhpClassGeneratorUtil.getNameFromFqn(modelType));

        final String modelFactoryType = modelType.concat("Factory");
        uses.add(modelFactoryType);
        attributes.setProperty("MODEL_FACTORY",
                PhpClassGeneratorUtil.getNameFromFqn(modelFactoryType)
        );

        final String resourceType = saveEntityCommandData.getResourceModelClassFqn();
        uses.add(resourceType);
        attributes.setProperty("RESOURCE", PhpClassGeneratorUtil.getNameFromFqn(resourceType));

        attributes.setProperty("ENTITY_NAME", saveEntityCommandData.getEntityName());
        attributes.setProperty("NAMESPACE", saveEntityCommandData.getNamespace());
        attributes.setProperty("CLASS_NAME", SaveEntityCommandFile.CLASS_NAME);

        attributes.setProperty("EXCEPTION", PhpCoreType.EXCEPTION.getType());
        attributes.setProperty("DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getTypeName());
        attributes.setProperty("COULD_NOT_SAVE", ExceptionType.COULD_NOT_SAVE.getTypeName());
        attributes.setProperty("LOGGER", FrameworkLibraryType.LOGGER.getTypeName());

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }
}
