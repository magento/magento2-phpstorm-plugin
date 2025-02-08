/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public abstract class PhpFileGenerator extends FileGenerator {

    private static final String IMPORT_PROPERTY_NAME = "USES";

    protected AbstractPhpFile file;
    // Util that simplifies file properties definition.
    protected final PhpClassTypesBuilder typesBuilder;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;

    /**
     * Php file generator constructor.
     *
     * @param project Project
     * @param checkFileAlreadyExists boolean
     */
    public PhpFileGenerator(
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = new ModuleIndex(project);
        typesBuilder = new PhpClassTypesBuilder();
    }

    /**
     * Php file generation.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        file = getFile();

        final PhpClass phpClass = GetPhpClassByFQN.getInstance(project).execute(
                file.getClassFqn()
        );

        if (this.checkFileAlreadyExists && phpClass != null) {
            onClassAlreadyExists(phpClass);

            return phpClass.getContainingFile();
        }

        final PsiDirectory moduleDirectory = moduleIndex.getModuleDirectoryByModuleName(
                file.getModuleName()
        );

        if (moduleDirectory == null) {
            onFileGenerated(null, actionName);
            return null;
        }
        final PsiDirectory fileBaseDir = directoryGenerator.findOrCreateSubdirectories(
                moduleDirectory,
                file.getDirectory()
        );

        final PsiFile generatedFile = fileFromTemplateGenerator.generate(
                file,
                getAttributes(),
                fileBaseDir,
                actionName
        );
        onFileGenerated(generatedFile, actionName);

        return generatedFile;
    }

    /**
     * Class already exists behaviour.
     *
     * @param phpClass PhpClass
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    protected void onClassAlreadyExists(final @NotNull PhpClass phpClass) {
        JOptionPane.showMessageDialog(
                null,
                this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        file.getHumanReadableName() == null
                                ? file.getClassName() : file.getHumanReadableName()
                ),
                commonBundle.message("common.error"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Implement this method to add on file has been generated behaviour.
     *
     * @param generatedFile PsiFile
     * @param actionName String
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    protected void onFileGenerated(final PsiFile generatedFile, final @NotNull String actionName) {
        if (generatedFile == null) {
            JOptionPane.showMessageDialog(
                    null,
                    this.validatorBundle.message(
                            "validator.file.cantBeCreated",
                            file.getHumanReadableName() == null
                                    ? file.getClassName() : file.getHumanReadableName()
                    ),
                    commonBundle.message("common.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Initialize corresponding php file.
     *
     * @return AbstractPhpFile
     */
    protected abstract AbstractPhpFile initFile();

    /**
     * Get file object.
     *
     * @return AbstractPhpFile
     */
    protected AbstractPhpFile getFile() {
        if (file == null) {
            file = initFile();
        }

        return file;
    }

    @Override
    protected Properties getAttributes() {
        final @NotNull Properties attributes = super.getAttributes();

        if (typesBuilder.hasProperties()) {
            typesBuilder.mergeProperties(attributes);
        }

        if (!typesBuilder.getUses().isEmpty() && !typesBuilder.hasProperty(IMPORT_PROPERTY_NAME)) {
            attributes.setProperty(
                    IMPORT_PROPERTY_NAME,
                    PhpClassGeneratorUtil.formatUses(typesBuilder.getUses())
            );
        }

        return attributes;
    }
}
