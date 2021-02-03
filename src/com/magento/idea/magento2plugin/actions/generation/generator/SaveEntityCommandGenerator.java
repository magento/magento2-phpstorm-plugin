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
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
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
    private final List<String> uses;

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
        uses = new ArrayList<>();
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
        attributes.setProperty("ENTITY_NAME", saveEntityCommandData.getEntityName());
        attributes.setProperty("NAMESPACE", saveEntityCommandData.getNamespace());
        attributes.setProperty("CLASS_NAME", SaveEntityCommandFile.CLASS_NAME);
        attributes.setProperty("EXCEPTION", "Exception");
        uses.add("Exception");
        addProperty(attributes, "DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType());
        addProperty(attributes, "COULD_NOT_SAVE", ExceptionType.COULD_NOT_SAVE.getType());
        addProperty(attributes, "LOGGER", FrameworkLibraryType.LOGGER.getType());

        final String dtoType = saveEntityCommandData.getDataModelClassFqn();
        addProperty(attributes, "DTO", dtoType);

        final String dtoProperty = CaseFormat.UPPER_CAMEL.to(
                CaseFormat.LOWER_CAMEL, saveEntityCommandData.getEntityName()
        );
        attributes.setProperty("DTO_PROPERTY", dtoProperty);

        final String modelType = saveEntityCommandData.getModelClassFqn();
        addProperty(attributes, "MODEL", modelType);

        final String modelFactoryType = modelType.concat("Factory");
        addProperty(attributes, "MODEL_FACTORY", modelFactoryType);

        final String resourceType = saveEntityCommandData.getResourceModelClassFqn();
        addProperty(attributes, "RESOURCE", resourceType);

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
