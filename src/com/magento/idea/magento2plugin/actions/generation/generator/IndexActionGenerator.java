/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.IndexActionData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.actions.IndexActionFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class IndexActionGenerator extends FileGenerator {

    private final Project project;
    private final IndexActionData data;
    private final IndexActionFile file;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;

    /**
     * Constructor for adminhtml index (list) controller generator.
     *
     * @param data EntityIndexAdminhtmlActionData
     * @param project Project
     */
    public IndexActionGenerator(
            final @NotNull IndexActionData data,
            final @NotNull Project project
    ) {
        this(data, project, true);
    }

    /**
     * Constructor for adminhtml index (list) controller generator.
     *
     * @param data EntityIndexAdminhtmlActionData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public IndexActionGenerator(
            final @NotNull IndexActionData data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.data = data;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        file = new IndexActionFile(data.getEntityName());
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = ModuleIndex.getInstance(project);
    }

    /**
     * Generate Index controller for Adminhtml area.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass indexActionClass = GetPhpClassByFQN.getInstance(project).execute(
                file.getNamespaceBuilder(data.getModuleName()).getClassFqn()
        );

        if (this.checkFileAlreadyExists && indexActionClass != null) {
            return indexActionClass.getContainingFile();
        }

        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                data.getModuleName()
        );
        final PsiDirectory indexActionFileBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                file.getDirectory()
        );

        return fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                indexActionFileBaseDir,
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
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        phpClassTypesBuilder
                .appendProperty("ENTITY_NAME", data.getEntityName())
                .appendProperty("CLASS_NAME", IndexActionFile.CLASS_NAME)
                .appendProperty("ACL", data.getAcl())
                .appendProperty("MENU", data.getMenu())
                .appendProperty(
                        "NAMESPACE",
                        file.getNamespaceBuilder(data.getModuleName()).getNamespace()
                )
                .append("PARENT_CLASS_NAME", BackendModuleType.EXTENDS.getType())
                .append("HTTP_GET_METHOD", HttpMethod.GET.getInterfaceFqn())
                .append("RESULT", FrameworkLibraryType.RESULT_INTERFACE.getType())
                .append("RESPONSE", FrameworkLibraryType.RESPONSE_INTERFACE.getType())
                .append("RESULT_FACTORY", FrameworkLibraryType.RESULT_FACTORY.getType())
                .mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }
}
