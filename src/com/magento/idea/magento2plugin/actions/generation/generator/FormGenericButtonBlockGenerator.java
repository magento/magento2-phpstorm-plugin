/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.FormGenericButtonBlockData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.FormGenericButtonBlockFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class FormGenericButtonBlockGenerator extends FileGenerator {

    private final FormGenericButtonBlockData data;
    private final Project project;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;
    private final FormGenericButtonBlockFile file;

    /**
     * Generic Button Block generator constructor.
     *
     * @param data FormGenericButtonBlockData
     * @param project Project
     */
    public FormGenericButtonBlockGenerator(
            final @NotNull FormGenericButtonBlockData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Generic Button Block generator constructor.
     *
     * @param data FormGenericButtonBlockData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public FormGenericButtonBlockGenerator(
            final @NotNull FormGenericButtonBlockData data,
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
        file = new FormGenericButtonBlockFile();
    }

    /**
     * Generate ui component form generic block.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass genericButtonClass = GetPhpClassByFQN.getInstance(project).execute(
                file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
        );

        if (this.checkFileAlreadyExists && genericButtonClass != null) {
            return genericButtonClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );
        final PsiDirectory genericButtonBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                FormGenericButtonBlockFile.DIRECTORY
        );

        return fileFromTemplateGenerator.generate(
                new FormGenericButtonBlockFile(),
                getAttributes(),
                genericButtonBaseDir,
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
        final String entityIdGetter = "get" + Arrays.stream(data.getEntityId().split("_"))
                .map(s -> s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1))
                .collect(Collectors.joining());

        phpClassTypesBuilder
                .appendProperty("NAMESPACE",
                        file.getNamespaceBuilder(data.getModuleName()).getNamespace())
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", FormGenericButtonBlockFile.CLASS_NAME)
                .appendProperty("ENTITY_ID", data.getEntityId())
                .appendProperty("ENTITY_ID_GETTER", entityIdGetter)
                .append("CONTEXT", FormGenericButtonBlockFile.CONTEXT)
                .append("URL", FrameworkLibraryType.URL.getType())
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
