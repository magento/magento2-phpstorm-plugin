package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.actions.SaveActionFile;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.magento.packages.code.BackendModuleType;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class SaveEntityControllerFileGenerator extends FileGenerator {

    private final SaveEntityControllerFileData fileData;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final Project project;
    private final boolean checkFileAlreadyExists;
    private final List<String> uses;

    /**
     * Save Entity Controller File Generator.
     *
     * @param fileData SaveEntityControllerFileData
     * @param project Project
     */
    public SaveEntityControllerFileGenerator(
            final @NotNull SaveEntityControllerFileData fileData,
            final @NotNull Project project
    ) {
        this(fileData, project, true);
    }

    /**
     * Save Entity Controller File Generator.
     *
     * @param fileData SaveEntityControllerFileData
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public SaveEntityControllerFileGenerator(
            final @NotNull SaveEntityControllerFileData fileData,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.fileData = fileData;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        moduleIndex = ModuleIndex.getInstance(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        uses = new ArrayList<>();
    }

    /**
     * Generate Save action controller for entity.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass saveActionClass = GetPhpClassByFQN.getInstance(project).execute(
                String.format(
                        "%s%s%s",
                        fileData.getNamespace(),
                        Package.fqnSeparator,
                        SaveActionFile.CLASS_NAME
                )
        );

        if (this.checkFileAlreadyExists && saveActionClass != null) {
            return saveActionClass.getContainingFile();
        }
        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                fileData.getModuleName()
        );
        final PsiDirectory baseDirectory = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                SaveActionFile.getDirectory(fileData.getEntityName())
        );

        return fileFromTemplateGenerator.generate(
                SaveActionFile.getInstance(),
                getAttributes(),
                baseDirectory,
                actionName
        );
    }

    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        uses.add(BackendModuleType.CONTEXT.getType());
        uses.add(FrameworkLibraryType.RESPONSE_INTERFACE.getType());
        uses.add(FrameworkLibraryType.RESULT_INTERFACE.getType());
        attributes.setProperty("NAMESPACE", fileData.getNamespace());
        attributes.setProperty("ENTITY_NAME", fileData.getEntityName());
        attributes.setProperty("CLASS_NAME", SaveActionFile.CLASS_NAME);
        attributes.setProperty("ENTITY_ID", fileData.getEntityId());
        attributes.setProperty("ADMIN_RESOURCE", SaveActionFile.getAdminResource(
                        fileData.getModuleName(),
                        fileData.getAcl()
                )
        );
        addProperty(attributes, "IMPLEMENTS", HttpMethod.POST.getInterfaceFqn());
        addProperty(attributes, "DATA_PERSISTOR", FrameworkLibraryType.DATA_PERSISTOR.getType());
        addProperty(attributes, "ENTITY_DTO", fileData.getDtoType());
        addProperty(attributes, "ENTITY_DTO_FACTORY", fileData.getDtoType().concat("Factory"));
        addProperty(attributes, "EXTENDS", BackendModuleType.EXTENDS.getType());
        addProperty(attributes, "SAVE_COMMAND", fileData.getSaveCommandFqn());
        addProperty(attributes, "DATA_OBJECT", FrameworkLibraryType.DATA_OBJECT.getType());
        addProperty(attributes, "COULD_NOT_SAVE", SaveActionFile.COULD_NOT_SAVE);

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
