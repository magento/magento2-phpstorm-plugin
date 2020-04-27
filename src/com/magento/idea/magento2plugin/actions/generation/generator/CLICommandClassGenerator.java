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
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.CLICommandTemplate;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class CLICommandClassGenerator extends FileGenerator {
    private final CLICommandClassData cliCommandClassData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final CommonBundle commonBundle;

    public CLICommandClassGenerator(Project project, @NotNull CLICommandClassData cliCommandClassData) {
        super(project);
        this.project = project;
        this.cliCommandClassData = cliCommandClassData;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    public PsiFile generate(String actionName) {
        PhpFile cliCommandFile = createCLICommandClass(actionName);

        if (cliCommandFile == null) {
            String errorMessage = validatorBundle.message(
            "validator.file.cantBeCreated",
            commonBundle.message("common.cli.class.title")
            );

            throw new RuntimeException(errorMessage);
        }

        return cliCommandFile;
    }

    private PhpFile createCLICommandClass(String actionName) {
        PsiFile cliCommandFile = fileFromTemplateGenerator.generate(
                this.getCLICommandTemplate(),
                getAttributes(),
                this.getParentDirectory(),
                actionName
        );

        if (cliCommandFile == null) {
            return null;
        }

        return (PhpFile) cliCommandFile;
    }

    private PsiDirectory getParentDirectory() {
        String moduleName = this.cliCommandClassData.getModuleName();
        String[] cliCommandSubDirectories = this.cliCommandClassData.getParentDirectory().split("/");
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project).getModuleDirectoryByModuleName(moduleName);
        for (String cliCommandSubDirectory: cliCommandSubDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, cliCommandSubDirectory);
        }

        return parentDirectory;
    }

    private CLICommandTemplate getCLICommandTemplate() {
        String cliCommandClassName = this.cliCommandClassData.getClassName();
        return new CLICommandTemplate(cliCommandClassName);
    }

    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("NAMESPACE", this.cliCommandClassData.getNamespace());
        attributes.setProperty("NAME", this.cliCommandClassData.getClassName());
        attributes.setProperty("CLI_COMMAND_NAME", this.cliCommandClassData.getCommandName());
        attributes.setProperty("CLI_COMMAND_DESCRIPTION", this.cliCommandClassData.getCommandDescription());
    }
}
