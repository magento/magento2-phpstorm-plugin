package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.SourceModelPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public class SourceModelGenerator extends FileGenerator {
    private final SourceModelData sourceModelData;
    private final CommonBundle commonBundle;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Constructor.
     *
     * @param project Project
     * @param sourceModelData SourceModelData
     */
    public SourceModelGenerator(final Project project, final SourceModelData sourceModelData) {
        super(project);

        this.project = project;
        this.sourceModelData = sourceModelData;
        this.commonBundle = new CommonBundle();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.validatorBundle = new ValidatorBundle();
        this.directoryGenerator = new DirectoryGenerator();
    }

    @Override
    public PsiFile generate(final String actionName) {
        final String errorTitle = commonBundle.message("common.error");
        final PhpClass dataPatchClass = GetPhpClassByFQN.getInstance(project)
                .execute(getSourceModelFqn());

        if (validateIfFileAlreadyExist(dataPatchClass, errorTitle)) {
            return null;
        }

        final PhpFile resourceModelFile = createResourceModelClass(actionName);

        if (validateIfFileCanBeCreated(errorTitle, resourceModelFile)) {
            return null;
        }

        return resourceModelFile;
    }

    private boolean validateIfFileAlreadyExist(
            final PhpClass dataPatchClass,
            final String errorTitle
    ) {
        if (dataPatchClass != null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.alreadyExists",
                    "Resource Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return true;
        }

        return false;
    }

    private PhpFile createResourceModelClass(final String actionName) {
        final PsiDirectory parentDirectory = getResourceModelDirectory();
        final Properties attributes = getAttributes();
        final PsiFile dataPatchFile = fileFromTemplateGenerator.generate(
                new SourceModelPhp(sourceModelData.getClassName()),
                attributes,
                parentDirectory,
                actionName
        );

        if (dataPatchFile == null) {
            return null;
        }

        return (PhpFile) dataPatchFile;
    }

    private PsiDirectory getResourceModelDirectory() {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(sourceModelData.getModuleName());
        final String[] dataPatchDirectories = sourceModelData.getDirectory().split(File.separator);

        for (final String sourceModelDirectory : dataPatchDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    sourceModelDirectory
            );
        }

        return parentDirectory;
    }

    private boolean validateIfFileCanBeCreated(
            final String errorTitle,
            final PhpFile dataPathFile
    ) {
        if (dataPathFile == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    "Resource Model Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return true;
        }
        return false;
    }

    private String getSourceModelFqn() {
        return sourceModelData.getNamespace()
                + Package.fqnSeparator
                + sourceModelData.getClassName();
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        final String abstractSourceClass =
                "Magento\\Eav\\Model\\Entity\\Attribute\\Source\\AbstractSource";
        final List<String> uses = new ArrayList<>();
        uses.add(abstractSourceClass);

        attributes.setProperty("NAME", sourceModelData.getClassName());
        attributes.setProperty("NAMESPACE", sourceModelData.getNamespace());
        attributes.setProperty(
                "EXTENDS",
                PhpClassGeneratorUtil.getNameFromFqn(abstractSourceClass)
        );
        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }
}
