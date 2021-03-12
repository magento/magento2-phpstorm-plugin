package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.actions.DeleteActionFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.ExceptionType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityControllerFileGenerator extends FileGenerator {

    private final DeleteEntityControllerFileData fileData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final List<String> uses;

    /**
     * Delete Entity Controller File Generator.
     * @param fileData DeleteEntityControllerFileData
     * @param project Project
     */
    public DeleteEntityControllerFileGenerator(
            final DeleteEntityControllerFileData fileData,
            final @NotNull Project project
    ) {
        super(project);
        this.fileData = fileData;
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        moduleIndex = ModuleIndex.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        uses = new ArrayList<>();
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
                fileData.getModuleName()
        );
        final PsiDirectory baseDirectory = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                DeleteActionFile.getDirectory(fileData.getEntityName())
        );

        return fileFromTemplateGenerator.generate(
                DeleteActionFile.getInstance(),
                getAttributes(),
                baseDirectory,
                actionName
        );
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAMESPACE", fileData.getNamespace());
        attributes.setProperty("ENTITY_NAME", fileData.getEntityName());
        attributes.setProperty("CLASS_NAME", DeleteActionFile.CLASS_NAME);
        attributes.setProperty("ADMIN_RESOURCE", fileData.getAcl());
        attributes.setProperty("ENTITY_ID", fileData.getEntityId());
        addProperty(attributes, "DELETE_COMMAND", fileData.getDeleteCommandFqn());
        addProperty(attributes, "CONTEXT", BackendModuleType.CONTEXT.getType());
        addProperty(attributes, "RESULT_FACTORY", FrameworkLibraryType.RESULT_FACTORY.getType());
        addProperty(attributes, "RESULT_INTERFACE", FrameworkLibraryType.RESULT_INTERFACE.getType());
        addProperty(attributes, "EXTENDS", BackendModuleType.EXTENDS.getType());
        addProperty(attributes, "IMPLEMENTS_POST", HttpMethod.POST.getInterfaceFqn());
        addProperty(attributes, "IMPLEMENTS_GET", HttpMethod.GET.getInterfaceFqn());
        addProperty(attributes, "NO_SUCH_ENTITY_EXCEPTION",
                ExceptionType.NO_SUCH_ENTITY_EXCEPTION.getType());
        addProperty(attributes, "COULD_NOT_DELETE",
                ExceptionType.COULD_NOT_DELETE.getType());
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
