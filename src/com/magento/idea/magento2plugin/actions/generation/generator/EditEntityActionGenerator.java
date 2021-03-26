/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.EditEntityActionData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.actions.EditActionFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class EditEntityActionGenerator extends FileGenerator {

    private final Project project;
    private final EditEntityActionData data;
    private final boolean checkFileAlreadyExists;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final EditActionFile file;

    /**
     * Edit entity action/controller file generator constructor.
     *
     * @param data EditEntityActionData
     * @param project Project
     */
    public EditEntityActionGenerator(
            final @NotNull EditEntityActionData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Edit entity action/controller file generator constructor.
     *
     * @param data EditEntityActionData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public EditEntityActionGenerator(
            final @NotNull EditEntityActionData data,
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
        file = new EditActionFile(data.getEntityName());
    }

    /**
     * Generate edit action controller for entity.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass editActionClass = GetPhpClassByFQN.getInstance(project).execute(
                file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
        );

        if (this.checkFileAlreadyExists && editActionClass != null) {
            return editActionClass.getContainingFile();
        }

        final PsiDirectory editActionBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleIndex.getModuleDirectoryByModuleName(
                        data.getModuleName()
                ),
                file.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                editActionBaseDir,
                actionName
        );
    }

    /**
     * Fill edit action file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("NAMESPACE",
                        file.getNamespaceBuilder(data.getModuleName()).getNamespace())
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", file.getClassName())
                .appendProperty("ADMIN_RESOURCE", data.getAcl())
                .appendProperty("MENU_IDENTIFIER", data.getMenu())
                .append("EXTENDS", BackendModuleType.EXTENDS.getType())
                .append("IMPLEMENTS", HttpMethod.GET.getInterfaceFqn())
                .append("RESULT_INTERFACE", FrameworkLibraryType.RESULT_INTERFACE.getType())
                .append("RESULT_FACTORY", FrameworkLibraryType.RESULT_FACTORY.getType())
                .append("RESULT_PAGE", BackendModuleType.RESULT_PAGE.getType())
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
