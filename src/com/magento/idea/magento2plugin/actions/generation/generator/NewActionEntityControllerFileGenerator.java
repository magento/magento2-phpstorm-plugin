/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.NewActionEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.actions.NewActionFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NewActionEntityControllerFileGenerator extends FileGenerator {

    private final NewActionEntityControllerFileData fileData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final List<String> uses;

    /**
     * NewAction Entity Controller File Generator.
     * @param fileData NewActionEntityControllerFileData
     * @param project Project
     */
    public NewActionEntityControllerFileGenerator(
            final NewActionEntityControllerFileData fileData,
            final @NotNull Project project
    ) {
        super(project);
        this.fileData = fileData;
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        moduleIndex = new ModuleIndex(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        uses = new ArrayList<>();
    }

    /**
     * Generate NewAction controller for entity.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                fileData.getModuleName()
        );
        final PsiDirectory baseDirectory = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                NewActionFile.getDirectory(fileData.getEntityName())
        );

        return fileFromTemplateGenerator.generate(
                NewActionFile.getInstance(),
                getAttributes(),
                baseDirectory,
                actionName
        );
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAMESPACE", fileData.getNamespace());
        attributes.setProperty("ENTITY_NAME", fileData.getEntityName());
        attributes.setProperty("CLASS_NAME", NewActionFile.CLASS_NAME);
        addProperty(attributes, "EXTENDS", BackendModuleType.EXTENDS.getType());
        addProperty(attributes, "IMPLEMENTS", HttpMethod.GET.getInterfaceFqn());
        attributes.setProperty("ADMIN_RESOURCE", fileData.getAcl());
        attributes.setProperty("MENU_IDENTIFIER", fileData.getMenu());
        addProperty(attributes, "RESULT_INTERFACE",
                FrameworkLibraryType.RESULT_INTERFACE.getType()
        );
        addProperty(attributes, "RESULT_FACTORY", FrameworkLibraryType.RESULT_FACTORY.getType());
        addProperty(attributes, "RESULT_PAGE", BackendModuleType.RESULT_PAGE.getType());

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    /**
     * Add type to properties.
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
