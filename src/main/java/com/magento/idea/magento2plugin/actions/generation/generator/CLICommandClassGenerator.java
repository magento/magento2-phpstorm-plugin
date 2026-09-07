/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandClassData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CLICommandTemplate;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.AvoidUncheckedExceptionsInSignatures",
        "PMD.AvoidThrowingRawExceptionTypes",
        "PMD.UselessParentheses",
        "PMD.AvoidUncheckedExceptionsInSignatures"
})
public class CLICommandClassGenerator extends FileGenerator {
    private final CLICommandClassData phpClassData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final DirectoryGenerator dirGenerator;
    private final FileFromTemplateGenerator fileGenerator;
    private final CommonBundle commonBundle;

    /**
     * Generates new CLI Command PHP Class based on provided data.
     *
     * @param project      Project
     * @param phpClassData CLICommandClassData
     */
    public CLICommandClassGenerator(
            final Project project,
            final @NotNull CLICommandClassData phpClassData
    ) {
        super(project);
        this.project = project;
        this.phpClassData = phpClassData;

        this.dirGenerator = DirectoryGenerator.getInstance();
        this.fileGenerator = new FileFromTemplateGenerator(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    @Override
    public PsiFile generate(final String actionName) throws RuntimeException {
        final PhpFile cliCommandFile = createCLICommandClass(actionName);

        if (cliCommandFile == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    commonBundle.message("common.cli.class.title")
            );

            throw new RuntimeException(errorMessage);
        }

        return cliCommandFile;
    }

    private @Nullable PhpFile createCLICommandClass(final String actionName) {
        final PsiDirectory parentDirectory = getParentDirectory();

        if (parentDirectory == null) {
            return null;
        }

        final PsiFile cliCommandFile = fileGenerator.generate(
                this.getCLICommandTemplate(),
                getAttributes(),
                parentDirectory,
                actionName
        );

        return (cliCommandFile == null) ? null : (PhpFile) cliCommandFile;
    }

    private PsiDirectory getParentDirectory() {
        final String moduleName = this.phpClassData.getModuleName();
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final String[] subDirectories = this.phpClassData.getParentDirectory().split("/");

        for (final String subDirectory : subDirectories) {
            parentDirectory = dirGenerator.findOrCreateSubdirectory(parentDirectory, subDirectory);
        }

        return parentDirectory;
    }

    private CLICommandTemplate getCLICommandTemplate() {
        final String name = this.phpClassData.getClassName();
        return new CLICommandTemplate(name);
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAMESPACE",
                this.phpClassData.getNamespace());
        attributes.setProperty("NAME",
                this.phpClassData.getClassName());
        attributes.setProperty("CLI_COMMAND_NAME",
                this.phpClassData.getCommandName());
        attributes.setProperty("CLI_COMMAND_DESCRIPTION",
                this.phpClassData.getCommandDescription());
    }
}
