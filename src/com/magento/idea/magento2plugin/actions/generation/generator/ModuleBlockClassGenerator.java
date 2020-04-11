/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.BlockFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.BlockPhp;
import com.magento.idea.magento2plugin.magento.files.PhpPreference;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.validators.ValidatorBundle;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.io.File;
import java.util.Properties;

public class ModuleBlockClassGenerator extends FileGenerator {
    private BlockFileData blockFileData;
    private Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    public ModuleBlockClassGenerator(@NotNull BlockFileData blockFileData, Project project) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.blockFileData = blockFileData;
        this.project = project;
    }

    public PsiFile generate(String actionName) {
        String errorTitle = "Error";
        PhpClass block = GetPhpClassByFQN.getInstance(project).execute(getBlockFqn());

        if (block != null) {
            String errorMessage = ValidatorBundle.message("validator.file.alreadyExists", "Block Class");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return null;
        }

        PhpFile blockFile = createBlockClass(actionName);
        if (blockFile == null) {
            String errorMessage = ValidatorBundle.message("validator.file.cantBeCreated", "Block Class");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return null;
        }

        return blockFile;
    }

    @NotNull
    private String getBlockFqn() {
        return blockFileData.getNamespace() + Package.FQN_SEPARATOR + blockFileData.getBlockClassName();
    }

    private PhpFile createBlockClass(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(getBlockModule());
        String[] blockDirectories = blockFileData.getBlockDirectory().split(File.separator);
        for (String blockDirectory: blockDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, blockDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile blockFile = fileFromTemplateGenerator.generate(PhpPreference.getInstance(blockFileData.getBlockClassName()), attributes, parentDirectory, actionName);
        if (blockFile == null) {
            return null;
        }
        return (PhpFile) blockFile;
    }

    protected void fillAttributes(Properties attributes) {
        String blockClassName = blockFileData.getBlockClassName();
        attributes.setProperty("NAME", blockClassName);
        attributes.setProperty("NAMESPACE", blockFileData.getNamespace());
        if (!BlockPhp.STOREFRONT_BLOCK_NAME.equals(blockClassName)) {
            attributes.setProperty("USE", BlockPhp.STOREFRONT_BLOCK_FQN);
            attributes.setProperty("EXTENDS", BlockPhp.STOREFRONT_BLOCK_NAME);
            return;
        }
        attributes.setProperty("EXTENDS", Package.FQN_SEPARATOR + BlockPhp.STOREFRONT_BLOCK_FQN);
    }

    public String getBlockModule() {
        return blockFileData.getBlockModule();
    }
}
