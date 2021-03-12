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
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DeleteEntityByIdCommandGenerator extends FileGenerator {

    private final Project project;
    private final DeleteEntityByIdCommandData deleteEntityByIdCommandData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;
    private final List<String> uses;

    /**
     * Delete entity command generator constructor.
     *
     * @param deleteEntityByIdCommandData DeleteEntityCommandData
     * @param project Project
     */
    public DeleteEntityByIdCommandGenerator(
            final @NotNull DeleteEntityByIdCommandData deleteEntityByIdCommandData,
            final @NotNull Project project
    ) {
        this(deleteEntityByIdCommandData, project, true);
    }

    /**
     * Delete entity command generator constructor.
     *
     * @param deleteEntityByIdCommandData DeleteEntityCommandData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public DeleteEntityByIdCommandGenerator(
            final @NotNull DeleteEntityByIdCommandData deleteEntityByIdCommandData,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.deleteEntityByIdCommandData = deleteEntityByIdCommandData;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = ModuleIndex.getInstance(project);
        uses = new ArrayList<>();
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
                deleteEntityByIdCommandData.getClassFqn()
        );

        if (this.checkFileAlreadyExists && deleteEntityCommandClass != null) {
            return deleteEntityCommandClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                deleteEntityByIdCommandData.getModuleName()
        );
        final PsiDirectory deleteCommandFileBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                DeleteEntityByIdCommandFile.getDirectory(deleteEntityByIdCommandData.getEntityName())
        );

        return fileFromTemplateGenerator.generate(
                new DeleteEntityByIdCommandFile(),
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
        attributes.setProperty("ENTITY_NAME", deleteEntityByIdCommandData.getEntityName());
        attributes.setProperty("NAMESPACE", deleteEntityByIdCommandData.getNamespace());
        attributes.setProperty("CLASS_NAME", DeleteEntityByIdCommandFile.CLASS_NAME);
        attributes.setProperty("ENTITY_ID", deleteEntityByIdCommandData.getEntityId());
        uses.add("Exception");
        addProperty(attributes, "COULD_NOT_DELETE", ExceptionType.COULD_NOT_DELETE.getType());
        addProperty(attributes, "NO_SUCH_ENTITY_EXCEPTION",
                ExceptionType.NO_SUCH_ENTITY_EXCEPTION.getType());
        addProperty(attributes, "LOGGER", FrameworkLibraryType.LOGGER.getType());

        final String modelType = deleteEntityByIdCommandData.getModelClassFqn();
        addProperty(attributes, "MODEL", modelType);

        final String modelFactoryType = modelType.concat("Factory");
        addProperty(attributes, "MODEL_FACTORY", modelFactoryType);

        final String resourceType = deleteEntityByIdCommandData.getResourceModelClassFqn();
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
