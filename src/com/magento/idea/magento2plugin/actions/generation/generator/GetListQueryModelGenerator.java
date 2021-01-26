/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.GetListQueryModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQuery;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class GetListQueryModelGenerator extends FileGenerator {

    private final Project project;
    private final GetListQueryModelData queryModelData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final NamespaceBuilder queryModelNamespaceBuilder;
    private final boolean checkFileAlreadyExists;

    /**
     * Query model generator Constructor.
     *
     * @param queryModelData QueryModelData
     * @param project Project
     */
    public GetListQueryModelGenerator(
            final @NotNull GetListQueryModelData queryModelData,
            final @NotNull Project project
    ) {
        this(queryModelData, project, true);
    }

    /**
     * Query model generator Constructor.
     *
     * @param queryModelData QueryModelData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public GetListQueryModelGenerator(
            final @NotNull GetListQueryModelData queryModelData,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.project = project;
        this.queryModelData = queryModelData;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = ModuleIndex.getInstance(project);
        queryModelNamespaceBuilder = new NamespaceBuilder(
                queryModelData.getModuleName(),
                GetListQuery.CLASS_NAME,
                GetListQuery.DIRECTORY
        );
    }

    /**
     * Generate Get List Query model.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass getListQueryClass = GetPhpClassByFQN.getInstance(project).execute(
                GetListQuery.getClassFqn(queryModelData.getModuleName())
        );

        if (this.checkFileAlreadyExists && getListQueryClass != null) {
            return getListQueryClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                queryModelData.getModuleName()
        );
        final PsiDirectory queryModelBaseDir = directoryGenerator.findOrCreateSubdirectory(
                moduleBaseDir,
                GetListQuery.DIRECTORY
        );

        return fileFromTemplateGenerator.generate(
                GetListQuery.getInstance(),
                getAttributes(),
                queryModelBaseDir,
                actionName
        );
    }

    /**
     * Fill file property attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final List<String> uses = new LinkedList<>();

        uses.add(queryModelData.getCollectionTypeFactory());
        uses.add(queryModelData.getCollectionType());
        uses.add(queryModelData.getEntityDataMapperType());
        uses.add(FrameworkLibraryType.COLLECTION_PROCESSOR.getType());
        uses.add(FrameworkLibraryType.SEARCH_CRITERIA_BUILDER.getType());
        uses.add(FrameworkLibraryType.SEARCH_CRITERIA.getType());
        uses.add(FrameworkLibraryType.SEARCH_RESULT.getFactory());
        uses.add(FrameworkLibraryType.SEARCH_RESULT.getType());

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
        attributes.setProperty("ENTITY_NAME", queryModelData.getEntityName());
        attributes.setProperty("NAMESPACE", queryModelNamespaceBuilder.getNamespace());
        attributes.setProperty("CLASS_NAME", GetListQuery.CLASS_NAME);
        attributes.setProperty(
                "ENTITY_COLLECTION_FACTORY_TYPE",
                PhpClassGeneratorUtil.getNameFromFqn(
                        queryModelData.getCollectionTypeFactory()
                )
        );
        attributes.setProperty(
                "ENTITY_COLLECTION_TYPE",
                PhpClassGeneratorUtil.getNameFromFqn(
                        queryModelData.getCollectionType()
                )
        );
        attributes.setProperty(
                "ENTITY_DATA_MAPPER_TYPE",
                PhpClassGeneratorUtil.getNameFromFqn(
                        queryModelData.getEntityDataMapperType()
                )
        );
        attributes.setProperty("COLLECTION_PROCESSOR_TYPE",
                FrameworkLibraryType.COLLECTION_PROCESSOR.getTypeName());
        attributes.setProperty("SEARCH_CRITERIA_BUILDER_TYPE",
                FrameworkLibraryType.SEARCH_CRITERIA_BUILDER.getTypeName());
        attributes.setProperty("SEARCH_CRITERIA_TYPE",
                FrameworkLibraryType.SEARCH_CRITERIA.getTypeName());
        attributes.setProperty("SEARCH_RESULT_FACTORY_TYPE",
                FrameworkLibraryType.SEARCH_RESULT.getFactoryName());
        attributes.setProperty("SEARCH_RESULT_TYPE",
                FrameworkLibraryType.SEARCH_RESULT.getTypeName());
    }
}
