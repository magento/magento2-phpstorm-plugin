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
import com.magento.idea.magento2plugin.magento.files.PhpPreference;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.io.File;
import java.util.Properties;

public class BlockClassGenerator extends FileGenerator {
    private BlockFileData blockFileData;
    private Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    public BlockClassGenerator(@NotNull BlockFileData blockFileData, Project project) {
        super(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.blockFileData = blockFileData;
        this.project = project;
    }

    public PsiFile generate(String actionName) {
        PhpClass block = GetPhpClassByFQN.getInstance(project).execute(blockFileData.getBlockFqn());
        if (block != null) {
           return null;
        }
        PhpFile blockFile = createBlockClass(actionName);
        if (blockFile == null) {
            JOptionPane.showMessageDialog(null, "Block Class cant be created!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return blockFile;
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
        String parentClassName = blockFileData.getTargetClass().getName();
        if (!parentClassName.equals(blockClassName)) {
            attributes.setProperty("USE", blockFileData.getTargetClass().getPresentableFQN());
            attributes.setProperty("EXTENDS", parentClassName);
            return;
        }
        attributes.setProperty("EXTENDS", blockFileData.getTargetClass().getFQN());
    }

    public String getBlockModule() {
        return blockFileData.getBlockModule();
    }
}
