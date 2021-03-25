/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.DataModelFile;
import com.magento.idea.magento2plugin.magento.files.DataModelInterfaceFile;
import com.magento.idea.magento2plugin.magento.files.actions.SaveActionFile;
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SaveEntityControllerFileGenerator extends FileGenerator {

    private final SaveEntityControllerFileData data;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final Project project;
    private final boolean checkFileAlreadyExists;
    private final SaveActionFile file;

    /**
     * Save Entity Controller File Generator.
     *
     * @param data SaveEntityControllerFileData
     * @param project Project
     */
    public SaveEntityControllerFileGenerator(
            final @NotNull SaveEntityControllerFileData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Save Entity Controller File Generator.
     *
     * @param data SaveEntityControllerFileData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SaveEntityControllerFileGenerator(
            final @NotNull SaveEntityControllerFileData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.data = data;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        moduleIndex = ModuleIndex.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        file = new SaveActionFile(data.getEntityName());
    }

    /**
     * Generate Save action controller for entity.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass saveActionClass = GetPhpClassByFQN.getInstance(project).execute(
                file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
        );

        if (this.checkFileAlreadyExists && saveActionClass != null) {
            return saveActionClass.getContainingFile();
        }
        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );
        final PsiDirectory baseDirectory = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                file.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                baseDirectory,
                actionName
        );
    }

    /**
     * Fill save action file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        String dtoType;

        if (data.isDtoWithInterface()) {
            final DataModelInterfaceFile dataModelInterfaceFile =
                    new DataModelInterfaceFile(data.getDtoInterfaceName());
            dtoType = dataModelInterfaceFile
                    .getNamespaceBuilder(data.getModuleName()).getClassFqn();
        } else {
            final DataModelFile dataModelFile = new DataModelFile(data.getDtoName());
            dtoType = dataModelFile
                    .getNamespaceBuilder(data.getModuleName()).getClassFqn();
        }

        phpClassTypesBuilder
                .appendProperty("NAMESPACE",
                        file.getNamespaceBuilder(data.getModuleName()).getNamespace())
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", SaveActionFile.CLASS_NAME)
                .appendProperty("ENTITY_ID", data.getEntityId())
                .appendProperty("ADMIN_RESOURCE", data.getAcl())
                .append("IMPLEMENTS", HttpMethod.POST.getInterfaceFqn())
                .append("DATA_PERSISTOR", FrameworkLibraryType.DATA_PERSISTOR.getType())
                .append("EXTENDS", BackendModuleType.EXTENDS.getType())
                .append("ENTITY_DTO", dtoType)
                .append("ENTITY_DTO_FACTORY", dtoType.concat("Factory"))
                .append("SAVE_COMMAND",
                        new SaveEntityCommandFile(
                                data.getEntityName()
                        ).getClassFqn(data.getModuleName())
                )
                .append("DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType())
                .append("COULD_NOT_SAVE", SaveActionFile.COULD_NOT_SAVE)
                .append("CONTEXT", BackendModuleType.CONTEXT.getType())
                .append("RESPONSE_INTERFACE", FrameworkLibraryType.RESPONSE_INTERFACE.getType())
                .append("RESULT_INTERFACE", FrameworkLibraryType.RESULT_INTERFACE.getType())
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
