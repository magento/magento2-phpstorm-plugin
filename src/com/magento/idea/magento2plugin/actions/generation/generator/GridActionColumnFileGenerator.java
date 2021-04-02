/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.GridActionColumnData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.GridActionColumnFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class GridActionColumnFileGenerator extends FileGenerator {

    private final Project project;
    private final GridActionColumnData data;
    private final GridActionColumnFile file;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final NamespaceBuilder classNamespaceBuilder;
    private final boolean checkFileAlreadyExists;
    private final List<String> uses;

    /**
     * Constructor for grid ui component action column class generator.
     *
     * @param data GridActionColumnData
     * @param project Project
     */
    public GridActionColumnFileGenerator(
            final @NotNull GridActionColumnData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Constructor for grid ui component action column class generator.
     *
     * @param data GridActionColumnData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public GridActionColumnFileGenerator(
            final @NotNull GridActionColumnData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.data = data;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        file = new GridActionColumnFile();
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = new ModuleIndex(project);
        uses = new ArrayList<>();
        classNamespaceBuilder = new NamespaceBuilder(
                data.getModuleName(),
                GridActionColumnFile.CLASS_NAME,
                GridActionColumnFile.DIRECTORY
        );
    }

    /**
     * Generate action column class.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass actionColumnClass = GetPhpClassByFQN.getInstance(project).execute(
                classNamespaceBuilder.getClassFqn()
        );

        if (this.checkFileAlreadyExists && actionColumnClass != null) {
            return actionColumnClass.getContainingFile();
        }

        final PsiDirectory actionColumnFileBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleIndex.getModuleDirectoryByModuleName(
                        data.getModuleName()
                ),
                file.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                actionColumnFileBaseDir,
                actionName
        );
    }

    /**
     * Fill index controller file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("ENTITY_NAME", data.getEntityName());
        attributes.setProperty("ENTITY_ID", data.getEntityIdColumn());
        attributes.setProperty("NAMESPACE", classNamespaceBuilder.getNamespace());
        attributes.setProperty("CLASS_NAME", GridActionColumnFile.CLASS_NAME);
        attributes.setProperty("EDIT_URL_PATH", data.getEditUrlPath());
        attributes.setProperty("DELETE_URL_PATH", data.getDeleteUrlPath());

        addProperty(attributes, "PARENT_CLASS", GridActionColumnFile.PARENT_CLASS);
        addProperty(attributes, "URL", FrameworkLibraryType.URL.getType());
        addProperty(attributes, "CONTEXT", GridActionColumnFile.CONTEXT);
        addProperty(attributes, "UI_COMPONENT_FACTORY", GridActionColumnFile.UI_COMPONENT_FACTORY);

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    /**
     * Add type to property list.
     *
     * @param properties Properties
     * @param propertyName String
     * @param type String
     */
    private void addProperty(
            final @NotNull Properties properties,
            final String propertyName,
            final String type
    ) {
        uses.add(type);
        properties.setProperty(propertyName, PhpClassGeneratorUtil.getNameFromFqn(type));
    }
}
