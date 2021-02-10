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
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.FormGenericButtonBlockFile;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private final List<String> uses;

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
        uses = new ArrayList<>();
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
                data.getClassFqn()
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
        attributes.setProperty("NAMESPACE", data.getNamespace());
        attributes.setProperty("ENTITY_NAME", data.getEntityName());
        attributes.setProperty("CLASS_NAME", FormGenericButtonBlockFile.CLASS_NAME);
        attributes.setProperty("ENTITY_ID", data.getEntityId());

        final String entityIdGetter = "get" + Arrays.stream(data.getEntityId().split("_"))
                .map(s -> s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1))
                .collect(Collectors.joining());

        attributes.setProperty("ENTITY_ID_GETTER", entityIdGetter);
        addProperty(attributes, "CONTEXT", FormGenericButtonBlockFile.CONTEXT);
        addProperty(attributes, "URL", FrameworkLibraryType.URL.getType());
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
