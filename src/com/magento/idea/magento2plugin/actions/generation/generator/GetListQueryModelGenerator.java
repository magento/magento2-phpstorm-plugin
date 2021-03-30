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
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CollectionModelFile;
import com.magento.idea.magento2plugin.magento.files.EntityDataMapperFile;
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class GetListQueryModelGenerator extends FileGenerator {

    private final Project project;
    private final GetListQueryModelData data;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;
    private final GetListQueryFile file;

    /**
     * Query model generator Constructor.
     *
     * @param data QueryModelData
     * @param project Project
     */
    public GetListQueryModelGenerator(
            final @NotNull GetListQueryModelData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Query model generator Constructor.
     *
     * @param data QueryModelData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public GetListQueryModelGenerator(
            final @NotNull GetListQueryModelData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.project = project;
        this.data = data;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = ModuleIndex.getInstance(project);
        file = new GetListQueryFile(data.getEntityName());
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
                file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
        );

        if (this.checkFileAlreadyExists && getListQueryClass != null) {
            return getListQueryClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );
        final PsiDirectory queryModelBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                file.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                file,
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
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();
        final CollectionModelFile collectionModelFile =
                new CollectionModelFile(data.getCollectionName());
        final NamespaceBuilder collectionNamespace =
                collectionModelFile.getNamespaceBuilder(data.getModuleName(), data.getModelName());

        phpClassTypesBuilder
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty(
                        "NAMESPACE",
                        file.getNamespaceBuilder(data.getModuleName()).getNamespace()
                )
                .appendProperty("CLASS_NAME", GetListQueryFile.CLASS_NAME)
                .append(
                        "ENTITY_COLLECTION_TYPE",
                        collectionNamespace.getClassFqn()
                )
                .append(
                        "ENTITY_COLLECTION_FACTORY_TYPE",
                        collectionNamespace.getClassFqn().concat("Factory")
                )
                .append(
                        "ENTITY_DATA_MAPPER_TYPE",
                        new EntityDataMapperFile(
                                data.getEntityName()
                        ).getClassFqn(data.getModuleName())
                )
                .append(
                        "COLLECTION_PROCESSOR_TYPE",
                        FrameworkLibraryType.COLLECTION_PROCESSOR.getType()
                )
                .append(
                        "SEARCH_CRITERIA_BUILDER_TYPE",
                        FrameworkLibraryType.SEARCH_CRITERIA_BUILDER.getType()
                )
                .append(
                        "SEARCH_CRITERIA_TYPE",
                        FrameworkLibraryType.SEARCH_CRITERIA.getType()
                )
                .append(
                        "SEARCH_RESULT_TYPE",
                        FrameworkLibraryType.SEARCH_RESULT.getType()
                )
                .append(
                        "SEARCH_RESULT_FACTORY_TYPE",
                        FrameworkLibraryType.SEARCH_RESULT.getFactory()
                )
                .mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }
}
