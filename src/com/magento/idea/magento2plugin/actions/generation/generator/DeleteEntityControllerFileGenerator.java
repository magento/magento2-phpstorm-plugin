package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.actions.DeleteActionFile;
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityControllerFileGenerator extends FileGenerator {

    private final DeleteEntityControllerFileData data;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final DeleteActionFile file;

    /**
     * Delete Entity Controller File Generator.
     * @param data DeleteEntityControllerFileData
     * @param project Project
     */
    public DeleteEntityControllerFileGenerator(
            final DeleteEntityControllerFileData data,
            final @NotNull Project project
    ) {
        super(project);
        this.data = data;
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        moduleIndex = new ModuleIndex(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        file = new DeleteActionFile(data.getEntityName());
    }

    /**
     * Generate Delete controller.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
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
     * Fill delete action file attributes.
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
                .appendProperty("CLASS_NAME", DeleteActionFile.CLASS_NAME)
                .appendProperty("ADMIN_RESOURCE", data.getAcl())
                .appendProperty("ENTITY_ID", data.getEntityId())
                .append("DELETE_COMMAND",
                        new DeleteEntityByIdCommandFile(
                                data.getEntityName()
                        ).getClassFqn(data.getModuleName())
                )
                .append("CONTEXT", BackendModuleType.CONTEXT.getType())
                .append("RESULT_FACTORY", FrameworkLibraryType.RESULT_FACTORY.getType())
                .append("RESULT_INTERFACE", FrameworkLibraryType.RESULT_INTERFACE.getType())
                .append("EXTENDS", BackendModuleType.EXTENDS.getType())
                .append("IMPLEMENTS_POST", HttpMethod.POST.getInterfaceFqn())
                .append("IMPLEMENTS_GET", HttpMethod.GET.getInterfaceFqn())
                .append("NO_SUCH_ENTITY_EXCEPTION",
                        ExceptionType.NO_SUCH_ENTITY_EXCEPTION.getType())
                .append("COULD_NOT_DELETE", ExceptionType.COULD_NOT_DELETE.getType())
                .mergeProperties(attributes);

        attributes.setProperty("USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses()));
    }
}
